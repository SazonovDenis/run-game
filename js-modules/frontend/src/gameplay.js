import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import utilsCore from "./utils2D"
import testData from "./gameplayTestData"
import {daoApi} from "./dao"

export default {

    settings: {
        animationInterval: 10,
        ballWidth: 32,
        ballHeihth: 32,
        goalSize: 16,
        minDl: 100,
        maxDl: 1000,
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
        ctx.eventBus.on("changeGoalValue", this.on_changeGoalValue)
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
        ctx.th.resetGoal()
        ctx.th.setGoalInfo(dataTask.task.text)
    },

    api_choiceTask() {
        let res = daoApi.loadStore('m/Game/choiceTask', [1001])

        ctx.state.taskIdx = ctx.state.taskIdx + 1;
        if (ctx.state.taskIdx >= testData.tasks.length) {
            ctx.state.taskIdx = 0;
        }
        //
        return testData.tasks[ctx.state.taskIdx]
    },

    setGoalInfo(text) {
        ctx.state.goal.text = "Hit here #" + (ctx.state.taskIdx + 1) + ", " + text
    },

    // Сбрасываем состояние результата (цели)
    resetGoal() {
        ctx.state.game.goalHitSize = 4
        ctx.state.goal.value = 4
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
            stateGoal.value = stateGoal.value + 1
            if (stateGoal.value > 10) {
                stateGoal.value = 10
            }
            stateGame.modeShowOptions = "hint-true"
            stateGame.goalHitSize = 1
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
                stateGoal.value = stateGoal.value - stateGame.goalHitSize
            }

            //
            clearInterval(stateDrag.interval)

            //
            stateBall.value = 0

            //
            ctx.eventBus.emit("changeGoalValue", stateGoal.value)

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


    on_changeGoalValue(ev, obj) {
        if (ctx.state.goal.value == 0) {
            ctx.th.nextTask()
        }
    },

    onEvent(ev, obj) {
        //console.info("gameplay.event", ev, obj)
        //console.info("state", ctx.state)
    },


}
