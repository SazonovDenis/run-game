<template>

    <MenuContainer
        title="Играем!"
        frameReturn="/gameStatistic"
        :showFooter="true">

        <UserTaskPanel
            v-if="isAuth()"
            :gameTask="globalState.gameTask" :dataState="globalState.dataState"
        />

        <template v-slot:footerContent>
            <GameState :game="globalState.game" :tasksResult="globalState.tasksResult"/>
        </template>

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

        onLoadedGameTask() {
            // Перестать показывать подсказки после первого выбора
            ctx.globalState.dataState.showTaskHint = false

            //
            if (ctx.globalState.game.done) {
                apx.showFrame({
                    frame: '/gameStatistic', props: {prop1: 1}
                })
            }
        },
    },

    created() {
    },

    async mounted() {
        // Если игры нет - нечего тут делать
        if (!ctx?.globalState?.game || ctx.globalState.game.done) {
            apx.showFrame({
                frame: '/gameStatistic',
            })
            return
        }

        // Когда UserTaskPanel загрузит пустое задание, в котором
        // состояние игры будет "игра окончена" - надо уходить со страницы "Игра"
        ctx.eventBus.on("loadedGameTask", this.onLoadedGameTask)

        // Грузим текущее задание (если оно не загружено)
        await ctx.gameplay.loadCurrentOrNextTask()
    },

    unmounted() {
        ctx.eventBus.off("loadedGameTask", this.onLoadedGameTask)

        ctx.gameplay.globalClear_Game()
    },

}

</script>


<style scoped>

</style>