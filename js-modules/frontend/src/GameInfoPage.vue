<template>

    <MenuContainer :title="title">

        <div class="col justify-center q-mt-lg q-mb-lg11 q-gutter-md">
            <div>
                <game-info :game="globalState.game">
                </game-info>
            </div>

            <div v-if="globalState.game.id && !globalState.game.done">
                <jc-btn kind="primary" label="Продолжить игру"
                        style="min-width: 15em;"
                        @click="continueActiveGame()">
                </jc-btn>
            </div>

            <div v-if="globalState.game.id && !globalState.game.done">
                <jc-btn kind="secondary" label="Выйти из игры"
                        style="min-width: 15em;"
                        @click="closeActiveGame()">
                </jc-btn>
            </div>

            <div v-if="globalState.game.plan && globalState.game.done">
                <jc-btn kind="secondary" label="Играть уровень еще раз"
                        style="min-width: 15em;"
                        @click="startNewGame()">
                </jc-btn>
            </div>

            <div v-if="globalState.game.plan && globalState.game.done">
                <jc-btn kind="secondary" label="Редактировать уровень"
                        style="min-width: 15em;"
                        @click="planTaskStatistic(globalState.game.plan)">
                </jc-btn>
            </div>

            <div v-if="globalState.game.plan && !globalState.game.done">
                <jc-btn kind="secondary" label="Выбрать другой уровень"
                        style="min-width: 15em;"
                        @click="onSelectLevel()">
                </jc-btn>
            </div>

            <div v-if="globalState.game.plan && globalState.game.done">
                <jc-btn kind="primary" label="Выбрать другой уровень"
                        style="min-width: 15em;"
                        @click="onSelectLevel()">
                </jc-btn>
            </div>
        </div>


    </MenuContainer>

</template>

<script>

import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
import GameInfo from "./comp/GameInfo"
import MenuContainer from "./comp/MenuContainer"
import auth from "run-game-frontend/src/auth"

export default {
    name: "GameInfoPage",
    components: {MenuContainer, GameInfo},

    data() {
        return {
            globalState: ctx.getGlobalState(),
        }
    },

    computed: {
        title: function() {
            if (!this.globalState.game.id) {
                return ""
            } else if (this.globalState.game.done) {
                return "Последняя игра"
            } else {
                return "Текушая игра"
            }
        }
    },

    methods: {
        async closeActiveGame() {
            await gameplay.closeActiveGame()

            apx.showFrame({
                frame: '/', props: {prop1: 1}
            })
        },

        continueActiveGame() {
            apx.showFrame({
                frame: '/game', props: {prop1: 1}
            })
        },

        async onSelectLevel() {
            await gameplay.closeActiveGame()

            apx.showFrame({
                frame: '/levels',
            })
        },

        planTaskStatistic(plan) {
            apx.showFrame({
                frame: '/plan', props: {plan: plan}
            })
        },

        async startNewGame() {
            await gameplay.gameStart(this.globalState.game.plan)

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

        // Есть текущая игра?
        if (!this.globalState.game.id) {
            // Загружаем текущую  игру
            await gameplay.loadActiveGame()
        }

        // Есть текущая игра?
        if (!this.globalState.game.id) {
            // Загружаем последнюю игру
            await gameplay.loadLastGame()
        }
    },


}

</script>
