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

import {apx} from "../src/vendor"
import ctx from "./gameplayCtx"
import auth from "./auth"
import UserTaskPanel from "./comp/UserTaskPanel"
import GameState from "./comp/GameState"
import MenuContainer from "./comp/MenuContainer"

/**
 * Окно игры, показываются компонент "игровое поле".
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

        ///////////////////////////////////////
        /*
                // Грузим текущую игру (если не загружена)
                if (!ctx.globalState.game) {
                    await ctx.gameplay.loadActiveGame()
                }

                if (!ctx.globalState.game) {
                    apx.showFrame({
                        frame: '/', props: {prop1: 1}
                    })
                    return
                }

                if (ctx.globalState.game.done) {
                    apx.showFrame({
                        frame: '/gameInfo', props: {prop1: 1}
                    })
                }
        */
        ///////////////////////////////////////

        // Грузим текущее задание (если оно не загружено)
        await ctx.gameplay.loadCurrentOrNextTask()
    },

    unmounted() {
        ctx.eventBus.off("loadedGameTask", this.onLoadedGameTask)
        //gameplay.shutdown()
    },

}

</script>


<style scoped>

</style>