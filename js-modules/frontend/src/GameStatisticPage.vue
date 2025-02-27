<template>

    <MenuContainer
        tabMenuName="StatisticPage"
        :title="title"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :loaded="dataLoaded"
    >

        <div v-if="!this.localState.game">
            <LogoGame/>
            <div class="message-no-data">Нет данных об игре</div>
        </div>


        <template v-if="localState.game">

            <div class="justify-center row q-mt-md">


                <!-- -->

                <div class="result-words">

                    <div class="row">

                        <StatisticWordsLearned :statistic="localState.statistic"/>

                        <StatisticWordsRepeated :statistic="localState.statistic"/>

                    </div>

                </div>


                <!-- -->

                <div class="result-words">

                    <div class="row q-mt-md">

                        <StatisticRatingDiff
                            class="q-mx-md q-my-sm q-mb-md"
                            :statistic="localState.statistic"/>

                    </div>

                </div>

            </div>

        </template>


        <template v-if="localState.game">

            <div class="row">

                <div class="q-ma-sm" v-if="!localState.game.done">
                    <jc-btn kind="primary"
                            label="Продолжить игру"
                            style="min-width: 12em;"
                            @click="continueActiveGame()">
                    </jc-btn>
                </div>

                <div class="q-ma-sm" v-if="!localState.game.done">
                    <jc-btn kind="secondary"
                            label="Выйти из игры"
                            style="min-width: 12em;"
                            @click="closeActiveGame()">
                    </jc-btn>
                </div>

                <div class="q-ma-sm"
                     v-if="localState.game.plan && localState.game.done">
                    <jc-btn kind="secondary"
                            label="Состав уровня"
                            style="min-width: 12em;"
                            @click="planTaskStatistic(localState.game.plan)">
                    </jc-btn>
                </div>

                <q-space/>

                <div class="q-ma-sm"
                     v-if="localState.game.plan && localState.game.done">
                    <jc-btn kind="primary"
                            label="Играть уровень"
                            style="min-width: 12em;"
                            @click="startNewGame()">
                    </jc-btn>
                </div>

            </div>

        </template>


        <TaskList
            v-if="localState.game && localState.game.done"
            :tasks="localState.tasks"
            :showTaskData="true"
            :showAnswerResult="true"
            :showRating="true"
        />


    </MenuContainer>

</template>

<script>

import {apx} from "./vendor"
import gameplay from "./gameplay"
import MenuContainer from "./comp/MenuContainer"
import StatisticRatingDiff from "./comp/StatisticRatingDiff"
import StatisticWordsRepeated from "./comp/StatisticWordsRepeated"
import StatisticWordsLearned from "./comp/StatisticWordsLearned"
import TaskList from "./comp/TaskList"
import LogoGame from "./comp/LogoGame"


export default {

    name: "GameStatisticPage",

    components: {
        MenuContainer, TaskList, LogoGame,
        StatisticRatingDiff, StatisticWordsRepeated, StatisticWordsLearned
    },

    props: {
        gameId: null,
        frameReturn: String,
        frameReturnProps: Object,
    },

    data() {
        return {
            dataLoaded: false,

            localState: {},
        }
    },

    computed: {
        title: function() {
            if (this.localState.game) {
                let displayFormat2 = "d MMMM yyyy, HH:MM"
                let dbeg = apx.date.toDateTime(this.localState.game.dbeg).toFormat(displayFormat2)
                return "Игра от " + dbeg
            } else if (!this.localState.game || !this.localState.game.id) {
                return null
            } else if (this.localState.game.done) {
                return null
            } else {
                return "Текушая игра"
            }
        }
    },

    methods: {
        async closeActiveGame() {
            // Закрываем последнюю игру
            await gameplay.closeActiveGame()

            // Загружаем последнюю игру, со статистикой
            this.localState = await gameplay.loadLastGame()
        },

        async continueActiveGame() {
            await gameplay.loadActiveGame()

            apx.showFrame({
                frame: '/game'
            })
        },

        async onSelectPlan() {
            await gameplay.closeActiveGame()

            apx.showFrame({
                frame: '/plans',
                props: {
                    title: "Выбор уровня",
                    onPlanClick: this.gameStart,
                    showEdit: false,
                }
            })
        },

        async gameStart(planId) {
            await gameplay.gameStart(planId)

            apx.showFrame({
                frame: '/game', props: {}
            })
        },

        planTaskStatistic(planId) {
            apx.showFrame({
                frame: '/plan', props: {planId: planId}
            })
        },

        async startNewGame() {
            await gameplay.gameStart(this.localState.game.plan)

            apx.showFrame({
                frame: '/game',
            })
        },
    },

    async mounted() {
        if (this.gameId) {
            // Загружаем игру по id
            this.localState = await gameplay.api_getGame(this.gameId)

        } else {
            // Загружаем текущую  игру
            this.localState = await gameplay.api_getActiveGame()

            // Есть текущая игра?
            if (!this.localState.game) {
                // Загружаем последнюю игру
                this.localState = await gameplay.api_getLastGame()
            }

            if (!this.localState.game) {
                //return
            }
        }

        /*
        // --- Подготовим возможность сортировки по порядку выдачи заданий, но с группировкой по фактам

        // Сделам "сортировочный" рейтинг одинаковым внутри группы "факт-вопрос"
        // Это для того, чтобы при сортировке по рейтингу члены группы двигались вместе
        let getDtTaskForSort = function(ratingTaskGroup, taskItem) {
            let dtTask = taskItem.dtTask
            if (!dtTask) {
                dtTask = "3333-12-31T00:00:00"
            }
            return "" +
                ratingTaskGroup + "_" +
                taskItem.factQuestion + "_" +
                taskItem.factAnswer + "_" +
                dtTask
        };
        //
        let compareFunction = function(v1, v2) {
            // По умолчанию сортируем по коду факта-вопроса, а потом по рейтингу
            if (v1.factQuestion > v2.factQuestion) {
                return 1
            } else if (v1.factQuestion < v2.factQuestion) {
                return -1
            } else if (v1.factQuestion === v2.factQuestion && v1.dtTaskForSort > v2.dtTaskForSort) {
                return 1
            } else if (v1.factQuestion === v2.factQuestion && v1.dtTaskForSort < v2.dtTaskForSort) {
                return -1
            } else {
                return 0
            }
        }
        //
        let dtTaskGroup
        for (let i = 0; i < this.localState.tasks.length; i++) {
            let task = this.localState.tasks[i]

            if (i === 0 || task.factQuestion !== this.localState.tasks[i - 1].factQuestion) {
                dtTaskGroup = task.dtTask
            }

            task.dtTaskForSort = getDtTaskForSort(dtTaskGroup, task)
        }

        this.localState.tasks.sort(compareFunction)

        // --- Сортировка по умолчанию
        //this.sortField = "question"
        */


        //
        this.dataLoaded = true
    },


}

</script>

<style lang="less" scoped>

.message-no-data {
    text-align: center;
    color: #404040;
    font-size: 2em;
}

hr {
    margin: 1em 0;
}

</style>