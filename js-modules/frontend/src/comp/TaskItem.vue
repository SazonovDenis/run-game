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

            <q-item-label :class="'row rgm-task-question ' + getItemClass(item)"
                          v-if="isFactFirstAnswer(item, index)">

                <TaskValue v-if="showTaskData"
                           :task="item.taskQuestion"
                           :doShowText="true"
                           ref="taskValue"
                />

                <TaskValue v-else
                           :task="item.question"
                           :doShowText="true"
                           ref="taskValue"
                />

            </q-item-label>


            <q-item-label class="row">

                <template v-if="maskAnswer === true && item.maskAnswer">

                    <div class="rgm-task-answer">
                        * * * * * * * *
                    </div>

                </template>

                <template v-else>

                    <TaskValue v-if="showTaskData"
                               class="rgm-task-answer"
                               :task="item.taskAnswer"
                               :doShowText="true"/>

                    <TaskValue v-else
                               class="rgm-task-answer"
                               :task="item.answer"
                               :doShowText="true"/>

                </template>


            </q-item-label>

        </q-item-section>


        <q-item-section top side class="task-item-menu"
                        v-if="showEdit">

            <div class="text-grey-8 q-gutter-xs">

                <template v-for="menuItem in itemMenu">

                    <ItemMenuItem :item="item" :menuItem="menuItem"/>

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
import ItemMenuItem from "./ItemMenuItem"


/**
 * Слово в списке.
 */
export default {

    name: "TaskItem",

    components: {
        ItemMenuItem, TaskValue, TaskAnswerResult, RaitingValue,
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

        /**
         * Режим сокрытия ответов (для тренировки)
         */
        maskAnswer: {type: Boolean, default: false},

        showEdit: {type: Boolean},

        // todo определиться как показывать несколько вариантов ответов
        index: 0,
    },

    data() {
        return {}
    },

    methods: {

        itemClick() {
            if (this.maskAnswer) {
                this.item.maskAnswer = !this.item.maskAnswer
            }

            // Приграем звук
            if (!this.maskAnswer || !this.item.maskAnswer) {
                this.playQuestion()
            }
        },

        /*
                findItemFull(item) {
                    console.info("findItemFull: " + item)
                },
        */

        playQuestion() {
            this.$refs.taskValue.play()
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

.item-is-hidden {
    color: rgba(20, 20, 150, 0.8);
}

.item-in-plan {
    _color: rgba(50, 100, 50, .9);
}

</style>