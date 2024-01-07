<template>

    <template v-if="taskHasSound">

        <div class="row task" @click="play(task)">

            <div class="task-sound">
                <template v-if="canPlaySound">
                    <q-icon name="speaker-on"/>
                </template>
                <template v-else>
                    <q-icon name="speaker"/>
                </template>
            </div>


            <div v-if="doShowText" class="task-text">
                <div v-if="task.valueSpelling" class="task-spelling">
                    {{ task.valueSpelling }}
                </div>
                <div v-if="task.valueTranslate" class="task-translate">
                    {{ task.valueTranslate }}
                </div>
            </div>
            <div v-else class="task-text-image">
                <img :src="wave">
            </div>

        </div>

    </template>


    <template v-else>

        <div class="row task">

            <div class="task-sound">
                <q-icon name="speaker-off"/>
            </div>


            <div v-if="task.valueSpelling" class="task-spelling">
                {{ task.valueSpelling }}
            </div>
            <div v-if="task.valueTranslate" class="task-translate">
                {{ task.valueTranslate }}
            </div>

        </div>

    </template>


</template>

<script>

import {apx} from "../vendor"
import utils from '../utils'


/**
 * Значение задания. Текст, звук и т.п.
 */
export default {
    components: {},

    props: {
        task: {},
        doShowText: true,
    },

    data() {
        return {
            taskSoundLoaded: false,
        }
    },

    methods: {

        play(task) {
            console.info("task", task)
            console.info("audio._task", this.audio._task)

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
            this.audio.src = utils.getAudioSrc(this.task)
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

        wave() {
            return apx.url.ref("run/game/web/img/wave.png")
        },

        taskHasSound() {
            return (
                this.task != null &&
                this.task.valueSound != null
            )
        },

        canPlaySound() {
            return (
                this.task != null &&
                this.task.valueSound != null &&
                this.taskSoundLoaded
            )
        },

    },

    mounted() {
        this.audio = new Audio()
        this.audio._task = this.task
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


<style scoped>

.task-sound {
    color: #7a7a7a;
    padding-right: 0.2em;
    size: 1em;
}

.task-text {
}

.task-spelling {
    color: #505050;
}

.task-translate {
    color: #09468e;
    font-style: italic;
}

.task-text-image img {
    height: 1.6em;
    opacity: 0.8;
}

</style>