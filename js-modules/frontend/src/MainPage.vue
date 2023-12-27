<template>

    <MenuContainer title="MainPage_xxx">

        <template v-slot:footer>
            <GameState :game="globalState.game"/>
        </template>

    </MenuContainer>

</template>


<script>

import UserTaskPanel from "./comp/UserTaskPanel"
import UserInfo from "./comp/UserInfo"
import GameState from "./comp/GameState"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import utils from './utils'
import {apx} from './vendor'
import MenuContainer from "./comp/MenuContainer"
import auth from "./auth"

export default {
    name: "MainPage",

    components: {
        MenuContainer, UserTaskPanel, UserInfo, GameState, gameplay
    },

    // Состояние игрового мира
    data() {
        return {
            globalState: ctx.getGlobalState(),
        }
    },

    methods: {

        isAuth() {
            return auth.isAuth()
        },

        levels() {
            apx.showFrame({
                frame: '/levels', props: {prop1: 1}
            })
        },

        gameInfo() {
            apx.showFrame({
                frame: '/gameInfo', props: {prop1: 1}
            })
        },

        openFullscreen() {
            utils.openFullscreen()
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

        // Есть  текущая игра?
        await gameplay.loadActiveGame()
        //
        if (this.globalState.game.id) {
            this.gameInfo()
        } else {
            //this.levels()
        }
    },

    unmounted() {
    },

}

</script>


<style scoped>

</style>