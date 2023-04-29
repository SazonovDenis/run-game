<template>
    <div>
        Вопрос и варианты ответов

        <div style="border: 1px solid green">
            <Task :task="usrTask.task"/>
        </div>

        <div style="border: 1px solid red">
            <TaskOptions :taskOptions="usrTask.taskOptions"/>
        </div>

        <q-btn color="white" text-color="black" label="Reset"/>

    </div>

</template>

<script>


import Task from "./Task"
import TaskOptions from "./TaskOptions"
import TouchPanelGame from "./TouchPanelGame"

export default {
    components: {TouchPanelGame, Task, TaskOptions},

    props: {
        usrTask: {
            task: null,
            taskOptions: null
        }
    },

    /*
        data() {
            return {

            }
        },
    */

    methods: {},

    computed: {}
}
</script>


<style>

.touch {
    _background-color: #e6ffda;
}

</style>
