<template>
    <div class="row" v-if="tasksResult">
        <template v-for="taskResult in tasksResult">
            <div :class="classTask(taskResult) + ' task-result'"></div>
        </template>
    </div>
</template>

<script>

/**
 * Состояние заданий в игре (в виде закрашенных кружков)
 */
export default {
    components: {},

    props: {
        tasksResult: {},
    },

    methods: {
        classTask(taskResult) {
            if (taskResult.wasTrue) {
                return "task-result__was-true"
            } else if (taskResult.wasFalse) {
                return "task-result__was-false"
            } else if (taskResult.wasHint) {
                return "task-result__was-hint"
            } else if (taskResult.wasSkip) {
                return "task-result__was-skip"
            }
            return ""
        },
    },

    data() {
        return {}
    },

}
</script>


<style lang="less">

.task-result {

    border: 1px solid silver;
    width: 1em;
    height: 1em;
    margin: 0.1em;
    border-radius: 1em;

    &__was-true {
        background-color: #5b9e3a;
    }

    &__was-false {
        background-color: #d61818;
    }

    &__was-hint {
        background-color: #ffea1b;
    }

    &__was-skip {
        background-color: #c8c8c8;
    }

}


</style>
