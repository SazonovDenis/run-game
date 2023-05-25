<template>
    <div class="task-bar">

        <div class="task" @click="play">

            <div v-if="canPlaySound()" class="task-sound">
                <template v-if="doShowText">
                    <q-icon size="1.8em" name="speaker-on"/>
                </template>
                <template v-else>
                    <q-icon size="1.8em" name="speaker"/>
                </template>
            </div>
            <div v-else class="task-sound">
                <q-icon size="1.8em" name="speaker-off"/>
            </div>

            <div v-if="doShowText" class="task-text">
                {{ task.text }}
            </div>
            <div v-else class="task-text-image">
                <img :src="wave">
            </div>

            <!--
                        <div>Q: {{ task.dataTypeQuestion }}, A: {{ task.dataTypeAnswer }}</div>
            -->

        </div>

        <div class="task-help">
            <q-icon size="2em" name="help" @click="showHint"/>
        </div>

    </div>
</template>

<script>

import {apx} from "../vendor"
import dbConst from "../dao/dbConst"
import ctx from "../gameplayCtx"

export default {
    components: {},

    props: {
        task: {},
        state: {},
    },

    methods: {

        onTaskOptionSelected(taskOption) {
            // Играть звук, если была подсказка - значит идет заучивание
            if (taskOption.isTrue && this.state.alwaysShowText) {
                this.play()
            }
        },

        // Останавливаем текущий звук
        onLoadedGameTask(gameTask) {
            try {
                this.audio.pause()
            } catch(e) {
                console.error(e)
            }
        },

        canPlaySound() {
            return (
                this.task.sound != null &&
                (
                    this.state.alwaysShowText ||
                    this.task.dataTypeQuestion == dbConst.DataType_word_sound ||
                    this.task.dataTypeQuestion == dbConst.DataType_word_spelling
                )
            )
        },

        play() {
            if (this.canPlaySound()) {
                try {
                    this.audio.play()
                } catch(e) {
                    console.error(e)
                }
            }
        },

        showHint() {
            this.state.alwaysShowText = true

            //
            this.play()

            //
            ctx.eventBus.emit("showHint", true)
        },

    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                this.state.alwaysShowText = false

                // Новый звук
                if (this.task.sound) {
                    this.audio.src = apx.url.ref("sound/" + this.task.sound)
                } else {
                    this.audio.src = ""
                }
                //this.audio.src = apx.url.ref("sound/1000-puzzle-english/mp3/campbridge_UK/able.mp3")
            }, deep: true
        }
    },

    computed: {

        wave() {
            return apx.url.ref("run/game/web/wave.png")
        },

        doShowText() {
            return (
                this.task.text != null &&
                (
                    this.state.alwaysShowText ||
                    this.task.dataTypeQuestion != dbConst.DataType_word_sound
                )
            )
        },

        doShowSound() {
            return (
                this.task.sound != null
            )
        },

    },

    mounted() {
        let audio = this.audio = new Audio()
        let th = this
        audio.addEventListener('loadeddata', function() {
            th.play()
        }, false)

        //
        ctx.eventBus.on("taskOptionSelected", this.onTaskOptionSelected)
        ctx.eventBus.on("loadedGameTask", this.onLoadedGameTask)
    },

    unmounted() {
        ctx.eventBus.off("taskOptionSelected", this.onTaskOptionSelected)
        ctx.eventBus.off("loadedGameTask", this.onLoadedGameTask)
    },

}
</script>


<style>

.task-help, .task-sound, .task-text {
}

.task-bar {
    display: flex;
    flex-direction: row;

    height: 2.5em;
    margin: 5px;

    user-select: none;
    border-radius: 5px;
    background-color: #e6ffda;
}

.task {
    display: flex;
    flex-direction: row;

    flex-grow: 100;

    padding: 5px;
}

.task-sound {
    color: #7a7a7a;
}

.task-help {
    padding: 3px;
    color: #474747;
}

.task-text {
    padding-left: 5px;
    padding-right: 5px;
    font-size: 1.5em;
}

.task-text-image img {
    height: 1.6em;
    opacity: 0.8;
}

</style>