<template>
    <div>
        <div>
            Вопрос и варианты ответов
        </div>

        <div style="border: 1px solid green">
            <Task :task="usrTask.task"/>
        </div>

        <div style="border: 1px solid red; bottom: 10px">
            <TaskOptions :taskOptions="usrTask.taskOptions" :state="dataState"
                         v-on:changeGoalValue="this.$emit('changeGoalValue')"/>
        </div>


        <Ball id="ball" :drag="dataState.drag"/>

        <Goal id="goal" :goal="dataState.goal"/>

    </div>

</template>

<script>


import Ball from "./Ball"
import Goal from "./Goal"
import Task from "./Task"
import TaskOptions from "./TaskOptions"

export default {
    components: {Ball, Goal, Task, TaskOptions},

    props: {
        usrTask: {},
        dataState: {},
    },

    /*
        created() {
            this.dataState = data
        },
    */

    mounted() {
        //console.info("=== UserTaskPanel.mounted")
        //console.info("dataState: " + this.dataState)
        //console.info("dataState.drag: " + this.dataState.drag)
        //console.info("dataState.goal: " + this.dataState.goal)
        //console.info()
    },

    methods: {},

    computed: {}
}
</script>


<style>

</style>
