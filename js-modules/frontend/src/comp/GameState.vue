<template>
    <div class="game-state row">
        <span class="game-state__planText">{{ planText }}</span>
        <GameTasks :tasks="game.tasks"/>
        <!--
                <span class="game-state__gameStep">{{ gameStep }}</span>
        -->
    </div>
</template>

<script>

import GameTasks from "./GameTasks"

/**
 * Состояние игры. Виджет для таскбара.
 */
export default {

    components: {GameTasks},

    props: {
        game: {},
    },

    computed: {
        planText() {
            if (this.game.id > 0) {
                return this.game.planText
            } else {
                return "Игра не начата"
            }

        },
        gameStep() {
            if (this.game.id > 0) {
                return this.game.countAsked + " из " + this.game.countTask
            } else {
                return ""
            }

        },
    },

    data() {
        return {}
    },

}
</script>


<style lang="less" scoped>

.game-state {

    padding: 0.5em;
    align-items: center;

    span {
        margin: 0.5em;
    }

    &__planText {
        font-size: 80%;
        color: silver;
    }

    &__gameStep {
        color: #5b9e3a;
    }

}


</style>
