<template>
    <div v-if="game" class="game-info">


        <div class="game-info__duration">
            <span>Игра начата {{ dbeg }}</span>
<!--
            <span v-if="game.done">, завершена {{ dend }}</span>
-->
        </div>


        <!-- -->

        <div class="result-words-header">
            Слова
        </div>

        <div class="row">
            <template v-if="statistic.wordCountLearned != null">
                <q-space/>

                <StatisticWordsLearned :rating="statistic"/>

            </template>

            <template v-if="statistic.wordCountRepeated != null">

                <StatisticWordsRepeated :rating="statistic"/>

                <q-space/>
            </template>
        </div>


        <!-- -->

        <div class="q-mt-sm result-rating-header">
            Баллы
        </div>

        <div class="row">
            <q-space/>

            <StatisticRating :rating="statistic"/>

            <q-space/>
        </div>

        <!-- -->

    </div>

    <div v-else class="game-info">
        <LogoGame/>
        <div class="game-info__no-data">Нет данных об игре</div>
    </div>

</template>

<script>

import {apx} from '../vendor'
import utils from "../utils"
import LogoGame from "./LogoGame"
import StatisticRating from "./StatisticRating"
import StatisticWordsRepeated from "./StatisticWordsRepeated"
import StatisticWordsLearned from "./StatisticWordsLearned"


/**
 * Состояние игры. Виджет для экрана
 */
export default {

    name: "GameStatistic",

    components: {
        StatisticRating, StatisticWordsRepeated, StatisticWordsLearned, LogoGame,
    },

    props: {
        game: {},
        statistic: {},
    },

    computed: {
        dbeg: function() {
            return apx.date.toDisplayStr(this.game.dbeg)
        },
        dend: function() {
            return apx.date.toDisplayStr(this.game.dend)
        },
    },

    methods: {
        ratingText(rating) {
            return rating + "&nbsp;" + utils.ratingText(rating)
        }
    }

}

</script>

<style lang="less" scoped>


.game-info {
    font-size: 2em;
    text-align: center;
    margin: 0 2rem;

    &__no-data {
        margin: 1em;
        font-size: 1em;
        color: #6c6c6c;
    }

    &__text {
        font-size: 1.5em;
        color: #34558b;
    }

    &__count {
        padding-top: 0.5em
    }

    &__duration {
        color: #6c6c6c;
    }

}

</style>