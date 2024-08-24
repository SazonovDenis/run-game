<template>

    <q-item
        :class="'task-item ' + getClassItemRow(item, index)"
        clickable v-ripple
        @click="itemClick()"
    >

        <q-item-section v-if="showAnswerResult"
                        top avatar>
            <TaskAnswerResult :item="item"/>
        </q-item-section>


        <q-item-section class="task-item-task-question">

            <q-item-label :class="'row question ' + getItemClass(item)"
                          v-if="isFactFirstAnswer(item, index)">

                <TaskValue v-if="showTaskData"
                           :task="item.taskQuestion"
                           :doShowText="true"/>

                <TaskValue v-else
                           :task="item.question"
                           :doShowText="true"/>

            </q-item-label>


            <q-item-label class="row">

                <template v-if="item.maskAnswerResult === true">

                    <div class="answer">
                        * * * * * * * *
                    </div>

                </template>

                <template v-else>

                    <TaskValue v-if="showTaskData"
                               class="answer"
                               :task="item.taskAnswer"
                               :doShowText="true"/>

                    <TaskValue v-else
                               class="answer"
                               :task="item.answer"
                               :doShowText="true"/>

                </template>


            </q-item-label>

        </q-item-section>


        <q-item-section top side class="task-item-menu"
                        v-if="showEdit">

            <div class="text-grey-8 q-gutter-xs">

                <template v-for="menuItem in itemMenu">

                    <template v-if="menuItem.label">

                        <q-btn v-if="menuItem.hidden !== true"
                               no-caps dense rounded unelevated
                               size="0.9em"
                               :label="menuItem.label"
                               :_flat="!itemMenuOutline(menuItem, item)"
                               :outline="itemMenuOutline(menuItem, item)"
                               :icon="itemMenuIcon(menuItem, item)"
                               :color="itemMenuColor(menuItem, item)"
                               :text-color="itemMenuTextColor(menuItem, item)"
                               @click="itemMenuClick(menuItem, item)"
                        />

                    </template>

                    <template v-else>

                        <q-btn v-if="!itemMenuHidden(menuItem, item)"
                               dense round
                               size="1.2em"
                               :flat="!itemMenuOutline(menuItem, item)"
                               :outline="itemMenuOutline(menuItem, item)"
                               :icon="itemMenuIcon(menuItem, item)"
                               :color="itemMenuColor(menuItem, item)"
                               @click="itemMenuClick(menuItem, item)"
                        />
                    </template>

                </template>

            </div>

        </q-item-section>


        <q-item-section top side v-if="showRatingDiff && showRating">


            <div v-if="showRatingDiff">

                <q-badge
                    v-if="item.ratingTaskDiff > 0"
                    class="rgm-bage statistic-type-rating"
                    color="white"
                    :label="'+' + item.ratingTaskDiff + ' ' + ratingText(item.ratingTaskDiff)"
                />
                <q-badge
                    v-if="item.ratingTaskDiff < 0"
                    class="rgm-bage result-rating-dec"
                    color="white"
                    :label="item.ratingTaskDiff + ' ' + ratingText(item.ratingTaskDiff)"
                />

            </div>


            <div v-if="showRating">

                <RaitingValue :item="item"/>

            </div>


        </q-item-section>


    </q-item>

</template>

<script>

import utils from "../utils"
import TaskValue from "./TaskValue"
import TaskAnswerResult from "./TaskAnswerResult"
import RaitingValue from "./RaitingValue"


/**
 * Слово в списке.
 */
export default {

    name: "TaskItem",

    components: {
        TaskValue, TaskAnswerResult, RaitingValue,
    },

    setup() {
        return {utils}
    },

    props: {
        item: Object,

        itemMenu: {type: Array, default: null},

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

        maskAnswerResult: {type: Boolean, default: true},

        showEdit: {type: Boolean},

        // todo определиться как показывать несколько вариантов ответов
        index: 0,
    },

    data() {
        return {
            //maskAnswerResult: false
        }
    },

    methods: {

        itemClick() {
            this.item.maskAnswerResult = !this.item.maskAnswerResult
            this.playQuestion()
        },

        playQuestion() {
            console.info("playQuestion")
        },

        ratingText(rating) {
            return utils.ratingText(rating)
        },

        getItemClass(item) {
            if (!item) {
                return ""
            }

            let classStr = ""
            if (item.isHidden) {
                //classStr = classStr + " item-is-hidden"

            } else if (item.isInPlan) {
                //classStr = classStr + " item-in-plan"
            }

            return classStr
        },

        itemMenuOutline(menuItem, item) {
            if (menuItem.outline) {
                if (menuItem.outline instanceof Function) {
                    return menuItem.outline(item)
                } else {
                    return menuItem.outline
                }
            } else {
                return false
            }

        },

        itemMenuIcon(menuItem, item) {
            if (menuItem.icon) {
                if (menuItem.icon instanceof Function) {
                    return menuItem.icon(item)
                } else {
                    return menuItem.icon
                }
            } else {
                return null
            }

        },

        itemMenuColor(menuItem, item) {
            if (menuItem.color) {
                if (menuItem.color instanceof Function) {
                    return menuItem.color(item)
                } else {
                    return menuItem.color
                }
            } else {
                return "primary"
            }

        },

        itemMenuTextColor(menuItem, item) {
            if (menuItem.textColor) {
                if (menuItem.textColor instanceof Function) {
                    return menuItem.textColor(item)
                } else {
                    return menuItem.textColor
                }
            } else {
                return "black"
            }

        },

        itemMenuHidden(menuItem, item) {
            if (menuItem.hidden) {
                if (menuItem.hidden instanceof Function) {
                    return menuItem.hidden(item)
                } else {
                    return menuItem.hidden
                }
            } else {
                return false
            }
        },

        itemMenuClick(menuItem, item) {
            if (menuItem.onClick) {
                menuItem.onClick(item)
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

</style>