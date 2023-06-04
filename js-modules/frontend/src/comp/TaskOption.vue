<template>
    <div class="option" v-bind:id="'taskOption-'+taskOption.id">

        <div v-if="this.taskOption.sound">
            <q-btn color="blue" text-color="black" label="Play" @click="play"/>
        </div>

        <div class="option-text"
             v-on:mousedown="onMouseDown"

             v-on:touchstart="onTouchStart"
             v-on:touchmove="onTouchMove"
             v-on:touchend="onTouchEnd"
             v-on:touchcancel="onTouchCancel"
             v-on:click="no-click"
        >
            {{ taskOption.text }}
        </div>
    </div>
</template>

<script>

import {apx} from '../vendor'
import ctx from "../gameplayCtx"

export default {
    components: {
        apx
    },

    props: {
        taskOption: {},

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

            //
            if (!stateDrag.drag) {
                stateDrag.drag = true

                //
                document.addEventListener('mousemove', this.onMouseMove)
                document.addEventListener('mouseup', this.onMouseUp)

                //
                let eventDrag = {}
                eventDrag.x = event.pageX
                eventDrag.y = event.pageY
                eventDrag.srcElement = event.srcElement
                eventDrag.taskOption = this.taskOption

                //
                ctx.eventBus.emit("dragstart", eventDrag)
            }
        },

        onMouseMove(event) {
            //console.info("onMouseMove", event)

            let stateDrag = this.state.drag

            //
            if (stateDrag.drag) {
                //
                let eventDrag = {}
                eventDrag.x = event.pageX
                eventDrag.y = event.pageY
                eventDrag.srcElement = event.srcElement
                eventDrag.taskOption = this.taskOption

                //
                ctx.eventBus.emit("drag", eventDrag)
            }
        },

        onMouseUp(event) {
            document.removeEventListener('mousemove', this.onMouseMove)
            document.removeEventListener('mouseup', this.onMouseUp)

            //
            let stateDrag = this.state.drag

            //
            if (stateDrag.drag) {
                stateDrag.drag = false

                //
                let eventDrag = {}
                eventDrag.x = event.pageX
                eventDrag.y = event.pageY
                eventDrag.srcElement = event.srcElement
                eventDrag.taskOption = this.taskOption

                //
                ctx.eventBus.emit("dragend", eventDrag)
            }
        },

        onTouchStart(event) {
            let touch = event.touches[event.touches.length - 1]

            //
            let stateDrag = this.state.drag

            // Чтобы от второго нажатия не началось параллельное событие
            if (!stateDrag.drag) {
                stateDrag.drag = true

                //
                let eventDrag = {}
                eventDrag.x = touch.pageX
                eventDrag.y = touch.pageY
                eventDrag.srcElement = event.srcElement
                eventDrag.taskOption = this.taskOption

                //
                ctx.eventBus.emit("dragstart", eventDrag)
            }
        },

        onTouchMove(event) {
            let touch = event.touches[event.touches.length - 1]

            //
            let eventDrag = {}
            eventDrag.x = touch.pageX
            eventDrag.y = touch.pageY
            eventDrag.srcElement = event.srcElement
            eventDrag.taskOption = this.taskOption

            //
            ctx.eventBus.emit("drag", eventDrag)
        },

        onTouchEnd(event) {
            //console.info("onTouchCancel", event)

            //
            let stateDrag = this.state.drag

            //
            if (stateDrag.drag) {
                stateDrag.drag = false

                //
                let eventDrag = {}
                if (event.touches.length > 0) {
                    let touch = event.touches[event.touches.length - 1]
                    eventDrag.x = touch.pageX
                    eventDrag.y = touch.pageY
                } else {
                    eventDrag.x = stateDrag.x
                    eventDrag.y = stateDrag.y
                }
                eventDrag.srcElement = event.srcElement
                eventDrag.taskOption = this.taskOption

                //
                ctx.eventBus.emit("dragend", eventDrag)
            }
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

        play() {
            if (this.taskOption.sound) {
                try {
                    this.audio.play()
                } catch(e) {
                    console.error(e)
                }
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
    background-color: #ecf9ff;
}

.option-text {
    font-size: 1.5em;
}


</style>

