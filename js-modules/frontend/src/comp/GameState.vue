<template>
    <div class="game-state">
        <span class="game-state__game-text">{{ text }}</span>
        <span class="game-state__info">{{ info }}</span>
    </div>
</template>

<script>

/**
 * Состояние игры. Виджет для таскбара
 */
export default {
    components: {},

    props: {
        game: {},
    },

    computed: {
        text() {
            if (this.game.id > 0) {
                return this.game.text
            } else {
                return "Игра не начата"
            }

        },
        info() {
            if (this.game.id > 0) {
                return this.game.countDone + "/" + this.game.countTotal
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


<style lang="less">

.game-state {
    padding: 0.5em;

    span {
        margin: 0.5em;
    }

    &__text {
        font-size: 80%;
        color: #850000;
    }

    &__info {
        color: #5b9e3a;
    }
}


</style>
