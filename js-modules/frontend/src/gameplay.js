import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import utilsCore from "./utils2D"
import {daoApi} from "./dao"

import testData from "./gameplayTestData"

export default {

    //useTestData: true,

    settings: {
        animationInterval: 10,
        ballWidth: 32,
        ballHeihth: 32,
        goalSize: 16,
        minDl: 200,
        maxDl: 1000,
        valueGoalMax: 10,
        goalHitSizeDefault: 5,
        goalHitSizeHint: 2,
        goalHitSizeError: 1,
    },

    init(globalState) {
        // Инициализация контекста
        ctx.th = this
        ctx.settings = this.settings
        ctx.globalState = globalState
        ctx.eventBus = apx.app.eventBus

        // Подписка на события
        ctx.eventBus.on("dragstart", this.on_dragstart)
        ctx.eventBus.on("drag", this.on_drag)
        ctx.eventBus.on("dragend", this.on_dragend)
        ctx.eventBus.on("change:goal.value", this.onChange_goalValue)
        ctx.eventBus.on("showHint", this.onShowHint)
        //
        ctx.eventBus.on("*", this.onEvent)

        // Инициализация состояния тестовых данных
        if (this.useTestData) {
            ctx.useTestData = this.useTestData;
            ctx.testData_taskIdx = -1;
        }
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
    },

    // Новое задание
    async nextTask() {
        if (!ctx.globalState.user.id ||
            !ctx.globalState.game.id) {
            return
        }

        // Если не отправляли ответ - отправим
        if (ctx.globalState.gameTask.task && !ctx.globalState.dataState.mode.postTaskAnswerDone) {
            ctx.th.api_postTaskAnswer(ctx.globalState.gameTask.task.id, {wasSkip: true})
        }

        // Грузим новое задание с сервера
        let dataGameTask = await ctx.th.api_choiceTask()

        // Каждому варианту ответа проставляем id задания - нужно в интерфейсе
        for (let i = 0; i < dataGameTask.taskOptions.length; i++) {
            let taskOption = dataGameTask.taskOptions[i]
            taskOption.task = dataGameTask.task.id
        }

        // Перемешаем ответы
        dataGameTask.taskOptions = ctx.th.shuffleTaskOptions(dataGameTask.taskOptions)

        // Задание в глобальный контекст
        ctx.globalState.gameTask.task = dataGameTask.task
        ctx.globalState.gameTask.taskOptions = dataGameTask.taskOptions

        // Состояние раунда в глобальный контекст
        ctx.globalState.game = dataGameTask.game

        // Уведомим об изменении задания
        ctx.eventBus.emit("loadedGameTask", dataGameTask)

        // Разные умолчания
        ctx.globalState.dataState.mode.modeShowOptions = null
        ctx.globalState.dataState.mode.postTaskAnswerDone = false

        // Состояние цели
        ctx.th.resetGoal(dataGameTask.task.text)
    },

    async gameStart(idPlan) {
        let recGame = await ctx.th.api_gameStart(idPlan)

        // Задание и раунд в глобальном контексте
        ctx.globalState.game = recGame
        ctx.globalState.gameTask = {}

        //
        ctx.th.nextTask()
    },

    async api_gameStart(idPlan) {
        let resApi = await daoApi.loadStore('m/Game/gameStart', [idPlan])

        let res = resApi.records[0]

        return res
    },

    async api_choiceTask() {
        if (ctx.useTestData) {
            ctx.testData_taskIdx = ctx.testData_taskIdx + 1;
            if (ctx.testData_taskIdx >= testData.tasks.length) {
                ctx.testData_taskIdx = 0;
            }
            //
            let res = testData.tasks[ctx.testData_taskIdx]

            //
            res.game = {}
            res.game.text = "test"
            res.game.countTotal = testData.tasks.length
            res.game.countDone = ctx.testData_taskIdx

            //
            return res
        }


        //
        let resApi = await daoApi.loadStore('m/Game/choiceTask', [ctx.globalState.game.id])

        //
        let res = {
            task: resApi.task.records[0],
            taskOptions: resApi.taskOption.records,
            game: resApi.game.records[0],
        }

        //
        return res
    },

    api_postTaskAnswer(idGameTask, taskResult) {
        if (ctx.useTestData) {
            return
        }

        //
        if (ctx.globalState.dataState.mode.postTaskAnswerDone) {
            return
        }
        ctx.globalState.dataState.mode.postTaskAnswerDone = true

        //
        let res = daoApi.loadStore('m/Game/postTaskAnswer', [idGameTask, taskResult])

        //
        return res
    },

    // Сбрасываем состояние результата (цели)
    resetGoal(text) {
        ctx.globalState.dataState.mode.goalHitSize = ctx.settings.goalHitSizeDefault
        ctx.globalState.dataState.goal.text = text
        ctx.globalState.dataState.goal.valueGoal = ctx.settings.goalHitSizeDefault
        ctx.globalState.dataState.goal.valueDone = 0
    },


    on_dragstart(eventDrag) {
        //console.info("doDragStart", eventDrag)

        let elBall = document.getElementById("ball")
        let elGoal = document.getElementById("goal")

        let obj = eventDrag.srcElement
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
        ctx.th.stopMoveAnimation()

        //
        stateBall.value = 1
        stateBall.text = taskOption.text

        //
        stateBall.x = stateDrag.x
        stateBall.y = stateDrag.y
    },

    on_drag(eventDrag) {
        //console.info("doDragMove", eventDrag)

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
        //console.info("doDragEnd", eventDrag)

        let stateDrag = ctx.globalState.dataState.drag
        let stateGoal = ctx.globalState.dataState.goal
        let stateBall = ctx.globalState.dataState.ball
        let stateMode = ctx.globalState.dataState.mode


        // Уведомим сервер
        ctx.th.api_postTaskAnswer(eventDrag.taskOption.task, {
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

        // Выбрали правильный ответ - отреагируем
        if (ctx.th.isOptionIsTrueAnswer(eventDrag.taskOption)) {
            ctx.globalState.dataState.ball.ballIsTrue = true
            //stateMode.modeShowOptions = null
            stateMode.modeShowOptions = "hint-true"
        } else {
            ctx.globalState.dataState.ball.ballIsTrue = false
            stateBall.text = ""
            stateGoal.valueGoal = stateGoal.valueGoal + 1
            if (stateGoal.valueGoal > ctx.settings.valueGoalMax) {
                stateGoal.valueGoal = ctx.settings.valueGoalMax
            }
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
        ctx.th.startMoveAnimation(elBall)
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
        stateDrag.interval = setInterval(ctx.th.animationStep, ctx.settings.animationInterval)
    },

    stopMoveAnimation() {
        let stateDrag = ctx.globalState.dataState.drag

        //
        clearInterval(stateDrag.interval)
    },

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
            ctx.th.stopMoveAnimation()

            //
            stateBall.value = 0

            //
            ctx.eventBus.emit("change:goal.value", stateGoal.value)

            // Перемешаем ответы, если цель не поражена
            if (!ctx.th.goalDone()) {
                ctx.globalState.gameTask.taskOptions = ctx.th.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

                // Снова выбираем
                stateMode.modeShowOptions = null
            }

            //
            return
        }

        // Выход шарика за границы экрана
        if (stateDrag.x + ctx.settings.ballWidth > innerWidth || stateDrag.x < 0 || stateDrag.y + ctx.settings.ballHeihth > innerHeight || stateDrag.y < 0) {
            //
            ctx.th.stopMoveAnimation()

            //
            stateBall.value = 0

            // Перемешаем ответы
            ctx.globalState.gameTask.taskOptions = ctx.th.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

            // Снова выбираем
            stateMode.modeShowOptions = null

            //
            return
        }

        // Рост или уменьшение по ходу движения
        let framePerSec = 1000 / ctx.settings.animationInterval
        if (!ctx.globalState.dataState.ball.ballIsTrue) {
            stateBall.value = stateBall.value - 1.9 / framePerSec
        } else {
            stateBall.value = stateBall.value + 1.1 / framePerSec
        }

        // Выход размера шарика за ограничение размера
        if (stateBall.value > 5 || stateBall.value < 0) {
            //
            ctx.th.stopMoveAnimation()

            //
            stateBall.value = 0

            // Перемешаем ответы
            ctx.globalState.gameTask.taskOptions = ctx.th.shuffleTaskOptions(ctx.globalState.gameTask.taskOptions)

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
        if (ctx.th.goalDone()) {
            ctx.th.nextTask()
        }
    },

    onShowHint(v) {
        if (v) {
            // Показывать подсказки
            ctx.globalState.dataState.mode.modeShowOptions = "hint-true"

            // Уменьшим силу удара
            if (ctx.globalState.dataState.mode.goalHitSize > ctx.settings.goalHitSizeHint) {
                ctx.globalState.dataState.mode.goalHitSize = ctx.settings.goalHitSizeHint
            }

            // Если не отправляли ответ - отправим
            if (!ctx.globalState.dataState.mode.postTaskAnswerDone) {
                ctx.th.api_postTaskAnswer(ctx.globalState.gameTask.task.id, {wasHint: true})
            }
        }
    },

    onEvent(ev, obj) {
        //console.info("gameplay.event", ev, obj)
        //console.info("state", ctx.state)
    },


}
