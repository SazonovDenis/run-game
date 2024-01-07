<template>
    <div class="col" v-if="gameTasks">
        <template v-for="task in gameTasks">
            <div class="row">
                <div :class="classTask(task) + ' task-state-state'"></div>

                <GameTask :task="task"/>

                <div v-if="task.ratingInc > 0"
                     class="task-text task-text__ratingInc">
                    +{{ task.ratingInc }}
                </div>

                <div v-if="task.ratingDec < 0"
                     class="task-text task-text__ratingDec">
                    {{ task.ratingDec }}
                </div>
            </div>
        </template>
    </div>
</template>

<script>

import GameTask from "./GameTask"

/**
 * Состояние заданий в игре (в виде закрашенных кружков)
 */
export default {
    components: {GameTask},

    props: {
        gameTasks: {},
    },

    methods: {
        classTask(task) {
            if (task.wasTrue) {
                return "task-text__was-true"
            } else if (task.wasFalse) {
                return "task-text__was-false"
            } else if (task.wasHint) {
                return "task-text__was-hint"
            } else if (task.wasSkip) {
                return "task-text__was-skip"
            }
            return ""
        },
    },

    data() {
        return {}
    },

}
</script>


<style lang="less" scoped>

.task-state-state {
    border: 1px solid silver;
    border-radius: 1em;

    width: 1em;
    height: 1em;

    margin-top: 0.3em;
    margin-right: 0.3em;
}

.task-text {

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
