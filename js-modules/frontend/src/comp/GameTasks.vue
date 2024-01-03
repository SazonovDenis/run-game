<template>
    <div class="col" v-if="tasks">
        <template v-for="task in tasks">
            <div class="row" v-if="tasks">
                <div :class="classTask(task) + ' task-state-state'"></div>
                <div class="task-state-text">
                    {{ task.factQuestionValue }} &ndash;
                    {{ task.factAnswerValue }}
                </div>
                <div v-if="task.ratingInc > 0"
                     class="task-state-text task-state-text__ratingInc">
                    +{{ task.ratingInc }}
                </div>
                <div v-if="task.ratingDec < 0"
                     class="task-state-text task-state-text__ratingDec">
                    {{ task.ratingDec }}
                </div>
            </div>
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
        tasks: {},
    },

    methods: {
        classTask(task) {
            if (task.wasTrue) {
                return "task-state-text__was-true"
            } else if (task.wasFalse) {
                return "task-state-text__was-false"
            } else if (task.wasHint) {
                return "task-state-text__was-hint"
            } else if (task.wasSkip) {
                return "task-state-text__was-skip"
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

.task-state-state {
    border: 1px solid silver;
    border-radius: 1em;

    width: 1em;
    height: 1em;

    margin-top: 0.3em;
    margin-right: 0.3em;
}

.task-state-text {

    margin: 0.2em 0.2em;
    max-width: 80%;
    text-align: left;

    &__ratingDec {
        color: #850000;
    }

    &__ratingInc {
        color: #5b9e3a;
    }

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
