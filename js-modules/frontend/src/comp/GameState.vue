<template>
    <div class="game-state row">
        <span class="game-state__planText">{{ planText }}</span>
        <GameTasksState :tasksResult="tasksResult"/>
    </div>
</template>

<script>

import GameTasksState from "./GameTasksState"

/**
 * Состояние заданий в игре.
 * Виджет для таскбара.
 */
export default {

    components: {
        GameTasksState
    },

    props: {
        game: {},
        tasksResult: {},
    },

    computed: {
        planText() {
            if (this.game && this.game.id > 0) {
                return this.game.planText
            } else {
                return "Игра не начата"
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
