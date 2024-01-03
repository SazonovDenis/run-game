<template>
    <div v-if="game.id" class="game-info">
        <div class="game-info__text">{{ game.planText }}</div>

        <div class="game-info__duration">
            <span>Игра начата {{ dbeg }}</span>
            <span v-if="game.done">, завершена {{ dend }}</span>
        </div>

        <q-separator/>

        <div class="game-info__count">
            Общий итог: {{ game.statistic.rating0 }} из {{ game.statistic.ratingMax }}
        </div>
        <div class="game-info__count">
            Баллы за скорость: {{ game.statistic.ratingQuickness0 }}
        </div>

        <q-separator/>

        <div>Игра</div>

        <div class="game-info__count game-info__ratingInc ">
            Заработано: {{ game.statistic.ratingInc }}
        </div>
        
        <div class="game-info__count game-info__ratingDec ">
            Потеряно: {{ game.statistic.ratingDec }}
        </div>

        <div class="game-tasks row">
            <GameTasks :tasks="game.tasks"/>
        </div>

        <q-separator/>

        <div>План</div>

        <div class="game-tasks row">
            <TasksStatistic :tasksStatistic="game.tasksStatistic"/>
        </div>

    </div>

    <div v-else class="game-info xxx-yyy">Нет текущей игры</div>
</template>

<script>

import {apx} from '../vendor'
import GameTasks from "./GameTasksState"
import TasksStatistic from "./TasksStatistic"


/**
 * Состояние игры. Виджет для экрана
 */
export default {

    name: "GameInfo",

    components: {
        GameTasks, TasksStatistic
    },

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

<style lang="less" scoped>

.game-tasks {
    justify-content: center;
}

hr {
    margin: 1em 0;
}

.game-info {
    font-size: 1.5em;
    text-align: center;
    margin: 0.5rem;

    &__text {
        font-size: 2em;
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