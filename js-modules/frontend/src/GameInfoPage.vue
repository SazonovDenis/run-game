<template>

    <MenuContainer title="Итоги игры">

        <div class="col justify-center q-mt-lg q-mb-lg11 q-gutter-md">
            <div>
                <game-info :game="globalState.game">
                </game-info>
            </div>

            <div v-if="globalState.game.id && !globalState.game.done">
                <jc-btn kind="secondary" label="Выйти из игры"
                        @click="closeActiveGame()">
                </jc-btn>
            </div>

            <div v-if="globalState.game.id && !globalState.game.done">
                <jc-btn kind="primary" label="Продолжить игру"
                        style="min-width: 15em;"
                        @click="continueActiveGame()">
                </jc-btn>
            </div>

            <div v-if="globalState.game.plan && globalState.game.done">
                <jc-btn kind="secondary" label="Играть еще раз"
                        @click="startNewGame()">
                </jc-btn>
            </div>

            <div>
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

        onSelectLevel: function() {
            apx.showFrame({
                frame: '/levels',
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
