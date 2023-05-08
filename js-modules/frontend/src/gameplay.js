import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import utilsCore from "./utils2D"
import {daoApi} from "./dao"

export default {

    settings: {
        animationInterval: 10,
        ballWidth: 32,
        ballHeihth: 32,
        goalSize: 16,
        minDl: 100,
        maxDl: 1000,
        valueGoalMax: 10,
        goalHitSizeDefault: 4,
        goalHitSizeHint: 2,
        goalHitSizeError: 1,
    },

    init(state) {
        // Инициализация контекста
        ctx.th = this
        ctx.settings = this.settings
        ctx.state = state
        ctx.eventBus = apx.app.eventBus

        // Подписка на события
        ctx.eventBus.on("dragstart", this.on_dragstart)
        ctx.eventBus.on("drag", this.on_drag)
        ctx.eventBus.on("dragend", this.on_dragend)
        ctx.eventBus.on("change:goal.value", this.onChange_goalValue)
        ctx.eventBus.on("showHint", this.onShowHint)
        //
        ctx.eventBus.on("*", this.onEvent)

        // Инициализация состояния
        ctx.state.taskIdx = -1;
    },

    // Новое задание
    async nextTask() {
        // Грузим новое задание с сервера
        let dataTask = await ctx.th.api_choiceTask()

        // Уведомим об изменении задания
        ctx.eventBus.emit("loadedUsrTask", dataTask)

        // Разные умолчания
        ctx.state.game.modeShowOptions = null
        // Новая цель
        ctx.th.resetGoal(dataTask.task.text)
    },

    async api_choiceTask() {
        let resApi = await daoApi.loadStore('m/Game/choiceTask', [9999])

        //
        let task = resApi.task.records[0]
        let taskOptions = resApi.taskOption.records

        let res = {
            task,
            taskOptions,
        }

        // Чтобы можно было получить код задания по одному ответу - нужно в интерфейсе
        for (let i = 0; i < taskOptions.length; i++) {
            let taskOption = taskOptions[i]
            taskOption.task = task.id
        }

        //
        //console.info("task", task)
        //console.info("taskOptions", taskOptions)
        //console.info("res", res)

        //
        /*
                ctx.state.taskIdx = ctx.state.taskIdx + 1;
                if (ctx.state.taskIdx >= testData.tasks.length) {
                    ctx.state.taskIdx = 0;
                }
                //
                return testData.tasks[ctx.state.taskIdx]
        */

        //
        return res
    },

    api_postTaskAnswer(idUsrTask, idTaskOption) {
        let res = daoApi.loadStore('m/Game/postTaskAnswer', [idUsrTask, idTaskOption])

        return res
    },

    // Сбрасываем состояние результата (цели)
    resetGoal(text) {
        ctx.state.game.goalHitSize = ctx.settings.goalHitSizeDefault
        ctx.state.goal.text = text
        ctx.state.goal.valueGoal = 4
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
        console.info("obj.id: ", obj.id)

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
        ctx.th.api_postTaskAnswer(eventDrag.taskOption.task, eventDrag.taskOption.id)

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

        //
        ctx.th.startMoveAnimation(elBall)
    },

    isOptionIsTrueAnswer(taskOption) {
        if (taskOption.trueFact) {
            return true
        } else {
            return false
        }
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
            ctx.state.game.modeShowOptions = "hint-true"
            ctx.state.game.goalHitSize = ctx.settings.goalHitSizeHint
        }
    },

    onEvent(ev, obj) {
        //console.info("gameplay.event", ev, obj)
        //console.info("state", ctx.state)
    },


}
