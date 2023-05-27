<template>
    <div class="options" id="options">
        <div v-for="taskOption in taskOptions" v-bind:class="getClassName(taskOption)">
            <TaskOption :taskOption="taskOption" :state="state"/>
        </div>

    </div>
</template>

<script>

import TaskOption from "./TaskOption"

export default {
    components: {TaskOption},

    props: {
        taskOptions: null,
        state: null,
    },

    methods: {
        getClassName(taskOption) {
            if (this.state.mode.modeShowOptions == "hint-true" && taskOption.isTrue) {
                return "fact-true"
            }
        },
    },

    computed: {},

}
</script>


<style>

.options {
    user-select: none;
    touch-action: none;
}

.fact-true {
    border: 2px solid green;
}

</style>