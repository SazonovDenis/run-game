<template>
    <q-btn color="white" text-color="black" label="Reset" v-on:click="resetTasks()"/>
    <q-btn color="white" text-color="black" label="Reset Goal" v-on:click="resetGoal()"/>

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
            },

            taskIdx: 1,
        }
    },

    methods: {
        changeGoalValue(v) {
            console.info("changeGoalValueY", v)
            console.info("this.goal.value: " + this.dataState.goal.value)
            if (this.dataState.goal.value == 0) {
                this.resetGoal()
                this.loadNextTask()
            }
        },

        loadNextTask() {
            this.taskIdx = this.taskIdx + 1;
            if (this.taskIdx >= data.tasks.length) {
                this.taskIdx = 0;
            }
            //
            let dataTask = data.tasks[this.taskIdx]

            //
            this.usrTask = dataTask

            //
            this.dataState.goal = {
                text: "Hit here " + (this.taskIdx + 1) + " !!!",
                value: 3,
            }

            //
        },

        resetGoal() {
            this.dataState.goal.value = 5
        },

        resetTasks() {
            this.taskIdx = -1;
            this.loadNextTask();
        },


    },

    async mounted() {
        //console.info("=== UserTaskPanel.test#created")

        //let res = await kisBase.daoApi.loadStore('m/Game/choiceTask', [1001])

        //let res = await kisBase.daoApi.invoke("m/Game/choiceTask", [1001])

        this.loadNextTask();
    }

}

</script>


<style scoped>

</style>