<template>

    <template v-if="taskHasSound">

        <div class="row" @click="play()">

            <div v-if="doShowText" class="row task-text">
                <div v-if="task.valueSpelling" class="task-spelling">
                    {{ task.valueSpelling }}
                </div>
                <div v-if="task.valueTranslate" class="task-translate">
                    {{ task.valueTranslate }}
                </div>

<!--
                <q-badge
                    style="font-size: 0.5rem"
                    class="q-ml-xs"
                    text-color="white"
                    color="primary">
                    Анг
                </q-badge>
-->

                <div class="task-sound q-ml-xs">
                    <template v-if="soundPlaying">
                        <q-icon name="speaker-on" class="q-pb-xs" color="orange-10"
                                size="0.9em"/>
                    </template>
                    <template v-else>
                        <q-icon name="headphones" class="q-pb-xs" color="orange-10"
                                size="0.9em"/>
                    </template>
                </div>
            </div>

            <div v-else :class="'task-text-image ' + classAnimation">
                <q-icon name="soundwave" class="animation animation-1" size="1.2em"/>
                <q-icon name="soundwave" class="animation animation-2" size="1.2em"/>
                <q-icon name="soundwave" class="animation animation-3" size="1.2em"/>
                <q-icon name="soundwave" class="animation animation-4" size="1.2em"/>
                <q-icon name="soundwave" class="animation animation-5" size="1.2em"/>
            </div>

        </div>

    </template>


    <template v-else>

        <div class="row">

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

import utils from '../utils'
import dbConst from "run-game-frontend/src/dao/dbConst"


/**
 * Значение задания. Текст, звук и т.п.
 */
export default {
    components: {},

    props: {
        task: {},
        doShowText: {
            type: Boolean,
            default: true,
        },
        showTaskHint: {
            type: Boolean,
            default: true,
        },
    },

    data() {
        return {
            taskSoundLoaded: false,
            soundPlaying: false,
        }
    },

    watch: {
        task: function() {
            this.taskSoundLoaded = false
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
                if (!this.taskSoundLoaded) {
                    this.loadAudioTask()
                }
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

            // Автоматически играем звук, если тип попроса это разрешает (тап вопроса - правописание или звук)
            if (this.canPlaySoundAuto()) {
                this.audio.play()
            }
        },

        onSoundError() {
            this.taskSoundLoaded = false
        },

        onSoundPlay() {
            this.soundPlaying = true
        },

        onSoundPause() {
            this.soundPlaying = false
        },

        canPlaySoundAuto() {
            return (
                this.showTaskHint ||
                this.task.dataType === dbConst.DataType_word_sound ||
                this.task.dataType === dbConst.DataType_word_spelling
            )
        },


    },


    computed: {

        classAnimation() {
            if (this.soundPlaying) {return "animated"} else {return ""}
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
                this.taskSoundLoaded &&
                this.canPlaySoundAuto()
            )
        },

    },

    mounted() {
        this.audio = new Audio()
        this.audio.addEventListener('loadeddata', this.onSoundLoaded, false)
        this.audio.addEventListener('error', this.onSoundError, false)
        this.audio.addEventListener('play', this.onSoundPlay, false)
        this.audio.addEventListener('pause', this.onSoundPause, false)
    },

    unmounted() {
        if (this.audio != null) {
            this.audio.removeEventListener('loadeddata', this.onSoundLoaded)
            this.audio.removeEventListener('error', this.onSoundError)
            this.audio.removeEventListener('play', this.onSoundPlay)
            this.audio.removeEventListener('pause', this.onSoundPause)
            this.audio = null
        }
    },

}
</script>


<style scoped>

.task-text {
}

.task-sound {
}

.task-spelling {
}

.task-translate {
}

.task-text-image img {
    margin-top: 0.1em;
    height: 1em;
    opacity: 0.8;
}

.animated > .animation {
    animation-name: play;
    animation-duration: 0.5s;
    animation-iteration-count: infinite;
}

@keyframes play {
    from {
        opacity: 1;
    }
    50% {
        opacity: 0.2;
    }
    to {
        opacity: 1;
    }
}

.animation-1 {
    animation-delay: .0s;
}

.animation-2 {
    animation-delay: .1s;
}

.animation-3 {
    animation-delay: .2s;
}

.animation-4 {
    animation-delay: .3s;
}

.animation-5 {
    animation-delay: .4s;
}


</style>