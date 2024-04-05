<template>

    <div class="row task">
        <TaskValue :task="task.question" :doShowText="true"/>
        <div>&ndash;</div>
        <TaskValue :task="task.answer" :doShowText="true"/>
    </div>

</template>

<script>

import {apx} from "../vendor"
import dbConst from "../dao/dbConst"
import utils from "../utils"
import TaskValue from "./TaskValue"


/**
 * Задание. Пара: вопрос и ответ
 */
export default {
    components: {TaskValue},

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
                this.task.factQuestionDataType === dbConst.DataType_word_sound
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

</style>
