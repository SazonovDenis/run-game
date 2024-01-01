<template>
    <div v-if="game.id" class="game-info">
        <div class="game-info__text">{{ game.planText }}</div>

        <q-separator/>

        <div class="">
            <span>Игра начата {{ dbeg }}</span>
            <span v-if="game.done">, завершена {{ dend }}</span>
        </div>

        <div class="game-info__count">
            Показано заданий: {{ game.countAsked }} из {{ game.countTask }}
        </div>

        <q-separator/>

        <div>Игра</div>

        <div>Ответов: {{ game.countAnswered }}</div>
        <div>Верных ответов: {{ game.countTrue }}</div>
        <div>Ошибок: {{ game.countFalse }}</div>
        <div>Подсказок: {{ game.countHint }}</div>
        <div>Пропусков: {{ game.countSkip }}</div>

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