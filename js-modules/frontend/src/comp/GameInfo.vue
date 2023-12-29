<template>
    <div v-if="game.id" class="game-info">
        <div class="game-info__text">{{ game.text }}</div>

        <q-separator/>

        <div class="">Игра начата {{ dbeg }}</div>
        <div v-if="game.done" class="">Игра завершена {{ dend }}</div>

        <q-separator/>

        <div class="game-info__count">
            Показано заданий: {{ game.countAsked }} из {{ game.countTask }}
        </div>

        <q-separator/>

        <div>Количество ответов: {{ game.countAnswered }}</div>
        <div>Количество верных ответов: {{ game.countTrue }}</div>
        <div>Количество ошибок: {{ game.countFalse }}</div>
        <div>Количество подсказок: {{ game.countHint }}</div>
        <div>Количество пропусков: {{ game.countSkip }}</div>

    </div>
    <div v-else class="game-info xxx-yyy">Нет текущей игры</div>
</template>

<script>

import {apx} from '../vendor'


/**
 * Состояние игры. Виджет для экрана
 */
export default {
    name: "GameInfo",

    props: {
        game: null
    },

    computed: {
        dbeg: function() {
            return apx.date.toDisplayStr(this.game.dbeg)
        },
        dend: function() {
            return apx.date.toDisplayStr(this.game.dend)
        },
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