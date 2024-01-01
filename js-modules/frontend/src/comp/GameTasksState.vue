<template>
    <div class="row" v-if="tasks">
        <template v-for="task in tasks">
            <div :class="classTask(task) + ' task-state'"></div>
        </template>
    </div>
</template>

<script>

/**
 * Состояние заданий в игре. Для виджета таскбара.
 */
export default {
    components: {},

    props: {
        tasks: {},
    },

    methods: {
        classTask(task) {
            if (task.wasTrue) {
                return "task-state__was-true"
            } else if (task.wasFalse) {
                return "task-state__was-false"
            } else if (task.wasHint) {
                return "task-state__was-hint"
            } else if (task.wasSkip) {
                return "task-state__was-skip"
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

.task-state {

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
