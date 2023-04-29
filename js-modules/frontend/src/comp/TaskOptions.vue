<template>
    <div class="options">
        Варианты ответов

        <div id="ball" class="ball">&nbsp;</div>
        <div id="goal" class="goal">Попади</div>

        <div v-for="taskOption in taskOptions">
            <TaskOption :taskOption="taskOption" :animationInterval="20"/>
        </div>

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

.options {
    user-select: none;
    touch-action: none;
}

.ball {
    position: absolute;
    width: 2em;
    height: 2em;
    border-radius: 1em;
    background-color: rgba(100, 200, 0, 0.3);
    z-index: 1000;
    display: none;
}

.goal {
    position: absolute;
    width: 10em;
    height: 10em;
    bottom: 50px;
    left: 30%;
    border-radius: 1em;
    background-color: rgba(0, 143, 200, 0.3);
    z-index: 1000;
    display: block;
}

</style>