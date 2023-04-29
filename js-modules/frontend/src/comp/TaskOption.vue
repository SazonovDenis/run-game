<template>
    <div v-bind:id="'taskOption-'+taskOption.id" class="option"
         v-on:mousedown="onMouseDown"
         v-on:mousemove="onMouseMove"
         v-on:mouseup="onMouseUp"

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
    },

    data: function() {
        return {
            state: {}
        }
    },

    methods: {
        onMouseDown(event) {
            console.info("onMouseDown", event)

            let ball = document.getElementById("ball")
            let obj = event.srcElement
            let state = this.state
            let taskOption = this.taskOption
            let animationInterval = this.animationInterval

            let ballWidth = 32
            let ballHeihth = 32

            console.info("obj: ", obj)
            console.info("obj.id: ", obj.id)

            ball.style.display = 'block'
            ball.innerText = taskOption.text

            clearInterval(state.interval)

            // передвинуть мяч под координаты курсора
            // и сдвинуть на половину ширины/высоты для центрирования
            function moveAt(el, pageX, pageY) {
                el.style.left = pageX - el.offsetWidth / 2 + 'px'
                el.style.top = pageY - el.offsetHeight / 2 + 'px'
            }

            function onMouseMove(event) {

                moveAt(ball, event.pageX, event.pageY)
            }

            moveAt(ball, event.pageX, event.pageY)

            //
            state.x = event.pageX
            state.y = event.pageY
            state.dtStart = new Date()

            // (3) перемещать по экрану
            document.addEventListener('mousemove', onMouseMove)

            // (4) положить мяч, удалить более ненужные обработчики событий
            ball.onmouseup = function(event) {
                document.removeEventListener('mousemove', onMouseMove)
                ball.onmouseup = null


                state.dtStop = new Date()
                state.duration = Math.abs(state.dtStop - state.dtStart)

                let framesIn = state.duration / animationInterval
                state.dx = (event.pageX - state.x) / framesIn
                state.dy = (event.pageY - state.y) / framesIn
                //
                state.x = event.pageX
                state.y = event.pageY

                //
                console.info(ball)
                console.info("startAnimation, state", state)

                startAnimation(ball, state.dx, state.dy)
                //ball.style.display = 'none'
            }

            function startAnimation(el, dx, dy) {
                state.interval = setInterval(() => {
                    state.x = state.x + dx
                    state.y = state.y + dy

                    if (state.x + ballWidth > innerWidth || state.x < 0 || state.y + ballHeihth > innerHeight || state.y < 0) {
                        clearInterval(state.interval)
                        ball.style.display = "none"
                        return
                    }

                    moveAt(el, state.x, state.y)
                }, animationInterval)
            }
        },

        onMouseMove(ev) {
            //console.info("onMouseMove", ev)
        },

        onMouseUp(ev) {
            console.info("onMouseUp", ev)
        },

        onTouchStart(event) {
            console.info("onTouchStart", event)
            let ball = event.srcElement
        },

        onTouchMove(ev) {
            console.info("onTouchMove", ev)
        },

        onTouchEnd(ev) {
            console.info("onTouchEnd", ev)
        },

        onTouchCancel(ev) {
            console.info("onTouchCancel", ev)
        },

        onDragStart(ev) {
            return false
        },

        moveAt(pageX, pageY) {
            let ball = this
            ball.style.left = pageX - ball.offsetWidth / 2 + 'px'
            ball.style.top = pageY - ball.offsetHeight / 2 + 'px'
        },


    },

    computed: {}
}
</script>


<style>

.option {
    max-width: 20em;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    background-color: #fff5da;
}

</style>

