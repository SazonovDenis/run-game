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
            if (taskOption.isTrue && this.state.showTaskHint) {
                this.play()
            }
        },

        useAudioTask() {
            // Останавливаем текущий звук
            try {
                this.audio.pause()
            } catch(e) {
                console.error(e)
            }

            //
            this.state.showTaskHint = false

            // Новый звук
            if (this.task != null && this.task.sound != null) {
                this.audio.src = apx.url.ref("sound/" + this.task.sound)
            } else {
                this.audio.src = ""
            }
        },

        canPlaySound() {
            return (
                this.task != null &&
                this.task.sound != null &&
                (
                    this.state.showTaskHint ||
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
            this.state.showTaskHint = true

            //
            this.play()

            //
            ctx.eventBus.emit("showHint", true)
        },

    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                this.useAudioTask()
            }, deep: true
        }
    },

    computed: {

        wave() {
            return apx.url.ref("run/game/web/wave.png")
        },

        doShowText() {
            return (
                this.task != null &&
                this.task.text != null &&
                (
                    this.state.showTaskHint ||
                    this.task.dataTypeQuestion != dbConst.DataType_word_sound
                )
            )
        },

        doShowSound() {
            return (
                this.task != null &&
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

        //
        th.useAudioTask()
    },

    unmounted() {
        ctx.eventBus.off("taskOptionSelected", this.onTaskOptionSelected)
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