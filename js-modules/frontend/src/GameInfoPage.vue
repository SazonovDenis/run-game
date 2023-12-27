<template>

    <MenuContainer title="Итоги игры">

        <game-info :game="globalState.game">

        </game-info>


        <template v-if="globalState.game.id && !globalState.game.done">

            <q-btn @click="closeActiveGame()">
                Выйти из игры
            </q-btn>

            <q-btn @click="continueActiveGame()">
                Продолжить игру
            </q-btn>

        </template>


        <template v-if="globalState.game.plan && globalState.game.done">

            <q-btn @click="startNewGame()">
                Играть еще раз
            </q-btn>
            <q-btn @click="onSelectLevel()">
                Выбрать другой уровень
            </q-btn>

        </template>

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
    }

}

</script>
