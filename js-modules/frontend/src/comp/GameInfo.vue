<template>
    <div v-if="game" class="game-info">


        <div class="game-info__duration">
            <span>Игра начата {{ dbeg }}</span>
            <span v-if="game.done">, завершена {{ dend }}</span>
        </div>

        <div class="game-info__count" style="padding-top: 0.5em">
            <span class="">
                За игру заработано:
            </span>
            <span class="game-info__rating-inc">
                {{ statistic.ratingTaskInc }}
                {{ ratingText(statistic.ratingTaskInc) }}
            </span>
            <span class="">, потеряно:&nbsp;</span>
            <span class="game-info__rating-dec">
                    {{
                    -statistic.ratingTaskDec
                }}&nbsp;{{ ratingText(-statistic.ratingTaskDec) }}
            </span>
        </div>

    </div>

    <div v-else class="game-info">
        <LogoGame/>
        <div class="game-info__no-data">Нет данных об игре</div>
    </div>

</template>

<script>

import {apx} from '../vendor'
import LogoGame from "./LogoGame"
import utils from "../utils"


/**
 * Состояние игры. Виджет для экрана
 */
export default {

    name: "GameInfo",

    components: {
        LogoGame,
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
            return utils.ratingText(rating)
        }
    }

}

</script>

<style lang="less" scoped>


.game-info {
    font-size: 1.5em;
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

    &__duration {
        color: #6c6c6c;
    }

}

</style>