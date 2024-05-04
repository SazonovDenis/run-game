<template>
    <div
        v-bind:id="'taskOption-' + taskOption.id"
        v-bind:class="'option ' + getClassName(taskOption)">

        <!--
        на случай если у ответа есть звук.
        А вообще заюзать TaskValue
        -->
        <div v-if="taskOption.valueSound">
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
            <span v-if="taskOption.valueTranslate">
                {{ taskOption.valueTranslate }}
            </span>

            <span v-if="taskOption.valueSpelling">
                {{ taskOption.valueSpelling }}
            </span>

        </div>
    </div>
</template>

<script>

import ctx from "../gameplayCtx"
import utils from "../utils"
import TaskValue from "../comp/TaskValue"

export default {
    components: {
        TaskValue
    },

    props: {
        taskOption: {},
        state: {},
    },

    data() {
        return {}
    },

    methods: {
        getClassName(taskOption) {
            if (this.state.mode.modeShowOptions == "hint-true") {
                if (taskOption.isTrue) {
                    return "fact-true"
                } else {
                    return "fact-false"
                }
            } else {
                return "fact"
            }
        },

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
        this.audio = new Audio()
        this.audio.src = utils.getAudioSrc(this.taskOption)
    },

    computed: {}

}
</script>


<style>

.option {
    user-select: none;
    max-width: 20em;
    margin: 5px;
    padding: 1em 1em;
    border-radius: 0.5rem;
    min-width: 10em;

    color: #202020;
}

.option-text {
    font-size: 1.5em;
}

.fact {
    background-color: #ecf9ff;
}

.fact-true {
    background-color: #e6ffda;
}

.fact-false {
    color: silver;
}


</style>

