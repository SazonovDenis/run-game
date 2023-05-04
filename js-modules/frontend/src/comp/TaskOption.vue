<template>
    <div class="option">

        <div v-if="this.taskOption.sound">
            <q-btn color="blue" text-color="black" label="Play" @click="play"/>
        </div>

        <div v-bind:id="'taskOption-'+taskOption.id"
             v-on:mousedown="onMouseDown"

             v-on:touchstart="onTouchStart"
             v-on:touchmove="onTouchMove"
             v-on:touchend="onTouchEnd"
             v-on:touchcancel="onTouchCancel"
             v-on:click="xxx"
        >
            {{ taskOption.text }}
        </div>
    </div>
</template>

<script>

import utilsCore from "../utils2D"
import {apx} from '../vendor'

export default {
    components: {
        apx
    },

    props: {
        taskOption: {},

        animationInterval: {
            default: 10,
            type: Number
        },
        ballWidth: {
            default: 32,
            type: Number
        },
        ballHeihth: {
            default: 32,
            type: Number
        },
        goalSize: {
            default: 16,
            type: Number
        },
        minDl: {
            default: 100,
            type: Number
        },
        maxDl: {
            default: 1000,
            type: Number
        },
        state: {},
    },

    data() {
        return {}
    },

    methods: {
        onMouseDown(event) {
            //console.info("onMouseDown", event)

            //
            let stateDrag = this.state.drag
            let stateGoal = this.state.goal

            //
            stateDrag.drag = true

            //
            document.addEventListener('mousemove', this.onMouseMove)
            document.addEventListener('mouseup', this.onMouseUp)

            //
            let eventDrag = {}
            eventDrag.x = event.pageX
            eventDrag.y = event.pageY
            eventDrag.srcElement = event.srcElement

            //
            this.doDragStart(eventDrag)
        },

        onMouseMove(event) {
            //console.info("onMouseMove", event)

            let stateDrag = this.state.drag
            let stateGoal = this.state.goal

            //
            if (stateDrag.drag) {
                //
                let eventDrag = {}
                eventDrag.x = event.pageX
                eventDrag.y = event.pageY
                eventDrag.srcElement = event.srcElement

                //
                this.doDragMove(eventDrag)
            }
        },

        onMouseUp(event) {
            document.removeEventListener('mousemove', this.onMouseMove)
            document.removeEventListener('mouseup', this.onMouseUp)

            //
            let stateDrag = this.state.drag
            let stateGoal = this.state.goal

            //
            if (stateDrag.drag) {
                stateDrag.drag = false

                //
                let eventDrag = {}
                eventDrag.x = event.pageX
                eventDrag.y = event.pageY
                eventDrag.srcElement = event.srcElement

                //
                this.doDragStop(eventDrag)
            }
        },


        doDragStart(eventDrag) {
            //console.info("doDragStart", eventDrag)

            let elBall = document.getElementById("ball")
            let elGoal = document.getElementById("goal")

            let obj = eventDrag.srcElement
            let taskOption = this.taskOption

            let stateDrag = this.state.drag
            let stateGoal = this.state.goal
            let stateBall = this.state.ball

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

        doDragMove(eventDrag) {
            //console.info("doDragMove", eventDrag)

            //
            let stateDrag = this.state.drag
            let stateGoal = this.state.goal
            let stateBall = this.state.ball

            //
            let elBall = document.getElementById("ball")

            //
            stateDrag.x = eventDrag.x
            stateDrag.y = eventDrag.y

            //
            stateBall.x = stateDrag.x
            stateBall.y = stateDrag.y
        },

        doDragStop(eventDrag) {
            //console.info("doDragStop", eventDrag)

            let stateDrag = this.state.drag
            let stateGoal = this.state.goal
            let stateBall = this.state.ball
            let stateGame = this.state.game

            //
            let elBall = document.getElementById("ball")
            let goal = document.getElementById("goal")

            //
            stateDrag.x = eventDrag.x
            stateDrag.y = eventDrag.y
            stateDrag.dtStop = new Date()
            stateDrag.duration = Math.abs(stateDrag.dtStop - stateDrag.dtStart)

            // Скорость броска, пкс/сек
            let dx = 1000 * (stateDrag.x - stateDrag.sx) / stateDrag.duration
            let dy = 1000 * (stateDrag.y - stateDrag.sy) / stateDrag.duration

            // Чтобы  летело не слишком...
            let dl = Math.sqrt(dx * dx + dy * dy)
            // ... не слишком медленно...
            if (dl < this.minDl) {
                dx = dx * this.minDl / dl
                dy = dy * this.minDl / dl
            }
            // ... и не слишком быстро
            if (dl > this.maxDl) {
                dx = dx * this.maxDl / dl
                dy = dy * this.maxDl / dl
            }

            // Учтем количество кадров в секунду
            let framePerSec = 1000 / this.animationInterval
            stateDrag.dx = dx / framePerSec
            stateDrag.dy = dy / framePerSec

            // Выбрали правильный ответ - отреагируем
            if (this.isOptionIsTrueAnswer(this.taskOption)) {
                this.ballIsTrue = true
                stateGame.modeShowOptions = null
            } else {
                this.ballIsTrue = false
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
            this.startMoveAnimation(elBall, stateDrag.dx, stateDrag.dy)
        },

        startMoveAnimation(el, dx, dy) {
            let elBall = document.getElementById("ball")
            let elGoal = document.getElementById("goal")

            let stateDrag = this.state.drag
            let stateGoal = this.state.goal
            let stateBall = this.state.ball
            let stateGame = this.state.game

            //
            stateDrag.interval = setInterval(() => {
                // Шаг
                stateDrag.x = stateDrag.x + dx
                stateDrag.y = stateDrag.y + dy

                // Пересечения определяем как пересечение отрезка, проведенного
                // от предыдущей точки движения до текущей с прямоугольником цели.
                // Так мы не дадим "проскочить" снаряду сквозь цель
                // при слишком большой скорости движения.
                let rectTrace = {
                    x1: stateDrag.x,
                    x2: stateDrag.x - dx,
                    y1: stateDrag.y,
                    y2: stateDrag.y - dy,
                }
                let rectGoal = utilsCore.getElRect(elGoal)
                //
                if (utilsCore.intersectRect(rectTrace, rectGoal)) {
                    if (this.ballIsTrue) {
                        stateGoal.value = stateGoal.value - stateGame.goalHitSize
                    }

                    //
                    clearInterval(stateDrag.interval)

                    //
                    stateBall.value = 0
                    //elBall.style.display = "none"

                    //
                    apx.app.eventBus.emit("changeGoalValue", stateGoal.value)

                    //
                    return
                }

                // Выход шарика за границы экрана
                if (stateDrag.x + this.ballWidth > innerWidth || stateDrag.x < 0 || stateDrag.y + this.ballHeihth > innerHeight || stateDrag.y < 0) {
                    clearInterval(stateDrag.interval)
                    stateBall.value = 0
                    return
                }

                // Рост или уменьшение по ходу движения
                let framePerSec = 1000 / this.animationInterval
                if (!this.ballIsTrue) {
                    stateBall.value = stateBall.value - 1.9 / framePerSec
                } else {
                    stateBall.value = stateBall.value + 2.1 / framePerSec
                }

                // Выход размера шарика за ограничение размера
                if (stateBall.value > 5 || stateBall.value < 0) {
                    clearInterval(stateDrag.interval)
                    stateBall.value = 0

                }

                // Продолжение движения
                stateBall.x = stateDrag.x
                stateBall.y = stateDrag.y
            }, this.animationInterval)
        },

        onTouchStart(event) {
            let touch = event.touches[event.touches.length - 1]

            //
            let stateDrag = this.state.drag
            let stateGoal = this.state.goal

            //
            stateDrag.drag = true

            //
            let eventDrag = {}
            eventDrag.x = touch.pageX
            eventDrag.y = touch.pageY
            eventDrag.srcElement = event.srcElement

            //
            this.doDragStart(eventDrag)
        },

        onTouchMove(event) {
            let touch = event.touches[event.touches.length - 1]

            //
            let eventDrag = {}
            eventDrag.x = touch.pageX
            eventDrag.y = touch.pageY
            eventDrag.srcElement = event.srcElement

            //
            this.doDragMove(eventDrag)
        },

        onTouchEnd(event) {
            //console.info("onTouchCancel", event)

            //
            let stateDrag = this.state.drag
            let stateGoal = this.state.goal

            //
            let eventDrag = {}
            if (event.touches.length > 0) {
                let touch = event.touches[event.touches.length - 1]
                eventDrag.x = touch.pageX
                eventDrag.y = touch.pageY
                eventDrag.srcElement = event.srcElement
            } else {
                eventDrag.x = stateDrag.x
                eventDrag.y = stateDrag.y
                eventDrag.srcElement = event.srcElement
            }

            //
            this.doDragStop(eventDrag)
        },

        onTouchCancel(event) {
            //console.info("onTouchCancel", event)
            this.onTouchEnd(event)
        },

        isOptionIsTrueAnswer(taskOption) {
            if (taskOption.trueFact) {
                return true
            } else {
                return false
            }
        },

        // передвинуть под координаты x, y
        // и сдвинуть на половину ширины/высоты для центрирования
        moveElementTo(el, x, y) {
            el.style.left = x - el.offsetWidth / 2 + 'px'
            el.style.top = y - el.offsetHeight / 2 + 'px'
        },

        play() {
            if (this.taskOption.sound) {
                this.audio.play()
            }
        },

    },

    mounted() {
        let audio = this.audio = new Audio()
        if (this.taskOption.sound) {
            audio.src = this.taskOption.sound
        }
    },


    computed: {}
}
</script>


<style>

.option {
    user-select: none;
    max-width: 20em;
    margin: 5px;
    padding: 10px;
    border-radius: 5px;
    background-color: #fff5da;
}

</style>

