<template>
    <div class="options" id="options">
<!--
        Варианты ответов
-->

        <div v-for="taskOption in taskOptions">
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

    },

    computed: {}
}
</script>


<style>

.options {
    user-select: none;
    touch-action: none;
}

</style>