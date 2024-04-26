<template>

    <MenuContainer title="Играем!" :showFooter="true">

        <template v-slot:footerContent>
            <GameState :game="globalState.game" :tasksResult="globalState.tasksResult"/>
        </template>

        <UserTaskPanel
            v-if="isAuth()"
            :gameTask="globalState.gameTask" :dataState="globalState.dataState"
        />

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
    },

    created() {
    },

    async mounted() {
        // Если игры нет - нечего тут делать
        if (!ctx?.globalState?.game || ctx.globalState.game.done) {
            apx.showFrame({
                frame: '/gameInfo',
            })
            return
        }

        // Грузим текущее задание (если оно не загружено)
        await ctx.gameplay.loadCurrentOrNextTask()
    },

    unmounted() {
    },

}

</script>


<style scoped>

</style>