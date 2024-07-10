import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import animation from "./animation"
import utilsCore from "./utils2D"
import {daoApi} from "./dao"

import auth from "./auth"

export default {

    init() {
        if (ctx.gameplay) {
            return
        }

        //
        //jcBase.cfg.envDev = false

        // Инициализация контекста
        ctx.gameplay = this
        ctx.eventBus = apx.app.eventBus

        // Начальное состояние контекста
        ctx.gameplay.globalClear_Game()

        // Подписка на события
        ctx.eventBus.on("dragstart", this.on_dragstart)
        ctx.eventBus.on("drag", this.on_drag)
        ctx.eventBus.on("dragend", this.on_dragend)
        ctx.eventBus.on("change:goal.value", this.onChange_goalValue)
        ctx.eventBus.on("showHint", this.onShowHint)
        //
        ctx.eventBus.on("animation-stop", this.onAnimationStop)
        //
        ctx.eventBus.on("*", this.onEvent)

        // Менеджер анимации
        animation.init()

        //
        daoApi.onAfterInvokeError = this.onAfterInvokeError
    },

    shutdown() {
        // Подписка на события
        ctx.eventBus.off("dragstart", this.on_dragstart)
        ctx.eventBus.off("drag", this.on_drag)
        ctx.eventBus.off("dragend", this.on_dragend)
        ctx.eventBus.off("change:goal.value", this.onChange_goalValue)
        ctx.eventBus.off("showHint", this.onShowHint)
        //
        ctx.eventBus.off("animation-stop", this.onAnimationStop)
        //
        ctx.eventBus.off("*", this.onEvent)
        //
        ctx.gameplay = null
    },

    //
    onAfterInvokeError(res) {
        let ERROR_CODE = {
            USER_NOT_SET: "RGM_USER_NOT_SET"
        }

        //
        let message = res?.error?.message

        //
        if (message && message.includes(ERROR_CODE.USER_NOT_SET)) {
            apx.showFrame({
                frame: '/login'
            })

            // Сами обработали ошибку
            return true
        }

        // Не обработали ошибку
        return false
    },


    /**
     * Грузим новое задание с сервера
     */
    async loadNextTask() {
        if (!auth.isAuth() || !ctx.globalState.game.id) {
            return
        }

        // Если не отправляли ответ - отправим
        if (ctx.globalState.gameTask && !ctx.globalState.dataState.mode.postTaskAnswerDone) {
            await ctx.gameplay.api_postTaskAnswer(ctx.globalState.gameTask.task.id, {wasSkip: true})
        }

        // Текущее задание - скрываем
        ctx.globalState.gameTask = null

        // Грузим новое задание с сервера
        let dataGameTask = await ctx.gameplay.api_choiceTask()

        // Задание в глобальном контексте
        this.globalUse_GameTask(dataGameTask)
    },

    /**
     * Грузим текущее задание с сервера
     */
    async loadCurrentTask() {
        // Задание и раунд в глобальном контексте
        if (!ctx.globalState.gameTask) {
            // Грузим текущее задание с сервера
            let resGameTask = await ctx.gameplay.api_getCurrentTask(ctx.globalState.game.id)

            // Задание в глобальном контексте
            this.globalUse_GameTask(resGameTask)
        }
    },

    async loadCurrentOrNextTask() {
        // Грузим текущее задание с сервера
        if (!ctx.globalState.gameTask) {
            await ctx.gameplay.loadCurrentTask()
        }

        // Грузим следующее задание с сервера
        if (!ctx.globalState.gameTask) {
            await ctx.gameplay.loadNextTask()
        }
    },

    async gameStart(planId) {
        let resGame = await ctx.gameplay.api_gameStart(planId)

        // Игра в глобальном контексте
        ctx.gameplay.globalUse_Game(resGame)

        // Грузим задание с сервера
        await ctx.gameplay.loadNextTask()
    },

    async loadActiveGame() {
        let resGame = await ctx.gameplay.api_getActiveGame()

        // Игра в глобальном контексте
        if (resGame && resGame.game) {
            ctx.gameplay.globalUse_Game(resGame)
        } else {
            ctx.gameplay.globalClear_Game()
        }

        return resGame
    },

    async loadLastGame() {
        let resGame = await ctx.gameplay.api_getLastGame()

        // Игра в глобальном контексте
        if (resGame && resGame.game) {
            ctx.gameplay.globalUse_Game(resGame)
        } else {
            ctx.gameplay.globalClear_Game()
        }

        //
        return resGame
    },

    async closeActiveGame() {
        await ctx.gameplay.api_closeActiveGame()
        ctx.gameplay.globalClear_Game()
    },

    globalUse_GameTask(resGameTask) {
        if (resGameTask.task) {
            // Каждому варианту ответа проставляем id задания - нужно в интерфейсе
            for (let i = 0; i < resGameTask.taskOptions.length; i++) {
                let taskOption = resGameTask.taskOptions[i]
                taskOption.task = resGameTask.task.id
            }

            // Перемешаем ответы
            resGameTask.taskOptions = ctx.gameplay.shuffleTaskOptions(resGameTask.taskOptions)

            // Текущее задание
            ctx.globalState.gameTask = {}
            ctx.globalState.gameTask.task = resGameTask.task
            ctx.globalState.gameTask.taskOptions = resGameTask.taskOptions

            // Состояние цели
            ctx.gameplay.resetGoal(resGameTask.task.text)
        } else {
            // Текущее задание - пустое
            ctx.globalState.gameTask = null

            // Состояние цели
            ctx.gameplay.resetGoal(null)
        }

        // Состояние раунда в глобальный контекст
        ctx.gameplay.globalUse_Game(resGameTask)

        // Уведомим об изменении задания
        ctx.eventBus.emit("loadedGameTask", resGameTask)

        // Разные умолчания
        ctx.globalState.dataState.mode.modeShowOptions = null
        ctx.globalState.dataState.mode.postTaskAnswerDone = false
    },

    globalClear_Game() {
        ctx.globalState.game = null
        ctx.globalState.gameTask = null
        ctx.globalState.tasksResult = null
    },

    globalUse_Game(resGameTask) {
        ctx.globalState.game = resGameTask.game
        ctx.globalState.tasksResult = resGameTask.tasksResult
    },

    async api_saveUsrFact(factQuestion, factAnswer, item) {
        let resApi = await daoApi.loadStore('m/Task/saveUsrFact',
            [factQuestion, factAnswer, {
                isHidden: item.isHidden, isKnownGood: item.isKnownBad
            }],
            {waitShow: false}
        )
        //return ctx.gameplay.parseResApiGame(resApi)
    },

    async api_saveUsrFacts(usrFacts, planId) {
        await daoApi.invoke('m/Task/saveUsrFacts', [usrFacts, planId], {waitShow: false})
    },

    async api_gameStart(planId) {
        let resApi = await daoApi.loadStore('m/Game/gameStart', [planId])
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

    async api_getGame(game) {
        let resApi = await daoApi.loadStore('m/Game/getGame', [game])
        return ctx.gameplay.parseResApiGame(resApi)
    },

    async api_closeActiveGame() {
        await daoApi.invoke('m/Game/closeActiveGame', [])
    },

    async api_getPlansVisible() {
        let resApi = await daoApi.loadStore(
            "m/Plan/getPlansVisible", []
        )

        return resApi.records
    },

    async api_getPlansPublic() {
        let resApi = await daoApi.loadStore(
            "m/Plan/getPlansPublic", []
        )

        return resApi.records
    },

    async api_getPlanTasks(plan) {
        let resApi = await daoApi.loadStore(
            'm/Game/getPlanTasks', [plan]
        )

        let res = {
            plan: resApi.plan.records[0],
            tasks: resApi.tasks.records,
            statistic: resApi.statistic,
            statisticPeriod: resApi.statisticPeriod.records,
        }

        return res
    },

    /**
     * Выбрать следующее задание
     */
    async api_choiceTask() {
        let resApi = await daoApi.loadStore('m/Game/choiceTask', [ctx.globalState.game.id])
        return ctx.gameplay.parseResApiTask(resApi)
    },

    /**
     * Загрузить текущее задание
     */
    async api_getCurrentTask() {
        let resApi = await daoApi.loadStore('m/Game/currentTask', [ctx.globalState.game.id])
        return ctx.gameplay.parseResApiTask(resApi)
    },

    parseResApiGame(resApi) {
        let res = {}

        //
        if (resApi.game) {
            res.game = resApi.game.records[0]
            if (res.game.dend) {
                res.game.done = true
            }
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

        // Если пришел расширенный ответ - со списком с подробными заданиями и ответами
        if (resApi.tasks) {
            res.tasks = resApi.tasks.records
        } else {
            res.tasks = null
        }

        //
        return res
    },

    parseResApiTask(resApi) {
        let res

        //
        let resGame = ctx.gameplay.parseResApiGame(resApi)

        //
        if (resApi.task) {
            res = {
                task: resApi.task.records[0],
                taskOptions: resApi.taskOption.records,
                game: resGame.game,
                tasksResult: resGame.tasksResult,
                statistic: resGame.statistic,
            }
        } else {
            res = {
                task: null,
                taskOptions: null,
                game: resGame.game,
                tasksResult: resGame.tasksResult,
                statistic: resGame.statistic,
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

    async api_getCurrentUser() {
        let res = await daoApi.invoke("m/Usr/getCurrentUserInfo", [])

        //
        return res
    },

    async api_login(login, password) {
        let res = await apx.ajax.request({
            url: "auth/login",
            params: {login: login, password: password},
        })

        //
        this.setAuthUserInfo(res.data)
    },

    async api_loginContextUser(usrId) {
        let res = await apx.ajax.request({
            url: "auth/loginContextUser",
            params: {usr: usrId},
        })

        //
        this.setContextUserInfo(res.data)
    },

    async api_updUsr(userInfo) {
        let res = await ajax.request({
            url: "auth/updUsr",
            params: {
                text: userInfo.text, login: userInfo.login, password: userInfo.password
            },
        })

        //
        this.setAuthUserInfo(res.data)
    },

    async api_register(text, login, password) {
        let res = await apx.ajax.request({
            url: "auth/register",
            params: {text: text, login: login, password: password},
        })

        //
        this.setAuthUserInfo(res.data)
    },

    async api_logout() {
        let res = await apx.ajax.request({
            url: "auth/logout",
        })

        //
        this.clearAuthUserInfo()

        // Задание и раунд в глобальном контексте
        ctx.gameplay.globalClear_Game()
    },

    async api_logoutContextUser(login, password) {
        let res = await apx.ajax.request({
            url: "auth/logoutContextUser",
        })

        //
        this.clearContextUserInfo()
    },


    /**
     * Записывает данные пользователя data (например, пришедшие от ajax-запроса авторизации auth/login)
     * в переменную apx.cfg.userInfo (которая возвращается фунцией auth.getUserInfo).
     *
     * Это дает состояние залогиненности в приложение.
     * Избавляет от необходимости перезагружать страницу, чтобы получить состояние залогинености
     * (при перезагрузке страницы переменная apx.cfg.userInfo заполняется автоматически).
     *
     * @param data данные пользователя
     */
    setAuthUserInfo(data) {
        let userInfo = auth.getUserInfo()
        userInfo.id = data.id
        userInfo.login = data.login
        userInfo.text = data.text
        userInfo.guest = data.guest
        userInfo.color = data.color
        userInfo.planDefault = data.planDefault
        userInfo.linksToWait = data.linksToWait
        userInfo.linksDependent = data.linksDependent

        //
        this.clearContextUserInfo()
    },

    setContextUserInfo(data) {
        let contextUser = {}
        contextUser.id = data.id
        contextUser.login = data.login
        contextUser.text = data.text
        contextUser.guest = data.guest
        contextUser.color = data.color
        contextUser.planDefault = data.planDefault
        //
        let userInfo = auth.getUserInfo()
        userInfo.contextUser = contextUser
    },

    /**
     * Стирает данные пользователя из переменной переменную apx.cfg.userInfo.
     *
     * Это дает состояние НЕ залогиненности.
     */
    clearAuthUserInfo() {
        let userInfo = auth.getUserInfo()
        userInfo.id = null
        userInfo.login = null
        userInfo.text = null
        userInfo.guest = null
        userInfo.color = null
        userInfo.planDefault = null
        userInfo.linksToWait = []
        userInfo.linksDependent = []

        //
        this.clearContextUserInfo()
    },

    clearContextUserInfo() {
        let userInfo = auth.getUserInfo()
        userInfo.contextUser = null
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
        let stateBall = ctx.globalState.dataState.ball

        //
        stateDrag.dtStart = new Date()
        stateDrag.sx = eventDrag.x
        stateDrag.sy = eventDrag.y
        stateDrag.x = eventDrag.x
        stateDrag.y = eventDrag.y

        //
        ctx.animation.animationStop("BallAnimation")

        // Выбрали правильный ответ? Покрасим мяч
        let answerIsTrue = ctx.gameplay.isOptionIsTrueAnswer(eventDrag.taskOption)
        stateBall.ballIsTrue = answerIsTrue
        // Начинаем таскать мяч размером 1 там, где палец/мышка
        stateBall.value = 1
        stateBall.text = taskOption.text
        stateBall.x = stateDrag.x
        stateBall.y = stateDrag.y
    },

    on_drag(eventDrag) {
        let stateDrag = ctx.globalState.dataState.drag
        let stateBall = ctx.globalState.dataState.ball

        //
        stateDrag.x = eventDrag.x
        stateDrag.y = eventDrag.y

        // Продолжаем таскать мяч размером 1 там, где палец/мышка
        stateBall.x = eventDrag.x
        stateBall.y = eventDrag.y
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


        // Выбрали ответ - отреагируем
        if (ctx.gameplay.isOptionIsTrueAnswer(eventDrag.taskOption)) {
            // Выбрали правильный ответ
            stateMode.modeShowOptions = "hint-true"
        } else {
            // Выбрали не правильный ответ
            stateBall.text = ""
            stateGoal.valueGoal = stateGoal.valueGoal + 1
            if (stateGoal.valueGoal > ctx.settings.valueGoalMax) {
                stateGoal.valueGoal = ctx.settings.valueGoalMax
            }
            // Покажем подсказки
            stateMode.modeShowOptions = "hint-true"
            stateMode.goalHitSize = ctx.settings.goalHitSizeError
        }

        // Показать текст подсказки после первого выбора
        ctx.globalState.dataState.showTaskHint = true

        // Показать текст подсказки после первого выбора
        ctx.globalState.dataState.showTaskHint = true

        //
        ctx.eventBus.emit("taskOptionSelected", eventDrag.taskOption)


        // Запустим анимацию "BallAnimation"
        let dtEnd = new Date()
        let duration = Math.abs(dtEnd - stateDrag.dtStart)
        //
        let answerIsTrue = ctx.gameplay.isOptionIsTrueAnswer(eventDrag.taskOption)
        //
        let elGoal = document.getElementById("goal")
        let rectGoal = utilsCore.getElRect(elGoal)
        //
        let cfg = {
            ballIsTrue: answerIsTrue,
            x: eventDrag.x,
            y: eventDrag.y,
            sx: stateDrag.sx,
            sy: stateDrag.sy,
            duration: duration,
            rectGoal: rectGoal,
        }
        //
        ctx.animation.animationRestart("BallAnimation", stateBall, cfg)
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

    onAnimationStop(animationInfo) {
        let stateBall = ctx.globalState.dataState.ball
        let stateGoal = ctx.globalState.dataState.goal
        let stateMode = ctx.globalState.dataState.mode
        let animationName = animationInfo.animation
        let animationData = animationInfo.data

        if (animationName === "BallAnimation") {

            // Попали в цель?
            if (animationData.result.goal && ctx.globalState.dataState.ball.ballIsTrue) {
                // Счетчик попаданий
                stateGoal.valueDone = stateGoal.valueDone + stateMode.goalHitSize

                // Покажем взрыв
                let cfg = {
                    x: stateBall.x,
                    y: stateBall.y,
                    afterStop: function() {
                        ctx.eventBus.emit("change:goal.value", stateGoal.value)

                        // Если события "change:goal.value" осталось загруженное
                        // задание ctx.globalState.gameTask - то можно перемешать.
                        // Оно остается старое, если не загружена новое, а это бывает
                        // при тренировочных стрельбах после ошибки.
                        if (ctx.globalState.gameTask) {
                            // Перемешаем ответы
                            ctx.globalState.gameTask.taskOptions = ctx.gameplay.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

                            // Снова выбираем сами, без подсказки
                            stateMode.modeShowOptions = null
                        }
                    }
                }
                ctx.animation.animationRestart("SpriteAnimation", ctx.globalState.sprite.blow, cfg)

            } else {
                // Покажем паузу с правильными ответами
                let cfg = {
                    duration: 3000,
                    afterStop: function() {
                        // Перемешаем ответы
                        ctx.globalState.gameTask.taskOptions = ctx.gameplay.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

                        // Снова выбираем сами, без подсказки
                        stateMode.modeShowOptions = null
                    }
                }
                ctx.animation.animationRestart("TimerAnimation", null, cfg)
            }

        }
    },


}
