<template>

    <div style="user-select: none;">

        <div class="main-window-menu">
            <q-btn color="white" text-color="black" label="Next"
                   v-if="user.id > 0"
                   v-on:click="nextTask"/>

            <q-btn color="white" text-color="black" label="Денис"
                   v-if="user.id == 0"
                   v-on:click="login('user1010', '')"/>
            <q-btn color="white" text-color="black" label="Паша"
                   v-if="user.id == 0"
                   v-on:click="login('user1012', '')"/>

            <q-btn color="white" text-color="black" label="Уровень"
                   v-if="user.id > 0"
                   v-on:click="levels()"/>

            <q-btn color="white" text-color="black" label="F"
                   v-if="user.id > 0"
                   v-on:click="openFullscreen()"/>

            <div class="menu-fill">{{ game.text }}, {{
                    game.countDone
                }}/{{ game.countTotal }}
            </div>

            <q-btn color="white" text-color="black" label="Logout"
                   v-if="user.id > 0"
                   v-on:click="logout()"/>

            <UserInfo :user="user"/>
        </div>

        <UserTaskPanel
            v-if="user.id > 0"
            :gameTask="gameTask" :dataState="dataState"/>

        <div v-else class="main-window-img">
            <img v-bind:src="backgroundImage">
            <div class="main-window-info">Вы не вошли в игру</div>
        </div>

    </div>

</template>


<script>

import UserTaskPanel from "./UserTaskPanel"
import UserInfo from "./UserInfo"
import gameplay from "../gameplay"
import ctx from "../gameplayCtx"
import utils from '../utils'
import {apx} from '../vendor'

export default {
    name: "MainWindow",

    components: {
        UserTaskPanel, UserInfo, gameplay
    },

    computed: {
        backgroundImage() {
            //return "url(" + apx.url.ref("run/game/web/cube.png") + ")"
            return apx.url.ref("run/game/web/cube.png")
        },
    },

    // Состояние игрового мира
    data() {
        return {
            gameTask: {
                task: {},
                taskOptions: {}
            },

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
                    value: 0,
                    ballIsTrue: null,
                },
                game: {
                    modeShowOptions: null,
                    goalHitSize: null,
                },

                alwaysShowText: false,
            },

            user: {
                id: 0,
                login: null,
                text: null,
                color: null,
            },

            game: {
                id: null,
                plan: null,
                text: "не загружена",
                countTotal: 0,
                countDone: 0,
            },

            testData_taskIdx: 1,
        }
    },

    methods: {

        async login(login, password) {
            let res = await apx.jcBase.ajax.request({
                url: "auth/login",
                params: {login: login, password: password},
            })

            this.user.id = res.data.id
            this.user.login = res.data.login
            this.user.text = res.data.text
            this.user.color = res.data.color

            //
            this.openFullscreen()

            //
            if (this.user.id > 0) {
                this.nextTask()
            }
        },

        async logout() {
            let res = await apx.jcBase.ajax.request({
                url: "auth/logout",
            })

            this.user.id = res.data.id
            this.user.login = res.data.login
            this.user.text = res.data.text
            this.user.color = res.data.color
            //
            if (!this.user.id) {
                this.user.id = 0
            }
        },

        async getUserInfo() {
            let res = await apx.jcBase.ajax.request({
                url: "auth/getUserInfo",
            })

            this.user.id = res.data.id
            this.user.login = res.data.login
            this.user.text = res.data.text
            this.user.color = res.data.color
            //
            if (!this.user.id) {
                this.user.id = 0
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
            if (this.user.id > 0) {
                gameplay.nextTask()
            }
        },

        // Присваиваем данные задания в глобальный контекст
        onLoadedGameTask(gameTask) {
            this.gameTask = gameTask
            //ctx.gameTask = this.gameTask
            //ctx.gameTask = gameTask
        },
    },

    created() {
        gameplay.init(this.gameTask, this.dataState, this.user, this.game)
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