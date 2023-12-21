<template>

    <MenuContainer title="Итоги игры">

        <game-info :game="globalState.game">

        </game-info>

        <q-btn v-if="globalState.game.id && !globalState.game.done"
               @click="closeActiveGame()">
            Выйти из игры
        </q-btn>

        <q-btn v-if="globalState.game.id && !globalState.game.done"
               @click="continueActiveGame()">
            Продолжить игру
        </q-btn>

        <q-btn v-if="globalState.game.plan && globalState.game.done"
               @click="startNewGame()">
            Играть еще раз
        </q-btn>

    </MenuContainer>

</template>

<script>

import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
import GameInfo from "./comp/GameInfo"
import MenuContainer from "./comp/MenuContainer"

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

        async startNewGame() {
            await gameplay.gameStart(globalState.game.plan)

            apx.showFrame({
                frame: '/game', props: {prop1: 1}
            })
        },
    }

}

</script>

<style>

.q-btn {
    margin: 0.5rem;
}

</style>