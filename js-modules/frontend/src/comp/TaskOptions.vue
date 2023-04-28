<template>
    <div>
        Варианты ответов
    </div>
    <div v-for="taskOption in taskOptions">
        <TaskOption :taskOption="taskOption"/>
    </div>
</template>

<script>

import TaskOption from "./TaskOption"

export default {
    components: {TaskOption},

    props: {
        taskOptions: null
    },

    methods: {},

    computed: {}
}
</script>

