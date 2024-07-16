<template>
    <div
        :class="getClassAnswerResult(item) + ' task-state-state'">
    </div>
</template>

<script>

export default {
    name: "TaskAnswerResult",

    props: {
        item: Object,
    },

    methods: {

        getClassAnswerResult(task) {
            if (task.wasTrue) {
                return "was-true"
            } else if (task.wasFalse) {
                return "was-false"
            } else if (task.wasHint) {
                return "was-hint"
            } else if (task.wasSkip) {
                return "was-skip"
            } else if (task.dtTask && !task.dtAnswer) {
                return "was-skip"
            }
            return ""
        },

    },
}

</script>

<style scoped>

.was-true {
    background-color: #5b9e3a;
}

.was-false {
    background-color: #d61818;
}

.was-hint {
    background-color: #ffea1b;
}

.was-skip {
    background-color: #c8c8c8;
}

.task-state-state {
    border: 1px solid silver;
    border-radius: 1em;

    width: 1em;
    height: 1em;

    margin-top: 0.3em;
    margin-right: 0.3em;
}

</style>