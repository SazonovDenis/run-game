<template>
    <div>
        Вопрос и варианты ответов

        <div style="border: 1px solid green">
            <Task :task="usrTask.task"/>
        </div>

        <div style="border: 1px solid red">
            <TaskOptions :taskOptions="usrTask.taskOptions"/>
        </div>
    </div>

</template>

<script>


import Task from "./Task"
import TaskOptions from "./TaskOptions"

export default {
    components: {Task, TaskOptions},

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

