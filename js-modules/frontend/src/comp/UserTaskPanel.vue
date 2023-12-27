<template>
    <div class="user-task">
        <q-btn color="white" text-color="black" label="Skip task"
               v-on:click="nextTask"/>

        <div v-if="doShowTask">
            <Task :task="gameTask.task" :state="dataState"/>
        </div>


        <Goal v-if="doShowTask" class="goal" id="goal" :goal="dataState.goal"/>


        <div class="game-field">&nbsp;</div>


        <div v-if="doShowTask" class="task-options">
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
import gameplay from "../../src/gameplay"

export default {
    components: {gameplay, Ball, Goal, Task, TaskOptions},

    props: {
        gameTask: {},
        dataState: {},
    },

    methods: {
        nextTask() {
            gameplay.loadNextTask()
        },
    },

    computed: {

        doShowTask() {
            return (
                this.gameTask.task &&
                this.gameTask.task.id
            )
        },

    }
}
</script>


<style>

.user-task {
    _position: relative;
    display: block;
}

.game-field {
    _border: 1px dotted red;
    height: 40vh;
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
