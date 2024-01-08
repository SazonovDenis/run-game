<template>

    <MenuContainer :title="title">

        <div v-if="dataLoaded" class="col justify-center q-mt-lg q-mb-lg11 q-gutter-md">
            <PlanInfo :planText="localState.game.planText"
                      :rating="localState.statistic.rating0"
                      :ratingQuickness="localState.statistic.ratingQuickness0"
                      :ratingMax="localState.statistic.ratingMax"
            />

            <q-separator/>

            <GameInfo
                :game="localState.game"
                :gameTasks="localState.gameTasks"
                :statistic="localState.statistic"
            />


            <template v-if="localState.game">

                <div class="row">


                    <div class="q-ma-sm" v-if="!localState.game.done">
                        <jc-btn kind="primary" label="Продолжить игру"
                                style="min-width: 12em;"
                                @click="continueActiveGame()">
                        </jc-btn>
                    </div>

                    <div class="q-ma-sm" v-if="!localState.game.done">
                        <jc-btn kind="secondary" label="Выйти из игры"
                                style="min-width: 12em;"
                                @click="closeActiveGame()">
                        </jc-btn>
                    </div>

                    <div class="q-ma-sm"
                         v-if="localState.game.plan && localState.game.done">
                        <jc-btn kind="secondary" label="Играть уровень еще раз"
                                style="min-width: 12em;"
                                @click="startNewGame()">
                        </jc-btn>
                    </div>

                    <div class="q-ma-sm"
                         v-if="localState.game.plan && localState.game.done">
                        <jc-btn kind="secondary" label="Редактировать уровень"
                                style="min-width: 12em;"
                                @click="planTaskStatistic(localState.game.plan)">
                        </jc-btn>
                    </div>

                    <div class="q-ma-sm"
                         v-if="localState.game.plan && !localState.game.done">
                        <jc-btn kind="secondary" label="Играть другой уровень"
                                style="min-width: 12em;"
                                @click="onSelectLevel()">
                        </jc-btn>
                    </div>

                    <div class="q-ma-sm"
                         v-if="localState.game.plan && localState.game.done">
                        <jc-btn kind="primary" label="Играть другой уровень"
                                style="min-width: 12em;"
                                @click="onSelectLevel()">
                        </jc-btn>
                    </div>

                    <div class="q-ma-sm" v-if="!localState.game.plan">
                        <jc-btn kind="primary" label="Выбрать уровень"
                                style="min-width: 12em;"
                                @click="onSelectLevel()">
                        </jc-btn>
                    </div>

                </div>

            </template>

            <template v-else>

                <div>
                    <jc-btn kind="primary" label="Выбрать другой уровень"
                            style="min-width: 12em;"
                            @click="onSelectLevel()">
                    </jc-btn>
                </div>

            </template>


            <TaskList
                v-if="localState.game.done"
                :showAnswerResult="true"
                :planTasks="localState.gameTasks"/>


        </div>


    </MenuContainer>

</template>

<script>

import {apx} from "./vendor"
import auth from "run-game-frontend/src/auth"
import gameplay from "./gameplay"
import MenuContainer from "./comp/MenuContainer"
import GameInfo from "./comp/GameInfo"
import PlanInfo from "./comp/PlanInfo"
import TaskList from "./comp/TaskList"

export default {
    name: "GameInfoPage",
    components: {MenuContainer, GameInfo, PlanInfo, TaskList},

    data() {
        return {
            localState: {},
            dataLoaded: false,
        }
    },

    computed: {
        title: function() {
            if (!this.localState.game || !this.localState.game.id) {
                return ""
            } else if (this.localState.game.done) {
                return "Последняя игра"
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

        continueActiveGame() {
            apx.showFrame({
                frame: '/game', props: {prop1: 1}
            })
        },

        async onSelectLevel() {
            await gameplay.closeActiveGame()

            apx.showFrame({
                frame: '/levelChoice',
            })
        },

        planTaskStatistic(plan) {
            apx.showFrame({
                frame: '/plan', props: {idPlan: plan}
            })
        },

        async startNewGame() {
            await gameplay.gameStart(this.localState.game.plan)

            apx.showFrame({
                frame: '/game', props: {prop1: 1}
            })
        },
    },

    async mounted() {
        // Есть текущий пользователь?
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login', props: {prop1: 1}
            })
            return
        }

        //
        this.dataLoaded = false

        // Загружаем текущую  игру
        this.localState = await gameplay.loadActiveGame()

        // Есть текущая игра?
        if (!this.localState.game) {
            // Загружаем последнюю игру
            this.localState = await gameplay.loadLastGame()
        }

        //
        this.dataLoaded = true
    },


}

</script>

<style lang="less" scoped>

.game-tasks {
    _font-size: 1.2em;
}

hr {
    margin: 1em 0;
}

</style>