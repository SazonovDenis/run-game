<template>

    <MenuContainer title="MainWindow_xxx">

        <template v-slot:footer>
            <GameState :game="globalState.game"/>
        </template>

        <q-btn color="white" text-color="black" label="F"
               v-if="isAuth()"
               v-on:click="openFullscreen()"/>

        <div class="main-window-img">
            <img v-bind:src="backgroundImage">
        </div>

    </MenuContainer>

</template>


<script>

import UserTaskPanel from "./UserTaskPanel"
import UserInfo from "./UserInfo"
import GameState from "./GameState"
import gameplay from "../gameplay"
import ctx from "../gameplayCtx"
import utils from '../utils'
import {apx} from '../vendor'
import MenuContainer from "./MenuContainer"
import auth from "run-game-frontend/src/auth"

export default {
    name: "MainWindow",

    components: {
        MenuContainer, UserTaskPanel, UserInfo, GameState, gameplay
    },

    computed: {
        backgroundImage() {
            return apx.url.ref("run/game/web/cube.png")
        },
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

    created() {
        gameplay.init(this.globalState)
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
        await gameplay.getActiveGame()
        if (this.globalState.game.id) {
            this.gameInfo()
        } else {
            //this.levels()
        }
    },

    unmounted() {
        ctx.eventBus.off("loadedGameTask", this.onLoadedGameTask)
        //gameplay.shutdown()
    },

}

</script>


<style scoped>

.main-window-menu {
    display: flex;
    flex-direction: row;
}

.menu-fill {
    flex-grow: 100;
}

.main-window-img img {
    padding-top: 10em;
    width: 10em;
}

.main-window-img {
    display: flex;
    flex-direction: column;
    align-items: center;

    width: 100%;
}

.main-window-info {
    padding: 1em;
    font-size: 1.5em;
    color: #7a7a7a;
}

</style>