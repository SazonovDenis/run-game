<template>
    <div v-if="game.id" class="game-info">
        <div class="game-info__text">{{ game.text }}</div>
        <div class="game-info__count">Пройдено заданий: {{ game.countDone }} из
            {{ game.countTotal }}
        </div>
        <div class="">Игра начата {{ game.dbeg }}</div>
        <div v-if="game.done" class="">Игра завершена {{ game.dend }}</div>
    </div>
    <div v-else class="game-info xxx-yyy">Нет текущей игры</div>
</template>

<script>

/**
 * Состояние игры. Виджет для экрана
 */
export default {
    name: "GameInfo",

    props: {
        game: null
    }
}

</script>

<style lang="less">

.game-info {
    font-size: 1.5em;
    text-align: center;
    margin: 0.5rem;

    &__text {
        font-size: 2em;
        color: #850000;
    }

    &__count {
        color: #5b9e3a;
    }
}

</style>