<template>
    <div>

        <q-item class="q-py-sm item-info-item" clickable v-ripple
                @click.stop="play()"
        >

            <div>

                <div class="row">

                    <div class="rgm-task-question">
                        {{ item.valueSpelling }}
                    </div>

                    <div class="rgm-task-transcription q-ml-sm"
                         v-if="item.valueTranscription">
                        [{{ item.valueTranscription }}]
                    </div>

                    <div class="task-sound q-ml-sm q-mt-xs">

                        <div class="task-sound q-ml-xs">
                            <template v-if="soundLoading">
                                <q-spinner color="orange-10" :thickness="4"
                                           size="1.2em"/>
                            </template>
                            <template v-else-if="soundPlaying">
                                <q-icon name="speaker-on" class="q-pb-xs"
                                        color="orange-10"
                                        size="1.1em"/>
                            </template>
                            <template v-else>
                                <q-icon name="headphones" class="q-pb-xs"
                                        color="orange-10"
                                        size="1.1em"/>
                            </template>
                        </div>

                    </div>

                </div>


                <div class="row q-mt-xs">

                    <template v-for="(translate, index) of item.valueTranslate">
                        <div v-if="index!==0" class="q-mx-sm">&bull;</div>
                        <div class="rgm-task-answer">{{ translate }}</div>
                    </template>

                </div>

            </div>

        </q-item>


        <q-list class="rgm-task-list">

            <template v-for="task of tasks">

                <div class="rgm-task-item-container rgm-task-item-container-border">

                    <q-item class="task-item item-first item-info-task">

                        <q-item-section class="task-item-task-question">

                            <q-item-label
                                @click.stop="showValueExample(task, !task.showValueExample)"
                            >
                                <div
                                    :class="'rgm-task-answer ' + getClass_taskAnswer(task)">
                                    {{ task.valueTranslate }}
                                </div>
                            </q-item-label>

                        </q-item-section>


                        <q-item-section top side class="task-item-menu">

                            <div class="text-grey-8 q-gutter-xs">

                                <q-btn v-if="task.valueExample && !task.showValueExample"
                                       no-caps dense round unelevated
                                       size="1.2em"
                                       :outlined="true"
                                       icon="more-h"
                                       @click.stop="showValueExample(task, true)"
                                />

                                <template v-for="menuItem in itemMenu">

                                    <ItemMenuItem :item="task" :menuItem="menuItem"/>

                                </template>

                            </div>

                        </q-item-section>

                    </q-item>


                    <q-item v-if="task.showValueExample"
                            class="item-info-example">

                        <q-item-section>

                            <q-item-label class="q-mt-xs">
                                <div v-for="(valueExample) in task.valueExample"
                                     class="rgm-task-example q-my-sm"
                                     @click.stop="showValueExample(task, false)"
                                >
                                    <span v-html="makeValueExample(valueExample)"/>
                                </div>
                            </q-item-label>

                        </q-item-section>

                    </q-item>

                </div>

            </template>

        </q-list>

    </div>

</template>

<script>

import TaskList from "../comp/TaskList"
import ItemMenuItem from "../comp/ItemMenuItem"
import AudioTask from "../comp/AudioTask"

export default {

    name: "ItemInfo",

    components: {
        TaskList, ItemMenuItem
    },

    props: {
        itemMenu: {type: Array, default: null},
        item: {type: Object, default: null},
        tasks: {type: Array, default: null},
    },

    data() {
        return {
            soundLoading: false,
            soundPlaying: false,
        }
    },

    computed: {

        taskHasSound() {
            return (
                this.item != null &&
                this.item.valueSound != null
            )
        },

    },

    methods: {

        play() {
            this.audio.play()
        },

        onSoundState(state, event) {
            this.soundLoading = state.soundLoading
            this.soundPlaying = state.soundPlaying
            if (event === "loaded") {
                this.audio.play()
            }
        },

        showValueExample(task, doShow) {
            if (task.valueExample) {
                task.showValueExample = doShow
            }
        },

        makeValueExample(valueExample) {
            let valueExampleArr = valueExample.split("-")
            if (valueExampleArr.length === 2) {
                let valueExampleSpelling = valueExampleArr[0]
                let valueExampleTranslate = valueExampleArr[1]
                valueExample = valueExampleSpelling + " &ndash; <span class='rgm-task-value-example-translate'>" + valueExampleTranslate + "</span>"
            } else {
                valueExampleArr = valueExample.split("--")
                if (valueExampleArr.length === 2) {
                    let valueExampleSpelling = valueExampleArr[0]
                    let valueExampleTranslate = valueExampleArr[1]
                    valueExample = valueExampleSpelling + " &ndash; <span class='rgm-task-value-example-translate'>" + valueExampleTranslate + "</span>"
                }
            }

            let valueSpellingWrapped = "<span class='rgm-task-value-example-spelling'>" + this.item.valueSpelling + "</span>"
            return valueExample.replace("~", valueSpellingWrapped)
        },

        getClass_taskAnswer(task) {
            if (task.showValueExample) {
                return "task-answer-padding-top"
            } else {
                return ""
            }
        },

        /*
                getClass_taskExample(index) {
                    if (index===0) {
                        return "task-example-first"
                    } else {
                        return ""
                    }
                } ,
        */


    },

    async mounted() {
        this.audio = new AudioTask(this.item)
        this.audio.onSoundState = this.onSoundState
    },

    unmounted() {
        this.audio.destroy()
        this.audio = null
    },


}

</script>

<style scoped>

.item-info-item {
    background-color: #fafafa;
    font-size: 120%;
}

.item-info-task {
    padding: 0.2em 1em;
}

.item-info-example {
    margin-top: -1.5em;
}

</style>