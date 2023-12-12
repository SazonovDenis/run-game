<template>

    <div style="user-select: none;">

        <div class="main-window-menu">
            <q-btn color="white" text-color="black" label="Next"
                   v-if="globalState.user.id > 0"
                   v-on:click="nextTask"/>

            <q-btn color="white" text-color="black" label="Денис"
                   v-if="globalState.user.id == 0"
                   v-on:click="login('user1010', '')"/>
            <q-btn color="white" text-color="black" label="Паша"
                   v-if="globalState.user.id == 0"
                   v-on:click="login('user1012', '')"/>

            <q-btn color="white" text-color="black" label="Уровень"
                   v-if="globalState.user.id > 0"
                   v-on:click="levels()"/>

            <q-btn color="white" text-color="black" label="F"
                   v-if="globalState.user.id > 0"
                   v-on:click="openFullscreen()"/>

            <GameInfo :game="globalState.game"/>

            <div class="menu-fill">
            </div>

<!--
            <q-btn color="white" text-color="black" label="Logout"
                   v-if="globalState.user.id > 0"
                   v-on:click="logout()"/>
-->

            <UserInfo :user="globalState.user"
                      v-on:click="logout()"
            />
        </div>

        <UserTaskPanel
            v-if="globalState.user.id > 0"
            :gameTask="globalState.gameTask" :dataState="globalState.dataState"/>

        <div v-else class="main-window-img">
            <img v-bind:src="backgroundImage">
        </div>

    </div>

</template>


<script>

import UserTaskPanel from "./UserTaskPanel"
import UserInfo from "./UserInfo"
import GameInfo from "./GameInfo"
import gameplay from "../gameplay"
import ctx from "../gameplayCtx"
import utils from '../utils'
import {apx, jcBase} from '../vendor'

export default {
    name: "MainWindow",

    components: {
        UserTaskPanel, UserInfo, GameInfo, gameplay
    },

    computed: {
        backgroundImage() {
            return apx.url.ref("run/game/web/cube.png")
        },
    },

    // Состояние игрового мира
    data() {
        return {
            // Состояние игрового мира
            globalState: {
                // Иноформация об авторизованном пользователе
                user: {
                    id: 0,
                    login: null,
                    text: null,
                    color: null,
                },

                // Иноформация о раунде
                game: {
                    id: null,
                    plan: null,
                    text: "не загружена",
                    countTotal: 0,
                    countDone: 0,
                },

                // Задание
                gameTask: {
                    task: {},
                    taskOptions: {}
                },

                // Взаимодействие с пользователем и соответствующие анимации
                dataState: {
                    drag: {
                        dtStart: null,
                        dtStop: null,
                        drag: null,
                        x: null,
                        y: null,
                        sx: null,
                        sy: null,
                    },
                    goal: {
                        text: null,
                        value: null,
                    },
                    ball: {
                        text: null,
                        // Размер мяча
                        value: 0,
                        // Мяч в режиме "правильный ответ"
                        ballIsTrue: null,
                    },
                    mode: {
                        modeShowOptions: null,
                        goalHitSize: null,
                    },

                    showTaskHint: false,
                },

                testData_taskIdx: 1,
            }
        }
    },

    methods: {

        async login(login, password) {
            let res = await apx.jcBase.ajax.request({
                url: "auth/login",
                params: {login: login, password: password},
            })

            this.globalState.user.id = res.data.id
            this.globalState.user.login = res.data.login
            this.globalState.user.text = res.data.text
            this.globalState.user.color = res.data.color

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
            let res = await apx.jcBase.ajax.request({
                url: "auth/logout",
            })

            this.globalState.user.id = res.data.id
            this.globalState.user.login = res.data.login
            this.globalState.user.text = res.data.text
            this.globalState.user.color = res.data.color
            if (!this.globalState.user.id) {
                this.globalState.user.id = 0
            }
            // Задание и раунд в глобальном контексте
            this.globalState.game = {}
            this.globalState.gameTask = {}
        },

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