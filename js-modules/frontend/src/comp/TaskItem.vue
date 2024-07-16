<template>

    <q-item
        :class="getClassItemRow(taskItem, index)"
        clickable v-ripple
    >

        <q-item-section top avatar v-if="showAnswerResult">
            <div
                :class="getClassAnswerResult(taskItem) + ' task-state-state'">
            </div>
        </q-item-section>


        <q-item-section>

            <q-item-label overline
                          :class="'question' + getItemClass(taskItem)"
                          v-if="isFactFirstAnswer(taskItem, index)">

                <TaskValue v-if="showTaskData"
                           :task="taskItem.taskQuestion"
                           :doShowText="true"/>

                <TaskValue v-else
                           :task="taskItem.question"
                           :doShowText="true"/>

            </q-item-label>


            <q-item-label class="answer">

                <TaskValue v-if="showTaskData"
                           :task="taskItem.taskAnswer"
                           :doShowText="true"/>

                <TaskValue v-else
                           :task="taskItem.answer"
                           :doShowText="true"/>

            </q-item-label>

        </q-item-section>


        <q-item-section top side v-if="showEdit">

            <div class="text-grey-8 q-gutter-xs">

                <template v-for="menuItem in itemsMenu">

                    <template v-if="menuItem.label">

                        <q-btn v-if="menuItem.hidden !== true"
                               no-caps dense rounded unelevated
                               size="0.9em"
                               :label="menuItem.label"
                               :_flat="!itemMenuOutline(menuItem, taskItem)"
                               :outline="itemMenuOutline(menuItem, taskItem)"
                               :icon="itemMenuIcon(menuItem, taskItem)"
                               :color="itemMenuColor(menuItem, taskItem)"
                               :text-color="itemMenuTextColor(menuItem, taskItem)"
                               @click="itemMenuClick(menuItem, taskItem)"
                        />

                    </template>

                    <template v-else>

                        <q-btn v-if="!itemMenuHidden(menuItem, taskItem)"
                               dense round
                               size="1.2em"
                               :flat="!itemMenuOutline(menuItem, taskItem)"
                               :outline="itemMenuOutline(menuItem, taskItem)"
                               :icon="itemMenuIcon(menuItem, taskItem)"
                               :color="itemMenuColor(menuItem, taskItem)"
                               @click="itemMenuClick(menuItem, taskItem)"
                        />
                    </template>

                </template>


                <!--
                <q-btn flat dense round
                       icon="more-h"
                       size="1.0em"
                />
                -->

            </div>

        </q-item-section>


        <q-item-section top side v-if="showRatingDiff && showRating">

            <template v-if="showRatingDiff">

                <q-badge
                    v-if="taskItem.ratingTaskDiff > 0"
                    style="font-weight: bold;"
                    color="white"
                    text-color="green-9"
                    text-weight="bold"
                    :label="'+' + taskItem.ratingTaskDiff"/>
                <q-badge
                    v-if="taskItem.ratingTaskDiff < 0"
                    style="font-weight: bold;"
                    color="white"
                    text-color="red-7"
                    text-weight="bold"
                    :label="taskItem.ratingTaskDiff"/>

            </template>


            <template v-if="showRating">

                <q-badge
                    style="font-weight: bold;"
                    :text-color="getRatingTextColor(taskItem.ratingTask)"
                    :color="getRatingColor(taskItem.ratingTask)"
                    :label="taskItem.ratingTask"/>

            </template>

        </q-item-section>


    </q-item>

</template>

<script>

import TaskValue from "./TaskValue"

export default {

    name: "TaskItem",

    components: {
        TaskValue
    },

    props: {
        taskItem: Object,

        itemsMenu: null,

        /**
         * Какое тело вопроса и ответа показывать.
         *
         * Если showTaskData == true, то показывать ПОДГОТОВЛЕННЫЕ ЗАДАНИЯ (вопрос и ответ)
         * (данные из Task<-TaskOption и Task<-TaskQuestion),
         * что полезно при просмотре списка заданий в игре.
         *
         * Если showTaskData == false, то показывать значения ФАКТОВ задания
         * (данные из Task.factQuestion->Fact.* и Task.factQuestion->Fact.*),
         * что полезно при просмотре списка фактов в плане.
         */
        showTaskData: {type: Boolean},

        showAnswerResult: {type: Boolean},
        showRating: {type: Boolean},
        showRatingDiff: {type: Boolean, default: true},

        showEdit: {type: Boolean},

        // todo определиться как показывать несколько вариантов ответов
        index: 0,
    },

    methods: {

        getItemClass(item) {
            if (!item) {
                return ""
            }

            let classStr = ""
            if (item.isHidden) {
                classStr = classStr + " item-is-hidden"

            } else if (item.isInPlan) {
                //classStr = classStr + " item-in-plan"
            }

            return classStr
        },

        itemMenuOutline(menuItem, taskItem) {
            if (menuItem.outline) {
                if (menuItem.outline instanceof Function) {
                    return menuItem.outline(taskItem)
                } else {
                    return menuItem.outline
                }
            } else {
                return false
            }

        },

        itemMenuIcon(menuItem, taskItem) {
            if (menuItem.icon) {
                if (menuItem.icon instanceof Function) {
                    return menuItem.icon(taskItem)
                } else {
                    return menuItem.icon
                }
            } else {
                return null
            }

        },

        itemMenuColor(menuItem, taskItem) {
            if (menuItem.color) {
                if (menuItem.color instanceof Function) {
                    return menuItem.color(taskItem)
                } else {
                    return menuItem.color
                }
            } else {
                return "primary"
            }

        },

        itemMenuTextColor(menuItem, taskItem) {
            if (menuItem.textColor) {
                if (menuItem.textColor instanceof Function) {
                    return menuItem.textColor(taskItem)
                } else {
                    return menuItem.textColor
                }
            } else {
                return "black"
            }

        },

        itemMenuHidden(menuItem, taskItem) {
            if (menuItem.hidden) {
                if (menuItem.hidden instanceof Function) {
                    return menuItem.hidden(taskItem)
                } else {
                    return menuItem.hidden
                }
            } else {
                return false
            }
        },

        itemMenuClick(menuItem, taskItem) {
            if (menuItem.onClick) {
                menuItem.onClick(taskItem)
            }
        },

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

        getClassItemRow(task, index) {
            if (index === 0 || this.isFactFirstAnswer(task, index)) {
                return "item-first"
            } else {
                return "item-next"
            }
        },

        isFactFirstAnswer(task, index) {
            ///////////////////////////////
            // todo определиться как показывать несколько вариантов ответов
            return true
            ///////////////////////////////

            if (index === 0) {
                return true
            } else if (task.factQuestion !== this.tasks[index - 1].factQuestion) {
                return true
            } else {
                return false
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

    },

}

</script>

<style scoped>

.question {
    font-size: 1.2em;
    color: #202020;
}

.answer {
    font-size: 1.0em;
    color: #09468e;
    font-style: italic;
}

.item-is-hidden {
    color: rgba(20, 20, 150, 0.8);
}

.item-in-plan {
    _color: rgba(50, 100, 50, .9);
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


.item-first {
    border-top: 1px solid silver;
}

.item-next {
    padding-top: 0;
    _padding-top: 0.2em;
    _padding-bottom: 0.2em;
}


</style>