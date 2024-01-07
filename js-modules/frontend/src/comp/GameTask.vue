<template>
    <div class="row task" @click="play">
        <div v-if="taskHasSound" class="task-sound">
            <template v-if="canPlaySound">
                <q-icon size="1em" name="speaker-on"/>
            </template>
            <template v-else>
                <q-icon size="1em" name="speaker"/>
            </template>
        </div>
        <div v-else class="task-sound">
            <q-icon size="1em" name="speaker-off"/>
        </div>

        <div class="textQuestion">
            {{ task.textQuestion }}
        </div>
        <div>
            &ndash;
        </div>
        <div class="textAnswer">
            {{ task.textAnswer }}
        </div>
    </div>
</template>

<script>

import {apx} from "../vendor"
import dbConst from "../dao/dbConst"
import utils from "../utils"


/**
 * Задание. Пара: вопрос и ответ
 */
export default {
    components: {},

    props: {
        task: {},
    },

    data() {
        return {
            taskSoundLoaded: false,
        }
    },

    methods: {
        play() {
            if (this.canPlaySound) {
                try {
                    this.audio.play()
                } catch(e) {
                    console.error(e)
                }
            } else {
                this.loadAudioTask()
            }
        },

        loadAudioTask() {
            // Останавливаем текущий звук
            try {
                this.audio.pause()
            } catch(e) {
                console.error(e)
            }

            // Новый звук
            this.taskSoundLoaded = false
            this.audio.src = utils.getAudioSrc(this.taskQuestion)
        },

        onSoundLoaded() {
            this.taskSoundLoaded = true
            //console.info("onSoundLoaded: " + this.audio.src)
            //
            this.audio.play()
        },

        onSoundError() {
            this.taskSoundLoaded = false
            //console.info("onSoundError: " + this.audio.src)
        },

    },

    computed: {

        taskHasSound() {
            return (
                this.task.factQuestionDataType == dbConst.DataType_word_sound
            )
        },


        wave() {
            return apx.url.ref("run/game/web/img/wave.png")
        },

        canPlaySound() {
            return (
                this.task != null &&
                this.task.soundQuestion != null &&
                this.taskSoundLoaded
            )
        },

        doShowText() {
            return (
                this.task != null &&
                this.task.textQuestion != null
            )
        },

        doShowSound() {
            return (
                this.task != null &&
                this.task.soundQuestion != null
            )
        },

    },

    mounted() {
        this.audio = new Audio()
        this.audio.addEventListener('loadeddata', this.onSoundLoaded, false)
        this.audio.addEventListener('error', this.onSoundError, false)
    },

    unmounted() {
        if (this.audio != null) {
            this.audio.removeEventListener('loadeddata', this.onSoundLoaded)
            this.audio.addEventListener('error', this.onSoundError)
            this.audio = null
        }
    },


}
</script>


<style lang="less" scoped>

.task div {
    padding-right: 0.2em;
}

.task-text-image img {
    height: 1.6em;
    opacity: 0.8;
}

.textQuestion {
    color: #474747;
}

.textAnswer {
    color: #2462ae;
    font-style: italic;
}

</style>
