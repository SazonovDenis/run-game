<template>

    <MenuContainer title="Game!!!">

        <template v-slot:footer>
            <GameState :game="globalState.game"/>
        </template>


        <!--
                    <div class="main-window-menu">
                        <q-btn color="white" text-color="black" label="Уровень"
                               v-if="globalState.user.id > 0"
                               v-on:click="levels"/>

                        <q-btn label="GameInfo"
                               v-on:click="gameInfo"/>

                        <q-btn color="white" text-color="black" label="F"
                               v-if="globalState.user.id > 0"
                               v-on:click="openFullscreen()"/>

                    </div>
        -->

        <UserTaskPanel
            v-if="globalState.user.id > 0"
            :gameTask="globalState.gameTask" :dataState="globalState.dataState"/>

    </MenuContainer>

</template>


<script>

import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
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

        nextTask() {
            if (this.globalState.user.id > 0) {
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