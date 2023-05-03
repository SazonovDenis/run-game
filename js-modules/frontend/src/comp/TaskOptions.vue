<template>
    <div class="options" id="options">
        <!--
                Варианты ответов
        -->

        <div v-for="taskOption in taskOptions" v-bind:class="getClassName(taskOption)">
            <TaskOption :taskOption="taskOption" :state="state"
                        :animationInterval="10"
                        v-on:changeGoalValue="this.$emit('changeGoalValue')"
            />
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
        changeGoalValueX(v) {
            console.info("changeGoalValue X", v)
            this.$emit('changeGoalValue', v)
        },

        getClassName(taskOption) {
            if (this.state.game.modeShowOptions == "hint-true" && taskOption.trueFact) {
                return "fact-true"
            }
        },
    },

    computed: {}
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