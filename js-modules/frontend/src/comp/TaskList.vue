<template>

    <q-list v-if="tasks.length > 0">
        <template v-for="(taskItem, index) in tasks">

            <q-slide-item @left="onLeft($event, taskItem)"
                          @right="onRight($event, taskItem)">

                <template v-slot:left
                          v-if="actionLeftSlide && actionLeftSlide.hidden !== true">
                    <div class="row">
                        <div v-if="actionLeftSlide.icon">
                            <q-icon left
                                    :name="itemMenuIcon(actionLeftSlide, taskItem)"/>
                        </div>
                        <div :class="!actionLeftSlide.info?'slide-left-no-info':''">
                            <div class="slide-label">
                                {{ actionLeftSlide.label }}
                            </div>
                            <div class="slide-info" v-if="actionLeftSlide.info">
                                {{ actionLeftSlide.info }}
                            </div>
                        </div>
                    </div>
                </template>

                <template v-slot:right
                          v-if="actionRightSlide && actionRightSlide.hidden !== true">
                    <div class="slide-label">
                        {{ actionRightSlide.label }}
                        <q-icon v-if="actionRightSlide.icon"
                                :name="itemMenuIcon(actionRightSlide, taskItem)"/>
                    </div>
                    <div class="slide-info" v-if="actionRightSlide.info">
                        {{ actionRightSlide.info }}
                    </div>
                </template>


                <q-item v-if="isItemShown(taskItem)"
                        :class="getClassItemRow(taskItem, index)"
                        clickable v-ripple>

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

            </q-slide-item>

        </template>


        <!--
        Элемент для последнего "пустого" элемента.
        Чтобы кнопки списка не загораживали последнюю строку
        -->
        <q-item v-if="showLastItemPadding">
            <div style="height: 2em">&nbsp;</div>
        </q-item>


    </q-list>


    <div v-else
         class="rgm-state-text">
        {{ messageNoItems }}
    </div>


</template>

<script>

import TaskValue from "./TaskValue"

export default {

    name: "TaskList",

    components: {
        TaskValue
    },

    props: {
        /**
         * Какое тело вопроса и ответа показывать.
         * Если showTaskData == true, то показывать подготовленные задания вопрос и ответ
         * (вопрос и ответ из Task<-TaskOption и Task<-TaskQuestion), что полезно при просмотре списка заданий в игре.
         * Если showTaskData == false, то показывать значения ФАКТОВ задания
         * (данные из Task.factQuestion->Fact.* и Task.factQuestion->Fact.*),
         * что полезно при просмотре списка фактов в плане.
         */
        showTaskData: false,

        showLastItemPadding: false,

        messageNoItems: {
            type: String,
            default: "Список пуст"
        },

        showAnswerResult: false,
        showEdit: false,
        tasks: null,
        itemsMenu: null,
        actionLeftSlide: null,
        actionRightSlide: null,
        filter: null,
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

        onLeft(event, taskItem) {
            event.reset()
            if (this.actionLeftSlide && this.actionLeftSlide.hidden !== true) {
                this.actionLeftSlide.onClick(taskItem)
            }
        },

        onRight(event, taskItem) {
            event.reset()
            if (this.actionRightSlide && this.actionLeftSlide.hidden !== true) {
                this.actionRightSlide.onClick(taskItem)
            }
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

        isItemShown(item) {
            if (!this.filter) {
                return true
            } else {
                return this.filter(item)
            }
        },

    },


}


</script>

<style>

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

.slide-label {
    font-size: 110%;
}

.slide-left-no-info {
    align-items: center;
    display: flex;
}

.slide-info {
    font-size: 70%;
}

</style>