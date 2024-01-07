<template>
    <div class="task-bar">

        <div class="row task" @click="play">

            <div v-if="canPlaySound" class="task-sound">
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
                {{ task.valueSpelling }}
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
import utils from '../utils'


/**
 * Задание в игре. Вопрос показанный в правильном состоянии (текст скрыт, если нужно)
 */
export default {
    components: {},

    props: {
        task: {},
        state: {},
    },

    methods: {

        loadAudioTask() {
            // Останавливаем текущий звук
            try {
                this.audio.pause()
            } catch(e) {
                console.error(e)
            }

            //
            this.state.showTaskHint = false

            // Новый звук
            this.state.taskSoundLoaded = false
            this.audio.src = utils.getAudioSrc(this.task)
        },

        play() {
            if (this.canPlaySound) {
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


        onTaskOptionSelected(taskOption) {
            // Играть звук, если была подсказка - значит идет заучивание
            if (taskOption.isTrue && this.state.showTaskHint) {
                this.play()
            }
        },

        onSoundLoaded() {
            this.state.taskSoundLoaded = true
            //console.info("onSoundLoaded: " + this.audio.src)
            //
            this.audio.play()
        },

        onSoundError() {
            this.state.taskSoundLoaded = false
            //console.info("onSoundError: " + this.audio.src)
        },

    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                this.loadAudioTask()
            }, deep: true
        }
    },

    computed: {

        wave() {
            return apx.url.ref("run/game/web/img/wave.png")
        },

        canPlaySound() {
            return (
                this.task != null &&
                this.task.valueSound != null &&
                this.state.taskSoundLoaded &&
                (
                    this.state.showTaskHint ||
                    this.task.dataType === dbConst.DataType_word_sound ||
                    this.task.dataType === dbConst.DataType_word_spelling
                )
            )
        },

        doShowText() {
            return (
                this.task != null &&
                this.task.valueSpelling != null &&
                (
                    this.state.showTaskHint ||
                    this.task.dataType !== dbConst.DataType_word_sound
                )
            )
        },

        doShowSound() {
            return (
                this.task != null &&
                this.task.valueSound != null
            )
        },

    },

    mounted() {
        let audio = this.audio = new Audio()
        audio.addEventListener('loadeddata', this.onSoundLoaded, false)
        audio.addEventListener('error', this.onSoundError, false)

        //
        ctx.eventBus.on("taskOptionSelected", this.onTaskOptionSelected)

        //
        this.loadAudioTask()
    },

    unmounted() {
        ctx.eventBus.off("taskOptionSelected", this.onTaskOptionSelected)
    },

}
</script>


<style scoped>

.task-help, .task-sound, .task-text {
}

.task-bar {
    display: flex;
    flex-direction: row;

    height: 3.5em;
    margin: 5px;

    user-select: none;
    border-radius: 0.5rem;
    background-color: #e6ffda;
}

.task {
    flex-grow: 100;

    padding: 1em;
}

.task-sound {
    color: #7a7a7a;
}

.task-help {
    padding: 0.8em;
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