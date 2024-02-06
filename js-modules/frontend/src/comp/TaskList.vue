<template>

    <q-list bordered separator>
        <template v-for="(taskItem, index) in tasks">

            <q-item clickable v-ripple v-if="isItemShown(taskItem)">

                <q-item-section top avatar v-if="showAnswerResult">
                    <div
                        :class="getClassAnswerResult(taskItem) + ' task-state-state'">
                    </div>
                </q-item-section>


                <q-item-section>

                    <q-item-label overline class="question">

                        <div>
                            <TaskValue :task="taskItem.question" :doShowText="true"/>
                        </div>

                    </q-item-label>


                    <q-item-label class="answer">

                        <div class="row">
                            <TaskValue :task="taskItem.answer" :doShowText="true"/>
                        </div>

                    </q-item-label>

                </q-item-section>


                <q-item-section top side v-if="showEdit">

                    <div class="text-grey-8 q-gutter-xs">

                        <q-btn flat dense round
                               icon="del"
                               size="1.2em"
                               :color="getHiddenColor(taskItem.isHidden)"
                               @click="itemHiddenToggle(taskItem)"
                        />

                        <q-btn flat dense round
                               icon="more-h"
                               size="1.0em"
                        />

                    </div>

                </q-item-section>


                <q-item-section top side>

                    <div v-if="showAnswerResult" style="min-width: 2em">

                        <q-badge
                            v-if="taskItem.ratingTaskDiff > 0"
                            color="green-5"
                            :label="'+'+taskItem.ratingTaskDiff"/>

                        <q-badge
                            v-if="taskItem.ratingTaskDiff < 0"
                            color="red-5"
                            :label="taskItem.ratingTaskDiff"/>
                    </div>

                    <div v-else style="min-width: 2em">

                        <q-badge
                            :text-color="getRatingTextColor(taskItem.ratingTask)"
                            :color="getRatingColor(taskItem.ratingTask)"
                            :label="taskItem.ratingTask"/>
                    </div>

                </q-item-section>


            </q-item>

        </template>
    </q-list>

</template>

<script>

import TaskValue from "./TaskValue"
import ctx from "../gameplayCtx"

export default {
    name: "TaskList",

    components: {
        TaskValue
    },

    props: {
        showAnswerResult: false,
        showEdit: false,
        tasks: null,
        filter: null,
    },

    methods: {
        getRatingColor(rating) {
            if (rating >= 0.8) {
                return "green-8"
            } else if (rating >= 0.5) {
                return "green-3"
            } else if (rating >= 0.1) {
                return "yellow-5"
            } else {
                return "blue-grey-1"
            }
        },

        getRatingTextColor(rating) {
            if (rating >= 0.8) {
                return "white"
            } else if (rating >= 0.5) {
                return "black"
            } else if (rating >= 0.1) {
                return "black"
            } else {
                return "black"
            }
        },

        getKnownBadColor(isKnownBad) {
            if (isKnownBad) {
                return "yellow-8"
            } else {
                return "grey-5"
            }
        },

        getKnownGoodColor(isKnownGood) {
            if (isKnownGood) {
                return "yellow-8"
            } else {
                return "grey-5"
            }
        },

        getHiddenColor(isHidden) {
            if (isHidden) {
                return "red-6"
            } else {
                return "grey-7"
            }
        },

        getClassAnswerResult(task) {
            if (task.wasTrue) {
                return "was-true"
            } else if (task.wasFalse) {
                return "was-false"
            } else if (task.wasHint) {
                return "was-hint"
            } else if (task.wasSkip) {
                return "was-skip"
            }
            return ""
        },

        isItemShown(task) {
            if (!this.filter) {
                return true
            } else {
                return this.filter(task)
            }
        },

        itemHiddenToggle(task) {
            task.isHidden = !task.isHidden
            if (task.isHidden) {
                task.isKnownGood = false
                task.isKnownBad = false
            }
            ctx.gameplay.api_saveUsrFact(task.factQuestion, task.factAnswer, task)
        },

        itemKnownBadToggle(task) {
            task.isKnownBad = !task.isKnownBad
            if (task.isKnownBad) {
                task.isHidden = false
                task.isKnownGood = false
            }
            ctx.gameplay.api_saveUsrFact(task.factQuestion, task.factAnswer, task)
        },

        itemKnownGoodToggle(task) {
            task.isKnownGood = !isKnownGood
            if (task.isKnownGood) {
                task.isHidden = false
                task.isKnownBad = false
            }
            ctx.gameplay.api_saveUsrFact(task.factQuestion, task.factAnswer, task)
        },

    },


}


</script>

<style scoped>

.question {
    font-size: 1.2em;
}

.answer {
    font-size: 1.0em;
}

.was-true {
    background-color: #5b9e3a;
}

.was-false {
    background-color: #d61818;
}

.was-hint {
    background-color: #ffea1b;
}

.was-skip {
    background-color: #c8c8c8;
}

.task-state {
    width: 1em;
}

.task-state-state {
    border: 1px solid silver;
    border-radius: 1em;

    width: 1em;
    height: 1em;

    margin-top: 0.3em;
    margin-right: 0.3em;
}


</style>