<template>

    <q-list v-if="tasks.length > 0" class="task-list">

        <template v-for="(taskItem, index) in tasks">

            <q-slide-item
                v-if="isItemShown(taskItem)"
                @left="onLeft($event, taskItem)"
                @right="onRight($event, taskItem)"
            >

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


                <TaskItem v-if="isItemShown(taskItem)"
                          :item="taskItem"
                          :showTaskData="showTaskData"
                          :showAnswerResult="showAnswerResult"
                          :showRating="showRating"
                          :showEdit="showEdit"
                          :itemMenu="itemsMenu"
                />


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
         class="q-pa-md rgm-state-text">
        {{ messageNoItems }}
    </div>


</template>

<script>

import TaskItem from "./TaskItem"

export default {

    name: "TaskList",

    components: {
        TaskItem
    },

    props: {
        tasks: null,

        itemsMenu: null,

        showTaskData: false,
        showAnswerResult: false,
        showRating: false,
        showEdit: false,

        showLastItemPadding: false,

        actionLeftSlide: null,
        actionRightSlide: null,
        filter: null,

        messageNoItems: {
            type: String,
            default: "Список пуст"
        },
    },

    methods: {


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

<style scoped>

.task-list {
    max-width: 50rem;
    margin: auto;
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