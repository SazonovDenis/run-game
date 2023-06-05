<template>
    <div class="options" id="options">
        <div v-for="taskOption in taskOptions">
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
            if (this.state.mode.modeShowOptions == "hint-true") {
                if (taskOption.isTrue) {
                    return "fact-true"
                } else {
                    return "fact-false"
                }
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

    display: flex;
    flex-wrap: wrap;
}

</style>