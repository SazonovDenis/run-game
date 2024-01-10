<template>

    <q-list bordered separator>
        <template v-for="planTask in planTasks">

            <q-item clickable v-ripple v-if="isItemShown(planTask)">

                <q-item-section top avatar v-if="showAnswerResult">
                    <div
                        :class="getClassAnswerResult(planTask) + ' task-state-state'">
                    </div>
                </q-item-section>

                <q-item-section>
                    <q-item-label overline class="question">
                        <TaskValue :task="planTask.question" :doShowText="true"/>
                    </q-item-label>

                    <q-item-label class="answer">
                        <TaskValue :task="planTask.answer" :doShowText="true"/>
                    </q-item-label>
                </q-item-section>

                <q-item-section top side v-if="showEdit">
                    <div class="text-grey-8 q-gutter-xs">
                        <q-btn flat dense round
                               icon="del"
                               :color="getHiddenColor(planTask.hidden)"
                               @click="itemHiddenToggle(planTask)"/>
                        <q-btn flat dense round
                               icon="star"
                               :color="getStarredColor(planTask.starred)"
                               @click="itemStarredToggle(planTask)"/>
                    </div>
                </q-item-section>

                <q-item-section top side>

                    <div v-if="showAnswerResult" style="min-width: 2em">

                        <q-badge
                            v-if="planTask.ratingInc > 0"
                            color="green-5"
                            :label="'+'+planTask.ratingInc"/>

                        <q-badge
                            v-if="planTask.ratingDec < 0"
                            color="red-5"
                            :label="planTask.ratingDec"/>

                        <!--
                            <div class="row">
                                <q-item-label caption>5 min ago</q-item-label>
                            </div>
                        -->
                    </div>

                    <div v-else style="min-width: 2em">

                        <q-badge
                            :text-color="getRatingTextColor(planTask.rating)"
                            :color="getRatingColor(planTask.rating)"
                            :label="planTask.rating"/>


                        <!--
                            <div class="row">
                                <q-item-label caption>5 min ago</q-item-label>
                            </div>
                        -->
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
        planTasks: null,
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

        getStarredColor(starred) {
            if (starred) {
                return "yellow-8"
            } else {
                return "grey-5"
            }
        },

        getHiddenColor(hidden) {
            if (hidden) {
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
            task.hidden = !task.hidden
            if (task.hidden && task.starred) {
                task.starred = false
            }
            ctx.gameplay.api_saveTaskUsr(task.id, task)
        },

        itemStarredToggle(task) {
            task.starred = !task.starred
            if (task.starred && task.hidden) {
                task.hidden = false
            }
            ctx.gameplay.api_saveTaskUsr(task.id, task)
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