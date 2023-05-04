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
            default: 2,
            type: Number
        },
        maxDl: {
            default: 6,
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
            let stateGrag = this.state.drag
            let stateGoal = this.state.goal

            //
            stateGrag.drag = true

            //
            document.addEventListener('mousemove', this.onMouseMove)
            document.addEventListener('mouseup', this.onMouseUp)

            //
            let ev = {}
            ev.pageX = event.clientX
            ev.pageY = event.clientY
            ev.srcElement = event.srcElement

            //
            this.doDragStart(ev)
        },

        onMouseMove(event) {
            //console.info("onMouseMove", event)

            let stateGrag = this.state.drag
            let stateGoal = this.state.goal

            //
            if (stateGrag.drag) {
                //
                let ev = {}
                ev.pageX = event.clientX
                ev.pageY = event.clientY
                ev.srcElement = event.srcElement

                //
                this.doDragMove(ev)
            }
        },

        onMouseUp(event) {
            document.removeEventListener('mousemove', this.onMouseMove)
            document.removeEventListener('mouseup', this.onMouseUp)

            //
            let stateGrag = this.state.drag
            let stateGoal = this.state.goal

            //
            if (stateGrag.drag) {
                stateGrag.drag = false

                //
                let ev = {}
                ev.pageX = event.clientX
                ev.pageY = event.clientY
                ev.srcElement = event.srcElement

                //
                this.doDragStop(ev)
            }
        },


        doDragStart(ev) {
            //console.info("doDragStart", ev)

            let elBall = document.getElementById("ball")
            let elGoal = document.getElementById("goal")

            let obj = ev.srcElement
            let taskOption = this.taskOption

            let stateGrag = this.state.drag
            let stateGoal = this.state.goal
            let stateBall = this.state.ball

            stateGrag.dtStart = new Date()
            stateGrag.sx = ev.pageX
            stateGrag.sy = ev.pageY
            stateGrag.x = ev.pageX
            stateGrag.y = ev.pageY

            //console.info("obj: ", obj)
            console.info("obj.id: ", obj.id)

            //
            clearInterval(stateGrag.interval)

            //
            stateBall.value = 1
            stateBall.text = taskOption.text
            //elBall.style.display = 'block'
            //elBall.innerText = taskOption.text

            //
            this.moveElementTo(elBall, stateGrag.x, stateGrag.y)
        },

        doDragMove(ev) {
            //console.info("doDragMove", ev)

            //
            let stateGrag = this.state.drag
            let stateGoal = this.state.goal

            //
            let elBall = document.getElementById("ball")

            //
            stateGrag.x = ev.pageX
            stateGrag.y = ev.pageY

            //
            this.moveElementTo(elBall, stateGrag.x, stateGrag.y)
        },

        doDragStop(ev) {
            //console.info("doDragStop", ev)

            let stateGrag = this.state.drag
            let stateGoal = this.state.goal
            let stateBall = this.state.ball
            let stateGame = this.state.game

            //
            let elBall = document.getElementById("ball")
            let goal = document.getElementById("goal")

            //
            stateGrag.x = ev.pageX
            stateGrag.y = ev.pageY
            //
            stateGrag.dtStop = new Date()
            stateGrag.duration = Math.abs(stateGrag.dtStop - stateGrag.dtStart)
            //
            let dragK = stateGrag.duration / this.animationInterval
            stateGrag.dx = (stateGrag.x - stateGrag.sx) / dragK
            stateGrag.dy = (stateGrag.y - stateGrag.sy) / dragK
            // Чтобы  летело не слишком медленно...
            let dl = Math.sqrt(stateGrag.dx * stateGrag.dx + stateGrag.dy * stateGrag.dy)
            if (dl < this.minDl) {
                stateGrag.dx = stateGrag.dx * this.minDl / dl
                stateGrag.dy = stateGrag.dy * this.minDl / dl
            }
            // ... и не слишком быстро
            if (dl > this.maxDl) {
                stateGrag.dx = stateGrag.dx * this.maxDl / dl
                stateGrag.dy = stateGrag.dy * this.maxDl / dl
            }

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
            this.startMoveAnimation(elBall, stateGrag.dx, stateGrag.dy)
        },

        startMoveAnimation(el, dx, dy) {
            let elBall = document.getElementById("ball")
            let elGoal = document.getElementById("goal")

            let stateGrag = this.state.drag
            let stateGoal = this.state.goal
            let stateBall = this.state.ball
            let stateGame = this.state.game

            //
            stateGrag.interval = setInterval(() => {
                // Шаг
                stateGrag.x = stateGrag.x + dx
                stateGrag.y = stateGrag.y + dy

                // Пересечения определяем как пересечение отрезка, проведенного
                // от предыдущей точки движения до текущей с прямоугольником цели.
                // Так мы не дадим "проскочить" снаряду сквозь цель
                // при слишком большой скорости движения.
                let rectTrace = {
                    x1: stateGrag.x,
                    x2: stateGrag.x - dx,
                    y1: stateGrag.y,
                    y2: stateGrag.y - dy,
                }
                let rectGoal = utilsCore.getElRect(elGoal)
                //
                if (utilsCore.intersectRect(rectTrace, rectGoal)) {
                    if (this.ballIsTrue) {
                        stateGoal.value = stateGoal.value - stateGame.goalHitSize
                    }

                    //
                    clearInterval(stateGrag.interval)

                    //
                    stateBall.value = 0
                    //elBall.style.display = "none"

                    //
                    apx.app.eventBus.emit("changeGoalValue", stateGoal.value)

                    //
                    return
                }

                // Выход за границы
                if (stateGrag.x + this.ballWidth > innerWidth || stateGrag.x < 0 || stateGrag.y + this.ballHeihth > innerHeight || stateGrag.y < 0) {
                    clearInterval(stateGrag.interval)
                    stateBall.value = 0
                    //elBall.style.display = "none"
                    return
                }

                // Продолжение движения
                if (!this.ballIsTrue) {
                    stateBall.value = stateBall.value - 1.9 * (this.animationInterval / 1000)
                } else {
                    stateBall.value = stateBall.value + 2.5 * (this.animationInterval / 1000)
                }
                if (stateBall.value > 5) {
                    stateBall.value = 5
                }
                //
                this.moveElementTo(elBall, stateGrag.x, stateGrag.y)
            }, this.animationInterval)
        },

        onTouchStart(event) {
            let touch = event.touches[event.touches.length - 1]

            //
            let stateGrag = this.state.drag
            let stateGoal = this.state.goal

            //
            stateGrag.drag = true

            //
            let ev = {}
            ev.pageX = touch.pageX
            ev.pageY = touch.pageY
            ev.srcElement = event.srcElement

            //
            this.doDragStart(ev)
        },

        onTouchMove(event) {
            let touch = event.touches[event.touches.length - 1]

            //
            let ev = {}
            ev.pageX = touch.pageX
            ev.pageY = touch.pageY
            ev.srcElement = event.srcElement

            //
            this.doDragMove(ev)
        },

        onTouchEnd(event) {
            //console.info("onTouchCancel", event)

            //
            let stateGrag = this.state.drag
            let stateGoal = this.state.goal

            //
            let ev = {}
            if (event.touches.length > 0) {
                let touch = event.touches[event.touches.length - 1]
                ev.pageX = touch.pageX
                ev.pageY = touch.pageY
                ev.srcElement = event.srcElement
            } else {
                ev.pageX = stateGrag.x
                ev.pageY = stateGrag.y
                ev.srcElement = event.srcElement
            }

            //
            this.doDragStop(ev)
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

