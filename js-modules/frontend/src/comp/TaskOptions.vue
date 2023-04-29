<template>
    <div>
        Варианты ответов
    </div>

    <div id="ball" class="ball">X</div>

    <div v-for="taskOption in taskOptions">
        <TaskOption :taskOption="taskOption" :animationInterval="20"/>
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


<style>

.ball {
    position: absolute;
    width: 2em;
    height: 2em;
    border-radius: 1em;
    background-color: rgba(200, 0, 0, 0.3);
    z-index: 1000;
    display: none;
}

</style>