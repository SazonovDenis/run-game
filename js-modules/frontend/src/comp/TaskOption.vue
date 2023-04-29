<template>
    <div v-bind:id="'taskOption-'+taskOption.id" class="option"
         v-on:mousedown="onMouseDown"
         v-on:mousemove="__onMouseMove"
         v-on:mouseup="__onMouseUp"

         v-on:touchstart="onTouchStart"
         v-on:touchmove="onTouchMove"
         v-on:touchend="onTouchEnd"
         v-on:touchcancel="onTouchCancel"

         v-on:ondragstart="onDragStart">
        Вариант: {{ taskOption.text }}
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        taskOption: null,
        animationInterval: 100,
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
    },

    data() {
        return {
            state: {
                dtStart: null,
                dtStop: null,
                drag: null,
                x: null,
                y: null,
                sx: null,
                sy: null,
                goalValue: 5,
            },
        }
    },

    methods: {
        onMouseDown(event) {
            //console.info("onMouseDown", event)

            let ev = {}
            ev.pageX = event.pageX
            ev.pageY = event.pageY
            ev.srcElement = event.srcElement

            let state = this.state
            state.drag = true

            //
            document.addEventListener('mousemove', this.onMouseMove)
            document.addEventListener('mouseup', this.onMouseUp)

            //
            this.doDragStart(ev)
        },

        onMouseMove(event) {
            //console.info("onMouseMove", event)

            let state = this.state

            //
            if (state.drag) {
                let ev = {}
                ev.pageX = event.pageX
                ev.pageY = event.pageY
                ev.srcElement = event.srcElement

                this.doDragMove(ev)
            }
        },

        onMouseUp(event) {
            //console.info("onMouseUp", this.state.dtStart)

            //
            document.removeEventListener('mousemove', this.onMouseMove)
            document.removeEventListener('mouseup', this.onMouseUp)

            //
            let state = this.state

            //
            if (state.drag) {
                state.drag = false

                let ev = {}
                ev.pageX = event.pageX
                ev.pageY = event.pageY
                ev.srcElement = event.srcElement

                this.doDragStop(ev)
            }
        },


        doDragStart(ev) {
            //console.info("doDragStart", ev)

            let ball = document.getElementById("ball")
            let goal = document.getElementById("goal")

            let obj = ev.srcElement
            let state = this.state
            let taskOption = this.taskOption

            state.dtStart = new Date()
            state.sx = ev.pageX
            state.sy = ev.pageY
            state.x = ev.pageX
            state.y = ev.pageY

            //console.info("obj: ", obj)
            console.info("obj.id: ", obj.id)

            //
            clearInterval(state.interval)

            //
            ball.style.display = 'block'
            ball.innerText = taskOption.text

            //
            this.moveElementTo(ball, state.x, state.y)
        },

        doDragMove(ev) {
            //console.info("doDragMove", ev)

            //
            let state = this.state
            let ball = document.getElementById("ball")

            //
            state.x = ev.pageX
            state.y = ev.pageY

            //
            this.moveElementTo(ball, state.x, state.y)
        },

        doDragStop(ev) {
            //console.info("doDragStop", ev)

            let state = this.state

            let ball = document.getElementById("ball")
            let goal = document.getElementById("goal")

            //
            state.x = ev.pageX
            state.y = ev.pageY
            //
            state.dtStop = new Date()
            state.duration = Math.abs(state.dtStop - state.dtStart)
            //
            let framesInSec = state.duration / this.animationInterval
            state.dx = (state.x - state.sx) / framesInSec
            state.dy = (state.y - state.sy) / framesInSec

            //
            this.startMoveAnimation(ball, state.dx, state.dy)
        },

        startMoveAnimation(el, dx, dy) {
            let ball = document.getElementById("ball")
            let goal = document.getElementById("goal")

            let state = this.state

            state.interval = setInterval(() => {
                state.x = state.x + dx
                state.y = state.y + dy

                if (state.x + this.ballWidth > innerWidth || state.x < 0 || state.y + this.ballHeihth > innerHeight || state.y < 0) {
                    clearInterval(state.interval)
                    ball.style.display = "none"
                    return
                }

                if (this.intersect(ball, goal)) {
                    state.goalValue = state.goalValue - 1

                    //
                    clearInterval(state.interval)

                    //
                    this.setGoalValue(state.goalValue)
                    ball.style.display = "none"

                    //
                    if (state.goalValue <= 0) {
                        goal.style.display = "none"
                    }
                }

                this.moveElementTo(el, state.x, state.y)
            }, this.animationInterval)
        },

        onTouchStart(event) {
            let touch = event.touches[event.touches.length - 1]

            //
            let ev = {}
            ev.pageX = touch.pageX
            ev.pageY = touch.pageY
            ev.srcElement = event.srcElement

            //
            let state = this.state
            state.drag = true

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
            let ev = {}
            if (event.touches.length > 0) {
                let touch = event.touches[event.touches.length - 1]
                ev.pageX = touch.pageX
                ev.pageY = touch.pageY
                ev.srcElement = event.srcElement
            } else {
                let state = this.state

                ev.pageX = state.x
                ev.pageY = state.y
                ev.srcElement = event.srcElement
            }

            //
            this.doDragStop(ev)
        },

        onTouchCancel(event) {
            //console.info("onTouchCancel", event)
            this.onTouchEnd(event)
        },

        onDragStart(ev) {
            return false
        },

        // передвинуть мяч под координаты курсора
        // и сдвинуть на половину ширины/высоты для центрирования
        moveElementTo(el, x, y) {
            el.style.left = x - el.offsetWidth / 2 + 'px'
            el.style.top = y - el.offsetHeight / 2 + 'px'
        },

        intersect(ball, goal) {
            if (
                ball.offsetTop > goal.offsetTop && ball.offsetTop < goal.offsetTop + goal.clientHeight &&
                ball.offsetLeft > goal.offsetLeft && ball.offsetLeft < goal.offsetLeft + goal.clientWidth
            ) {
                return true
            } else {
                return false
            }
        },

        setGoalValue(goalValue) {
            let goal = document.getElementById("goal")
            let state = this.state

            //goal.style.width = this.goalSize * goalValue + "px"
            goal.style.height = this.goalSize * goalValue + "px"
        },

    },

    mounted() {
        this.setGoalValue(this.state.goalValue)
    },

    computed: {}
}
</script>


<style>

.option {
    user-select: none;
    max-width: 20em;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    background-color: #fff5da;
}

</style>

