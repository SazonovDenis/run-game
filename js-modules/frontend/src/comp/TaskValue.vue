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

                <div class="task-sound q-ml-xs">
                    <template v-if="soundLoading">
                        <q-spinner color="orange-10" :thickness="4"
                                   size="1em"/>
                    </template>
                    <template v-else-if="soundPlaying">
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

import AudioTask from "./AudioTask"
import dbConst from "../dao/dbConst"


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
            soundLoading: false,
            soundPlaying: false,
        }
    },

    watch: {
        task: function(task) {
            this.audio.setTask(task)
        }
    },

    methods: {

        play() {
            if (this.taskSoundIsNotSecret) {
                this.audio.play()
            }
        },

        onSoundState(state, event) {
            this.soundLoading = state.soundLoading
            this.soundPlaying = state.soundPlaying

            if (event === "loaded") {
                // Автоматически играем звук, если тип вопроса это разрешает
                // (т.е. тип вопроса - правописание или звук)
                if (this.taskSoundIsNotSecret) {
                    this.audio.play()
                }
            }
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

        taskSoundIsNotSecret() {
            let taskSoundIsNotSecret =
                this.task.factType === dbConst.FactType_word_sound ||
                this.task.factType === dbConst.FactType_word_spelling

            return this.showTaskHint || taskSoundIsNotSecret
        }

    },

    mounted() {
        this.audio = new AudioTask(this.task)
        this.audio.onSoundState = this.onSoundState
    },

    unmounted() {
        this.audio.destroy()
        this.audio = null
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