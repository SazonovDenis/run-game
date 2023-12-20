<template>

    <MenuContainer title="">

        <template v-slot:footer>
            <GameState :game="globalState.game"/>
        </template>

        <div style="user-select: none;">

            <div class="main-window-menu">
                <!--
                                <q-btn color="white" text-color="black" label="Next"
                                       v-if="globalState.user.id > 0"
                                       v-on:click="nextTask"/>
                -->

                <q-btn color="white" text-color="black" label="Уровень"
                       v-if="globalState.user.id > 0"
                       v-on:click="levels"/>

                <q-btn label="GameInfo"
                       v-on:click="gameInfo"/>

                <q-btn color="white" text-color="black" label="F"
                       v-if="globalState.user.id > 0"
                       v-on:click="openFullscreen()"/>


<!--
                <div class="menu-fill">
                </div>
-->

            </div>

            <UserTaskPanel
                v-if="globalState.user.id > 0"
                :gameTask="globalState.gameTask" :dataState="globalState.dataState"/>

            <div v-else class="main-window-img">
                <img v-bind:src="backgroundImage">
            </div>

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

/*
        async login(login, password) {
            await gameplay.login(username, password)

            //
            if (!jcBase.cfg.envDev) {
                utils.openFullscreen()
            }

            //
            if (this.globalState.user.id > 0) {
                this.nextTask()
            }
        },

        async logout() {
            await gameplay.logout()

            apx.showFrame({
                frame: '/login',
            })
        },
*/

        async getUserInfo() {
            let res = await apx.jcBase.ajax.request({
                url: "auth/getUserInfo",
            })

            this.globalState.user.id = res.data.id
            this.globalState.user.login = res.data.login
            this.globalState.user.text = res.data.text
            this.globalState.user.color = res.data.color
            //
            if (!this.globalState.user.id) {
                this.globalState.user.id = 0
            }
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
        //jcBase.cfg.envDev = false
    },

    mounted() {
        ctx.eventBus.on("loadedGameTask", this.onLoadedGameTask)

        //
        this.getUserInfo()

        //
        this.nextTask()
    },

    unmounted() {
        ctx.eventBus.off("loadedGameTask", this.onLoadedGameTask)
        gameplay.shutdown()
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