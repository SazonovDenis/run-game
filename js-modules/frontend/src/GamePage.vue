<template>

    <MenuContainer title="Играем!">

        <template v-slot:footer>
            <GameState :game="globalState.game" :tasksResult="globalState.tasksResult"/>
        </template>

        <UserTaskPanel
            v-if="isAuth()"
            :gameTask="globalState.gameTask" :dataState="globalState.dataState"/>

    </MenuContainer>

</template>


<script>

import ctx from "./gameplayCtx"
import auth from "./auth"
import UserTaskPanel from "./comp/UserTaskPanel"
import GameState from "./comp/GameState"
import MenuContainer from "./comp/MenuContainer"
import {apx} from "run-game-frontend/src/vendor"

/**
 * Окно игры, показываются задание и ответы.
 */
export default {
    name: "GamePage",

    components: {
        MenuContainer, UserTaskPanel, GameState
    },

    computed: {},

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

        //
        onLoadedGameTask(gameTask) {
            if (ctx.globalState.game.done) {
                apx.showFrame({
                    frame: '/gameInfo', props: {prop1: 1}
                })
            }
        },
    },

    created() {
    },

    async mounted() {
        ctx.eventBus.on("loadedGameTask", this.onLoadedGameTask)

        //
        ctx.gameplay.loadCurrentOrNextTask()
    },

    unmounted() {
        ctx.eventBus.off("loadedGameTask", this.onLoadedGameTask)
        //gameplay.shutdown()
    },

}

</script>


<style scoped>

</style>