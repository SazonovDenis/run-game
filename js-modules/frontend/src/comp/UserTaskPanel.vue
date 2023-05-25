<template>
    <div class="user-task">

        <div>
            <Task :task="gameTask.task" :state="dataState"/>
        </div>


        <Goal class="goal" id="goal" :goal="dataState.goal"/>


        <div class="game-field">&nbsp;</div>


        <div class="task-options">
            <TaskOptions :taskOptions="gameTask.taskOptions" :state="dataState"/>
        </div>


        <Ball class="ball" id="ball" :drag="dataState.drag" :ball="dataState.ball"/>

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
        gameTask: {},
        dataState: {},
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
    _border: 1px dotted red;
    height: 100px;
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
