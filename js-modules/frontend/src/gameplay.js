import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import utilsCore from "./utils2D"
import {daoApi} from "./dao"

import auth from "./auth"

export default {

    init(globalState) {
        if (ctx.gameplay) {
            return
        }

        //
        //jcBase.cfg.envDev = false

        // Инициализация контекста
        ctx.gameplay = this
        ctx.eventBus = apx.app.eventBus

        // Подписка на события
        ctx.eventBus.on("dragstart", this.on_dragstart)
        ctx.eventBus.on("drag", this.on_drag)
        ctx.eventBus.on("dragend", this.on_dragend)
        ctx.eventBus.on("change:goal.value", this.onChange_goalValue)
        ctx.eventBus.on("showHint", this.onShowHint)
        //
        ctx.eventBus.on("*", this.onEvent)
    },

    shutdown() {
        // Подписка на события
        ctx.eventBus.off("dragstart", this.on_dragstart)
        ctx.eventBus.off("drag", this.on_drag)
        ctx.eventBus.off("dragend", this.on_dragend)
        ctx.eventBus.off("change:goal.value", this.onChange_goalValue)
        ctx.eventBus.off("showHint", this.onShowHint)
        //
        ctx.eventBus.off("*", this.onEvent)
        //
        ctx.gameplay = null
    },

    async getUserInfo() {
        let res = await apx.jcBase.ajax.request({
            url: "auth/getUserInfo",
        })

        this.globalState.user.id = res.data.id
        this.globalState.user.login = res.data.login
        this.globalState.user.text = res.data.text
        this.globalState.user.color = res.data.color
        //
        if (!this.globalState.user.id) {
            this.globalState.user.id = 0
        }
    },

    /**
     * Грузим новое задание с сервера
     */
    async loadNextTask() {
        if (!auth.isAuth() || !ctx.globalState.game.id) {
            return
        }

        // Если не отправляли ответ - отправим
        if (ctx.globalState.gameTask.task && !ctx.globalState.dataState.mode.postTaskAnswerDone) {
            await ctx.gameplay.api_postTaskAnswer(ctx.globalState.gameTask.task.id, {wasSkip: true})
        }

        // Текущее задание скрываем
        ctx.globalState.gameTask = {}

        // Грузим новое задание с сервера
        let dataGameTask = await ctx.gameplay.api_choiceTask()

        // Задание в глобальном контексте
        this.useGameTask(dataGameTask)
    },

    /**
     * Грузим текущее задание с сервера
     */
    async loadCurrentTask() {
        // Задание и раунд в глобальном контексте
        if (!ctx.globalState.gameTask.task) {
            // Грузим текущее задание с сервера
            let dataGameTask = await ctx.gameplay.api_getCurrentTask(ctx.globalState.game.id)

            // Задание в глобальном контексте
            this.useGameTask(dataGameTask)
        }
    },

    async loadCurrentOrNextTask() {
        // Грузим текущее задание с сервера
        if (!ctx.globalState.gameTask.task) {
            await ctx.gameplay.loadCurrentTask()
        }

        // Грузим следующее задание с сервера
        if (!ctx.globalState.gameTask.task) {
            await ctx.gameplay.loadNextTask()
        }
    },

    async gameStart(idPlan) {
        let resGame = await ctx.gameplay.api_gameStart(idPlan)

        // Задание и раунд в глобальном контексте
        ctx.gameplay.useGame(resGame)
        ctx.globalState.gameTask = {}

        //
        await ctx.gameplay.loadNextTask()
    },

    async loadActiveGame() {
        let resGame = await ctx.gameplay.api_getActiveGame()

        if (resGame) {
            // Игра в глобальном контексте
            if (ctx.globalState.game && resGame.game &&
                ctx.globalState.game.id !== resGame.game.id) {
                ctx.gameplay.useGame(resGame)
                ctx.globalState.gameTask = {}
            }
        } else {
            ctx.gameplay.clearGame()
        }

        return resGame
    },

    async loadLastGame() {
        let resGame = await ctx.gameplay.api_getLastGame()

        if (resGame) {
            // Игра в глобальном контексте
            if (ctx.globalState.game && resGame.game &&
                ctx.globalState.game.id !== resGame.game.id) {
                ctx.gameplay.useGame(resGame)
                ctx.globalState.gameTask = {}
            }
        } else {
            ctx.gameplay.clearGame()
        }

        return resGame
    },

    async closeActiveGame() {
        await ctx.gameplay.api_closeActiveGame()
        ctx.gameplay.clearGame()
    },

    useGameTask(dataGameTask) {
        if (dataGameTask.task) {
            // Каждому варианту ответа проставляем id задания - нужно в интерфейсе
            for (let i = 0; i < dataGameTask.taskOptions.length; i++) {
                let taskOption = dataGameTask.taskOptions[i]
                taskOption.task = dataGameTask.task.id
            }

            // Перемешаем ответы
            dataGameTask.taskOptions = ctx.gameplay.shuffleTaskOptions(dataGameTask.taskOptions)

            // Задание в глобальный контекст
            ctx.globalState.gameTask.task = dataGameTask.task
            ctx.globalState.gameTask.taskOptions = dataGameTask.taskOptions

            // Состояние цели
            ctx.gameplay.resetGoal(dataGameTask.task.text)
        } else {
            // Задание в глобальный контекст
            ctx.globalState.gameTask = {}

            // Состояние цели
            ctx.gameplay.resetGoal(null)
        }

        // Состояние раунда в глобальный контекст
        ctx.gameplay.useGame(dataGameTask.game)

        // Уведомим об изменении задания
        ctx.eventBus.emit("loadedGameTask", dataGameTask)

        // Разные умолчания
        ctx.globalState.dataState.mode.modeShowOptions = null
        ctx.globalState.dataState.mode.postTaskAnswerDone = false
    },

    clearGame() {
        ctx.globalState.game = {}
        ctx.globalState.gameTask = {}
        ctx.globalState.tasksResult = {}
    },

    useGame(res) {
        ctx.globalState.game = res.game
        ctx.globalState.tasksResult = res.tasksResult
        //
        if (ctx.globalState.game.dend) {
            ctx.globalState.game.done = true
        }
    },

    async api_gameStart(idPlan) {
        let resApi = await daoApi.loadStore('m/Game/gameStart', [idPlan])
        return ctx.gameplay.parseResApiGame(resApi)
    },

    async api_getLastGame() {
        let resApi = await daoApi.loadStore('m/Game/getLastGame', [])
        return ctx.gameplay.parseResApiGame(resApi)
    },

    async api_getActiveGame() {
        let resApi = await daoApi.loadStore('m/Game/getActiveGame', [])
        return ctx.gameplay.parseResApiGame(resApi)
    },

    async api_closeActiveGame() {
        await daoApi.invoke('m/Game/closeActiveGame', [])
    },

    async api_getPlans() {
        let res = await daoApi.loadStore(
            "m/Game/getPlans", {}
        )

        return res.records
    },

    async api_choiceTask() {
        let resApi = await daoApi.loadStore('m/Game/choiceTask', [ctx.globalState.game.id])
        return ctx.gameplay.parseResApiTask(resApi)
    },

    // Загрузить текущее задание
    async api_getCurrentTask() {
        let resApi = await daoApi.loadStore('m/Game/currentTask', [ctx.globalState.game.id])
        return ctx.gameplay.parseResApiTask(resApi)
    },

    parseResApiGame(resApi) {
        let res = {}

        //
        if (resApi.game) {
            res.game = resApi.game.records[0]
        } else {
            res.game = null
        }

        //
        if (resApi.tasksResult) {
            res.tasksResult = resApi.tasksResult.records
        } else {
            res.tasksResult = null
        }

        // Если пришел расширенный ответ - со статистикой
        if (resApi.statistic) {
            res.statistic = resApi.statistic
        } else {
            res.statistic = null
        }

        //
        return res
    },

    parseResApiTask(resApi) {
        let res

        //
        if (resApi.task) {
            res = {
                task: resApi.task.records[0],
                taskOptions: resApi.taskOption.records,
                game: ctx.gameplay.parseResApiGame(resApi),
            }
        } else {
            res = {
                task: null,
                taskOptions: null,
                game: ctx.gameplay.parseResApiGame(resApi),
            }
        }

        //
        return res
    },

    api_postTaskAnswer(idGameTask, taskResult) {
        if (ctx.globalState.dataState.mode.postTaskAnswerDone) {
            return
        }
        ctx.globalState.dataState.mode.postTaskAnswerDone = true

        //
        let res = daoApi.loadStore('m/Game/postTaskAnswer', [idGameTask, taskResult])

        //
        return res
    },

    async api_login(login, password) {
        let res = await apx.jcBase.ajax.request({
            url: "auth/login",
            params: {login: login, password: password},
        })

        //
        this.assignAuthUserInfo(res.data)
    },

    async api_updUsr(userInfo) {
        let res = await apx.jcBase.ajax.request({
            url: "auth/updUsr",
            params: {
                text: userInfo.text, login: userInfo.login, password: userInfo.password
            },
        })

        //
        this.assignAuthUserInfo(res.data)
    },

    async api_register(text, login, password) {
        let res = await apx.jcBase.ajax.request({
            url: "auth/register",
            params: {text: text, login: login, password: password},
        })

        //
        this.assignAuthUserInfo(res.data)
    },

    async api_logout() {
        let res = await apx.jcBase.ajax.request({
            url: "auth/logout",
        })

        //
        this.clearAuthUserInfo()

        // Задание и раунд в глобальном контексте
        ctx.gameplay.clearGame()
    },

    /**
     * Записывает данные пользователя, пришедшие от ajax-запроса авторизации auth/login
     * в переменную, которая возвращается фунцией auth.getUserInfo(),
     * что дает состояние залогиненности в приложение.
     *
     * Избавляет от необходимости перезагружать страницу,
     * чтобы получить состояние залогинености.
     *
     * @param data данные пользователя
     */
    assignAuthUserInfo(data) {
        let userInfo = auth.getUserInfo()
        userInfo.id = data.id
        userInfo.login = data.login
        userInfo.text = data.text
        userInfo.color = data.color
        userInfo.guest = data.guest
    },

    /**
     * Записывает данные пользователя, пришедшие от ajax-запроса авторизации auth/login
     * в переменную, которая возвращается фунцией auth.getUserInfo(),
     * что дает состояние залогиненности в приложение.
     *
     * Избавляет от необходимости перезагружать страницу,
     * чтобы получить состояние залогинености.
     *
     * @param data данные пользователя
     */
    clearAuthUserInfo() {
        let userInfo = auth.getUserInfo()
        userInfo.id = null
        userInfo.login = null
        userInfo.text = null
        userInfo.color = null
        userInfo.guest = null
    },


    // Сбрасываем состояние результата (цели)
    resetGoal(text) {
        ctx.globalState.dataState.mode.goalHitSize = ctx.settings.goalHitSizeDefault
        ctx.globalState.dataState.goal.text = text
        ctx.globalState.dataState.goal.valueGoal = ctx.settings.goalHitSizeDefault
        ctx.globalState.dataState.goal.valueDone = 0
    },


    on_dragstart(eventDrag) {
        let elBall = document.getElementById("ball")
        let elGoal = document.getElementById("goal")

        let taskOption = eventDrag.taskOption

        let stateDrag = ctx.globalState.dataState.drag
        let stateGoal = ctx.globalState.dataState.goal
        let stateBall = ctx.globalState.dataState.ball

        stateDrag.dtStart = new Date()
        stateDrag.sx = eventDrag.x
        stateDrag.sy = eventDrag.y
        stateDrag.x = eventDrag.x
        stateDrag.y = eventDrag.y

        //
        ctx.gameplay.stopMoveAnimation()

        //
        stateBall.value = 1
        stateBall.text = taskOption.text

        //
        stateBall.x = stateDrag.x
        stateBall.y = stateDrag.y
    },

    on_drag(eventDrag) {

        //
        let stateDrag = ctx.globalState.dataState.drag
        let stateGoal = ctx.globalState.dataState.goal
        let stateBall = ctx.globalState.dataState.ball

        //
        let elBall = document.getElementById("ball")

        //
        stateDrag.x = eventDrag.x
        stateDrag.y = eventDrag.y

        //
        stateBall.x = stateDrag.x
        stateBall.y = stateDrag.y
    },

    on_dragend(eventDrag) {
        let stateDrag = ctx.globalState.dataState.drag
        let stateGoal = ctx.globalState.dataState.goal
        let stateBall = ctx.globalState.dataState.ball
        let stateMode = ctx.globalState.dataState.mode


        // Уведомим сервер
        ctx.gameplay.api_postTaskAnswer(eventDrag.taskOption.task, {
            wasTrue: eventDrag.taskOption.isTrue,
            wasFalse: !eventDrag.taskOption.isTrue
        })

        //
        let elBall = document.getElementById("ball")
        let goal = document.getElementById("goal")

        //
        stateDrag.x = eventDrag.x
        stateDrag.y = eventDrag.y
        stateDrag.dtEnd = new Date()
        stateDrag.duration = Math.abs(stateDrag.dtEnd - stateDrag.dtStart)

        // Скорость броска, пкс/сек
        let dx = 1000 * (stateDrag.x - stateDrag.sx) / stateDrag.duration
        let dy = 1000 * (stateDrag.y - stateDrag.sy) / stateDrag.duration


        // Просто клик - пусть летит вверх
        if (dx === 0 && dy === 0) {
            dy = -1
        }
        // Чтобы  летело не слишком...
        let dl = Math.sqrt(dx * dx + dy * dy)
        // ... не слишком медленно...
        let x = ctx.settings
        if (dl < ctx.settings.minDl) {
            dx = dx * ctx.settings.minDl / dl
            dy = dy * ctx.settings.minDl / dl
        }
        // ... и не слишком быстро
        if (dl > ctx.settings.maxDl) {
            dx = dx * ctx.settings.maxDl / dl
            dy = dy * ctx.settings.maxDl / dl
        }

        // Учтем количество кадров в секунду
        let framePerSec = 1000 / ctx.settings.animationInterval
        stateDrag.dx = dx / framePerSec
        stateDrag.dy = dy / framePerSec

        // Выбрали ответ - отреагируем
        if (ctx.gameplay.isOptionIsTrueAnswer(eventDrag.taskOption)) {
            // Выбрали правильный ответ
            ctx.globalState.dataState.ball.ballIsTrue = true
            //stateMode.modeShowOptions = null
            stateMode.modeShowOptions = "hint-true"
        } else {
            // Выбрали не правильный ответ
            ctx.globalState.dataState.ball.ballIsTrue = false
            stateBall.text = ""
            stateGoal.valueGoal = stateGoal.valueGoal + 1
            if (stateGoal.valueGoal > ctx.settings.valueGoalMax) {
                stateGoal.valueGoal = ctx.settings.valueGoalMax
            }
            // Шарик никуда не летит
            stateDrag.dx = 0
            stateDrag.dy = 0
            // Покажем подсказки
            stateMode.modeShowOptions = "hint-true"
            stateMode.goalHitSize = ctx.settings.goalHitSizeError
        }
        stateBall.value = 1

        // Показать текст подсказки после первого выбора
        ctx.globalState.dataState.showTaskHint = true

        // Показать текст подсказки после первого выбора
        ctx.globalState.dataState.showTaskHint = true

        //
        ctx.eventBus.emit("taskOptionSelected", eventDrag.taskOption)

        //
        ctx.gameplay.startMoveAnimation(elBall)
    },

    isOptionIsTrueAnswer(taskOption) {
        if (taskOption.isTrue) {
            return true
        } else {
            return false
        }
    },

    shuffleTaskOptions(taskOptions) {
        let taskOptionsRes = []

        //
        for (let i = 0; i < taskOptions.length; i++) {
            let taskOption = taskOptions[i]
            if (Math.random() * 2 > 1) {
                taskOptionsRes.push(taskOption)
            } else {
                taskOptionsRes.unshift(taskOption)
            }
        }

        //
        return taskOptionsRes
    },

    startMoveAnimation() {
        let stateDrag = ctx.globalState.dataState.drag

        //
        stateDrag.interval = setInterval(ctx.gameplay.animationStep, ctx.settings.animationInterval)
    },

    stopMoveAnimation() {
        let stateDrag = ctx.globalState.dataState.drag

        //
        clearInterval(stateDrag.interval)
        stateDrag.interval = null
    },

    /**
     * Глобальный менеджер анимации.
     * Меняет данные в ctx.globalState.dataState так, чтобы рисовалась нужная анимация.
     * Отрисовка идет в компонентах на основе этого состояния.
     */
    animationStep() {
        let elBall = document.getElementById("ball")
        let elGoal = document.getElementById("goal")

        //
        let stateDrag = ctx.globalState.dataState.drag
        let stateGoal = ctx.globalState.dataState.goal
        let stateBall = ctx.globalState.dataState.ball
        let stateMode = ctx.globalState.dataState.mode

        // Шаг
        stateDrag.x = stateDrag.x + stateDrag.dx
        stateDrag.y = stateDrag.y + stateDrag.dy

        // Пересечения определяем как пересечение отрезка, проведенного
        // от предыдущей точки движения до текущей с прямоугольником цели.
        // Так мы не дадим "проскочить" снаряду сквозь цель
        // при слишком большой скорости движения.
        let rectTrace = {
            x1: stateDrag.x,
            x2: stateDrag.x - stateDrag.dx,
            y1: stateDrag.y,
            y2: stateDrag.y - stateDrag.dy,
        }
        let rectGoal = utilsCore.getElRect(elGoal)
        //
        if (utilsCore.intersectRect(rectTrace, rectGoal)) {
            if (ctx.globalState.dataState.ball.ballIsTrue) {
                stateGoal.valueDone = stateGoal.valueDone + stateMode.goalHitSize
            }

            //
            ctx.gameplay.stopMoveAnimation()

            //
            stateBall.value = 0

            //
            ctx.eventBus.emit("change:goal.value", stateGoal.value)

            // Перемешаем ответы, если цель не поражена
            if (!ctx.gameplay.goalDone()) {
                ctx.globalState.gameTask.taskOptions = ctx.gameplay.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

                // Снова выбираем
                stateMode.modeShowOptions = null
            }

            //
            return
        }

        // Выход шарика за границы экрана
        if (stateDrag.x + ctx.settings.ballWidth > innerWidth || stateDrag.x < 0 || stateDrag.y + ctx.settings.ballHeihth > innerHeight || stateDrag.y < 0) {
            //
            ctx.gameplay.stopMoveAnimation()

            //
            stateBall.value = 0

            // Перемешаем ответы
            ctx.globalState.gameTask.taskOptions = ctx.gameplay.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

            // Снова выбираем
            stateMode.modeShowOptions = null

            //
            return
        }

        // Рост или уменьшение по ходу движения
        let framePerSec = 1000 / ctx.settings.animationInterval
        if (!ctx.globalState.dataState.ball.ballIsTrue) {
            stateBall.value = stateBall.value - 1.5 / framePerSec
        } else {
            stateBall.value = stateBall.value + 1.1 / framePerSec
        }

        // Выход размера шарика за ограничение размера
        if (stateBall.value > 5 || stateBall.value < 0) {
            //
            ctx.gameplay.stopMoveAnimation()

            // Прекращаем полет шарика
            stateBall.value = 0

            // Перемешаем ответы
            ctx.globalState.gameTask.taskOptions = ctx.gameplay.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

            // Снова выбираем
            stateMode.modeShowOptions = null
        }

        // Продолжение движения
        stateBall.x = stateDrag.x
        stateBall.y = stateDrag.y
    },


    goalDone() {
        return ctx.globalState.dataState.goal.valueDone >= ctx.globalState.dataState.goal.valueGoal
    },

    onChange_goalValue(v) {
        if (ctx.gameplay.goalDone()) {
            ctx.gameplay.loadNextTask()
        }
    },

    onShowHint(doShowHint) {
        if (doShowHint) {
            // Показывать подсказки
            ctx.globalState.dataState.mode.modeShowOptions = "hint-true"

            // Уменьшим силу удара
            if (ctx.globalState.dataState.mode.goalHitSize > ctx.settings.goalHitSizeHint) {
                ctx.globalState.dataState.mode.goalHitSize = ctx.settings.goalHitSizeHint
            }

            // Если не отправляли ответ - отправим
            if (!ctx.globalState.dataState.mode.postTaskAnswerDone) {
                ctx.gameplay.api_postTaskAnswer(ctx.globalState.gameTask.task.id, {wasHint: true})
            }
        }
    },

    onEvent(ev, obj) {
        //console.log("gameplay.event", ev, obj)
        //console.log("state", ctx.state)
    },


}
