<template>
    <div v-if="game.id" class="game-info">
        <div class="game-info__text">{{ game.planText }}</div>


        <div class="game-info__count" style="padding-top: 0.5em">
            Всего баллов: {{ statistic.rating0 }} из {{ statistic.ratingMax }}
        </div>
        <div class="game-info__count">
            Баллы за скорость: {{ statistic.ratingQuickness0 }}
        </div>

        <q-separator/>

        <div class="game-info__duration">
            <span>Игра начата {{ dbeg }}</span>
            <span v-if="game.done">, завершена {{ dend }}</span>
        </div>

        <div class="game-info__count" style="padding-top: 0.5em">
            <span class="">
                За игру заработано:
            </span>
            <span class="game-info__ratingInc">
                {{ statistic.ratingInc }}
                {{ ratingText(statistic.ratingInc) }}
            </span>
            <span class="">, потеряно:&nbsp;</span>
            <span class="game-info__ratingDec">{{ statistic.ratingDec }}&nbsp;{{
                    ratingText(statistic.ratingDec)
                }}
            </span>
        </div>

        <div class="game-tasks row" style="padding-top: 0.5em">
            <GameTasks :gameTasks="gameTasks"/>
        </div>

    </div>
    <div v-else class="game-info">
        <LogoGame/>
        <div class="no-data-text">Нет данных об игре</div>
    </div>

</template>

<script>

import {apx} from '../vendor'
import GameTasks from "./GameTasks"
import LogoGame from "./LogoGame"


/**
 * Состояние игры. Виджет для экрана
 */
export default {

    name: "GameInfo",

    components: {
        GameTasks,
        LogoGame,
    },

    props: {
        game: {},
        gameTasks: {},
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
            let r = rating % 10
            if (r === 1) {
                return "балл"
            } else if (r === 2 || r === 3 || r === 4) {
                return "балла"
            } else {
                return "баллов"
            }
        }
    }
}

</script>

<style lang="less" scoped>

.game-tasks {
    justify-content: center;
}

hr {
    margin: 1em 0;
}

.no-data-text {
    margin: 1em;
    font-size: 1em;
    color: #6c6c6c;
}

.game-info {
    font-size: 1.5em;
    text-align: center;

    &__text {
        font-size: 1.5em;
        color: #34558b;
    }

    &__duration {
        color: #6c6c6c;
    }

    &__count {
        color: #4c4c4c;
    }

    &__ratingDec {
        color: #850000;
    }

    &__ratingInc {
        color: #5b9e3a;
    }
}

</style>