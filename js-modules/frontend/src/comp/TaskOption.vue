<template>
    <div v-bind:id="'taskOption-'+taskOption.id" class="option"
         v-on:mousedown="onMouseDown"

         v-on:touchstart="onTouchStart"
         v-on:touchmove="onTouchMove"
         v-on:touchend="onTouchEnd"
         v-on:touchcancel="onTouchCancel"

    >
        Вариант: {{ taskOption.text }}
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        taskOption: null,
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
            default: 5,
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
            let dragK = state.duration / this.animationInterval
            state.dx = (state.x - state.sx) / dragK
            state.dy = (state.y - state.sy) / dragK
            // Чтобы не слишком медленно летело
            let dl = Math.sqrt(state.dx * state.dx + state.dy * state.dy)
            if (dl < this.minDl) {
                state.dx = state.dx * this.minDl / dl
                state.dy = state.dy * this.minDl / dl
            }

            //
            this.startMoveAnimation(ball, state.dx, state.dy)
        },

        startMoveAnimation(el, dx, dy) {
            let ball = document.getElementById("ball")
            let goal = document.getElementById("goal")

            let state = this.state

            state.interval = setInterval(() => {
                // Шаг
                state.x = state.x + dx
                state.y = state.y + dy

                // Пересечения определяем как пересечение отрезка, проведенного
                // от предыдущей точки движения до текущей с прямоугольником цели.
                // Так мы не дадим "проскочить" снаряду сквозь цель
                // при слишком большой скорости движения.
                let rectTrace = {
                    x1: state.x,
                    x2: state.x - dx,
                    y1: state.y,
                    y2: state.y - dy,
                }
                let rectGoal = this.getElRect(goal)
                //
                if (this.intersectRect(rectTrace, rectGoal)) {
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

                    //
                    return
                }

                // Выход за границы
                if (state.x + this.ballWidth > innerWidth || state.x < 0 || state.y + this.ballHeihth > innerHeight || state.y < 0) {
                    clearInterval(state.interval)
                    ball.style.display = "none"
                    return
                }

                // Продолжение движения
                this.moveElementTo(ball, state.x, state.y)
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

        // передвинуть под координаты x, y
        // и сдвинуть на половину ширины/высоты для центрирования
        moveElementTo(el, x, y) {
            el.style.left = x - el.offsetWidth / 2 + 'px'
            el.style.top = y - el.offsetHeight / 2 + 'px'
        },

        getElRect(el) {
            let rect = {
                x1: el.offsetLeft,
                x2: el.offsetLeft + el.clientWidth,
                y1: el.offsetTop,
                y2: el.offsetTop + el.clientHeight,
            }
            return rect
        },

        intersect(ball, goal) {
            let rectA = this.getElRect(goal)
            let rectB = this.getElRect(ball)

            return this.intersectRect(rectA, rectB)
        },

        intersect_(ball, goal) {
            let rectA = getElRect(goal)
            let rectB = getElRect(ball)

            if (
                ball.offsetTop > goal.offsetTop && ball.offsetTop < goal.offsetTop + goal.clientHeight &&
                ball.offsetLeft > goal.offsetLeft && ball.offsetLeft < goal.offsetLeft + goal.clientWidth
            ) {
                return true
            } else {
                return false
            }
        },

        getXMin(rect) {
            if (rect.x1 < rect.x2) {return rect.x1} else {return rect.x2}
        },

        getXMax(rect) {
            if (rect.x1 > rect.x2) {return rect.x1} else {return rect.x2}
        },

        getYMin(rect) {
            if (rect.y1 < rect.y2) {return rect.y1} else {return rect.y2}
        },

        getYMax(rect) {
            if (rect.y1 > rect.y2) {return rect.y1} else {return rect.y2}
        },

        intersectRect(rectA, rectB) {
            let bxMin = this.getXMin(rectB)
            let bxMax = this.getXMax(rectB)
            let axMin = this.getXMin(rectA)
            let axMax = this.getXMax(rectA)

            let ayMin = this.getYMin(rectA)
            let ayMax = this.getYMax(rectA)
            let byMin = this.getYMin(rectB)
            let byMax = this.getYMax(rectB)

            return (
                bxMin >= axMin && bxMin <= axMax ||
                bxMax >= axMin && bxMax <= axMax ||
                bxMin <= axMin && bxMax >= axMax
            ) && (
                byMin >= ayMin && byMin <= ayMax ||
                byMax >= ayMin && byMax <= ayMax ||
                byMin <= ayMin && byMax >= ayMax
            );
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

