<template>
    <q-btn color="white" text-color="black" label="Next" v-on:click="test_nextTask()"/>

    <UserTaskPanel :usrTask="usrTask" :dataState="dataState"
                   v-on:changeGoalValue="changeGoalValue"/>

</template>


<script>

import UserTaskPanel from "./UserTaskPanel"
import data from "../data"

export default {
    name: "MainWindow",

    components: {
        UserTaskPanel,
    },

    // Состояние игрового мира
    data() {
        return {
            usrTask: {
                task: {},
                taskOptions: {}
            },

            dataState: {
                drag: {
                    dtStart: null,
                    dtStop: null,
                    drag: null,
                    x: null,
                    y: null,
                    sx: null,
                    sy: null,
                },
                goal: {
                    text: null,
                    value: null,
                },
                ball: {
                    text: null,
                    value: 0,
                },
            },

            taskIdx: 1,
        }
    },

    methods: {
        changeGoalValue(v) {
            console.info("changeGoalValue", v)
            console.info("this.goal.value: " + this.dataState.goal.value)
            if (this.dataState.goal.value == 0) {
                // Новое задание
                this.loadNextTask()
                // Новая цель
                this.resetGoalValue()
                this.setGoalInfo()
            }
        },

        loadNextTask() {
            // Грузим новое задание с сервера
            this.taskIdx = this.taskIdx + 1;
            if (this.taskIdx >= data.tasks.length) {
                this.taskIdx = 0;
            }
            //
            let dataTask = data.tasks[this.taskIdx]


            // Присваиваем данные задания себе
            this.usrTask = dataTask

            //
            this.setGoalInfo()
        },

        setGoalInfo() {
            this.dataState.goal.text = "Hit here #" + (this.taskIdx + 1) + ", " + this.usrTask.task.text
        },

        // Сбрасываем состояние результата (цели)
        resetGoalValue() {
            this.dataState.goal.value = 4
        },

        test_resetTasks() {
            this.taskIdx = -1;
            this.loadNextTask();
        },

        test_nextTask() {
            this.loadNextTask();
        },


    },

    async mounted() {
        //console.info("=== UserTaskPanel.test#created")

        //let res = await kisBase.daoApi.loadStore('m/Game/choiceTask', [1001])

        //let res = await kisBase.daoApi.invoke("m/Game/choiceTask", [1001])

        this.taskIdx = -1;
        this.loadNextTask();
        this.resetGoalValue();
        this.setGoalInfo()
    }

}

</script>


<style scoped>

</style>