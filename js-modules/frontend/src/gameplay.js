import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import utilsCore from "./utils2D"
import {daoApi} from "./dao"

import testData from "./gameplayTestData"

export default {

    //testData: true,

    settings: {
        animationInterval: 10,
        ballWidth: 32,
        ballHeihth: 32,
        goalSize: 16,
        minDl: 100,
        maxDl: 1000,
        valueGoalMax: 10,
        goalHitSizeDefault: 5,
        goalHitSizeHint: 2,
        goalHitSizeError: 1,
    },

    init(gameTask, state, user, game) {
        // Инициализация контекста
        ctx.th = this
        ctx.settings = this.settings
        ctx.gameTask = gameTask
        ctx.state = state
        ctx.user = user
        ctx.game = game
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
        if (this.testData) {
            ctx.state.testData = this.testData;
            ctx.state.testData_taskIdx = -1;
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
        // Если не отправляли ответ - отправим
        if (ctx.gameTask.task && !ctx.state.game.postTaskAnswerDone) {
            ctx.th.api_postTaskAnswer(ctx.gameTask.task.id, {wasSkip: true})
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
        //ctx.gameTask = dataGameTask

        // Уведомим об изменении задания
        ctx.eventBus.emit("loadedGameTask", dataGameTask)

        // Разные умолчания
        ctx.state.game.modeShowOptions = null
        ctx.state.game.postTaskAnswerDone = false

        // Состояние раунда
        ctx.game.id = dataGameTask.game.id
        ctx.game.plan = dataGameTask.game.plan
        ctx.game.text = dataGameTask.game.text
        ctx.game.countTotal = dataGameTask.game.countTotal
        ctx.game.countDone = dataGameTask.game.countDone

        // Состояние цели
        ctx.th.resetGoal(dataGameTask.task.text)
    },

    async gameStart(idPlan) {
        let recGame = await ctx.th.api_gameStart(idPlan)

        // Задиние в глобальном контексте очистим
        ctx.gameTask = {}

        //
        ctx.game.id = recGame.id
        ctx.game.plan = recGame.plan
        ctx.game.text = recGame.text
        ctx.game.countTotal = recGame.countTotal
        ctx.game.countDone = recGame.countDone

        //
        ctx.th.nextTask()
    },

    async api_gameStart(idPlan) {
        let resApi = await daoApi.loadStore('m/Game/gameStart', [idPlan])

        let res = resApi.records[0]

        return res
    },

    async api_choiceTask() {
        if (ctx.state.testData) {
            ctx.state.testData_taskIdx = ctx.state.testData_taskIdx + 1;
            if (ctx.state.testData_taskIdx >= testData.tasks.length) {
                ctx.state.testData_taskIdx = 0;
            }
            //
            let res = testData.tasks[ctx.state.testData_taskIdx]

            //
            res.game = {}
            res.game.text = "test"
            res.game.countTotal = testData.tasks.length
            res.game.countDone = ctx.state.testData_taskIdx

            //
            return res
        }


        //
        let resApi = await daoApi.loadStore('m/Game/choiceTask', [ctx.game.id])

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
        if (ctx.state.testData) {
            return
        }

        //
        if (ctx.state.game.postTaskAnswerDone) {
            return
        }
        ctx.state.game.postTaskAnswerDone = true

        //
        let res = daoApi.loadStore('m/Game/postTaskAnswer', [idGameTask, taskResult])

        //
        return res
    },

    // Сбрасываем состояние результата (цели)
    resetGoal(text) {
        ctx.state.game.goalHitSize = ctx.settings.goalHitSizeDefault
        ctx.state.goal.text = text
        ctx.state.goal.valueGoal = ctx.settings.goalHitSizeDefault
        ctx.state.goal.valueDone = 0
    },


    on_dragstart(eventDrag) {
        //console.info("doDragStart", eventDrag)

        let elBall = document.getElementById("ball")
        let elGoal = document.getElementById("goal")

        let obj = eventDrag.srcElement
        let taskOption = eventDrag.taskOption

        let stateDrag = ctx.state.drag
        let stateGoal = ctx.state.goal
        let stateBall = ctx.state.ball

        stateDrag.dtStart = new Date()
        stateDrag.sx = eventDrag.x
        stateDrag.sy = eventDrag.y
        stateDrag.x = eventDrag.x
        stateDrag.y = eventDrag.y

        //
        clearInterval(stateDrag.interval)

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
        let stateDrag = ctx.state.drag
        let stateGoal = ctx.state.goal
        let stateBall = ctx.state.ball

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

        let stateDrag = ctx.state.drag
        let stateGoal = ctx.state.goal
        let stateBall = ctx.state.ball
        let stateGame = ctx.state.game


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
            ctx.state.ball.ballIsTrue = true
            stateGame.modeShowOptions = null
        } else {
            ctx.state.ball.ballIsTrue = false
            stateBall.text = ""
            stateGoal.valueGoal = stateGoal.valueGoal + 1
            if (stateGoal.valueGoal > ctx.settings.valueGoalMax) {
                stateGoal.valueGoal = ctx.settings.valueGoalMax
            }
            stateGame.modeShowOptions = "hint-true"
            stateGame.goalHitSize = ctx.settings.goalHitSizeError
        }
        stateBall.value = 1

        // Перемешаем ответы
        ctx.gameTask.taskOptions = ctx.th.shuffleTaskOptions(ctx.gameTask.taskOptions)

        // Показать текст подсказки после первого выбора
        ctx.state.alwaysShowText = true

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

    startMoveAnimation(el) {
        let stateDrag = ctx.state.drag

        //
        stateDrag.interval = setInterval(ctx.th.animationStep, ctx.settings.animationInterval)
    },

    animationStep() {
        let elBall = document.getElementById("ball")
        let elGoal = document.getElementById("goal")

        //
        let stateDrag = ctx.state.drag
        let stateGoal = ctx.state.goal
        let stateBall = ctx.state.ball
        let stateGame = ctx.state.game

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
            if (ctx.state.ball.ballIsTrue) {
                stateGoal.valueDone = stateGoal.valueDone + stateGame.goalHitSize
            }

            //
            clearInterval(stateDrag.interval)

            //
            stateBall.value = 0

            //
            ctx.eventBus.emit("change:goal.value", stateGoal.value)

            //
            return
        }

        // Выход шарика за границы экрана
        if (stateDrag.x + ctx.settings.ballWidth > innerWidth || stateDrag.x < 0 || stateDrag.y + ctx.settings.ballHeihth > innerHeight || stateDrag.y < 0) {
            clearInterval(stateDrag.interval)
            stateBall.value = 0
            return
        }

        // Рост или уменьшение по ходу движения
        let framePerSec = 1000 / ctx.settings.animationInterval
        if (!ctx.state.ball.ballIsTrue) {
            stateBall.value = stateBall.value - 1.9 / framePerSec
        } else {
            stateBall.value = stateBall.value + 1.1 / framePerSec
        }

        // Выход размера шарика за ограничение размера
        if (stateBall.value > 5 || stateBall.value < 0) {
            clearInterval(stateDrag.interval)
            stateBall.value = 0

        }

        // Продолжение движения
        stateBall.x = stateDrag.x
        stateBall.y = stateDrag.y
    },


    onChange_goalValue(v) {
        if (ctx.state.goal.valueDone >= ctx.state.goal.valueGoal) {
            ctx.th.nextTask()
        }
    },

    onShowHint(v) {
        if (v) {
            // Показывать подсказки
            ctx.state.game.modeShowOptions = "hint-true"

            // Уменьшим силу удара
            if (ctx.state.game.goalHitSize > ctx.settings.goalHitSizeHint) {
                ctx.state.game.goalHitSize = ctx.settings.goalHitSizeHint
            }

            // Если не отправляли ответ - отправим
            if (!ctx.state.game.postTaskAnswerDone) {
                ctx.th.api_postTaskAnswer(ctx.gameTask.task.id, {wasHint: true})
            }
        }
    },

    onEvent(ev, obj) {
        //console.info("gameplay.event", ev, obj)
        //console.info("state", ctx.state)
    },


}
