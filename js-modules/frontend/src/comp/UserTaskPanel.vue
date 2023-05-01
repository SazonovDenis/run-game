<template>
    <div class="user-task">
        <div>
            Вопрос и варианты ответов
        </div>

        <div style="border: 1px solid green">
            <Task :task="usrTask.task"/>
        </div>

        <Goal class="goal" id="goal" :goal="dataState.goal"/>

        <div class="game-field">-</div>

        <div style="border: 1px solid red; bottom: 10px" class="task-options">
            <TaskOptions :taskOptions="usrTask.taskOptions" :state="dataState"
                         v-on:changeGoalValue="this.$emit('changeGoalValue')"/>
        </div>

        <Ball class="ball" id="ball" :drag="dataState.drag"/>

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

.user-task {
    _position: relative;
    display: block;
}

.game-field {
    height: 200px;
}

.task-options {
    _position: absolute;
    bottom: 0;
    width: 100%;
}

.goal {
    _position: relative;
    _bottom: 50px;
    left: 40%;
}

.ball {
    position: absolute;
}


</style>
