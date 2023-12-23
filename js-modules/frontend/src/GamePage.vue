<template>

    <MenuContainer title="Game!!!">

        <template v-slot:footer>
            <GameState :game="globalState.game"/>
        </template>

        <UserTaskPanel
            v-if="isAuth()"
            :gameTask="globalState.gameTask" :dataState="globalState.dataState"/>

    </MenuContainer>

</template>


<script>

import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import auth from "./auth"
import UserTaskPanel from "./comp/UserTaskPanel"
import GameState from "./comp/GameState"
import MenuContainer from "./comp/MenuContainer"

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

        nextTask() {
            if (auth.isAuth()) {
                gameplay.nextTask()
            }
        },

        //
        onLoadedGameTask(gameTask) {
        },
    },

    created() {
        gameplay.init(this.globalState)
    },

    async mounted() {
        ctx.eventBus.on("loadedGameTask", this.onLoadedGameTask)

        //
        //await gameplay.getActiveGame()
    },

    unmounted() {
        ctx.eventBus.off("loadedGameTask", this.onLoadedGameTask)
        //gameplay.shutdown()
    },

}

</script>


<style scoped>

</style>