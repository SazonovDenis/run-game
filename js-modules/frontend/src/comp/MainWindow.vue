<template>

    <div class="main-window-menu">
        <q-btn color="white" text-color="black" label="Next" v-on:click="nextTask"/>

        <q-btn color="white" text-color="black" label="User 1"
               v-on:click="login('user1', '111')"/>
        <q-btn color="white" text-color="black" label="User 2"
               v-on:click="login('user2', '222')"/>
        <q-btn color="white" text-color="black" label="Logout"
               v-on:click="logout()"/>
        <q-btn color="white" text-color="black" label="Full"
               v-on:click="openFullscreen()"/>

        <UserInfo :user="user"/>
    </div>

    <UserTaskPanel :usrTask="usrTask" :dataState="dataState"/>

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

    // Состояние игрового мира
    data() {
        return {
            usrTask: {
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
                }
            },

            user: {
                id: null,
                name: null,
                color: null,
            },

            testData_taskIdx: 1,
        }
    },

    methods: {

        async login(user, password) {
            let res = await apx.jcBase.ajax.request({
                url: "auth/login",
                params: {name: user, password: password},
            })

            this.user.id = res.data.id
            this.user.name = res.data.name
            this.user.color = res.data.color
        },

        async logout() {
            let res = await apx.jcBase.ajax.request({
                url: "auth/logout",
            })

            this.user.id = res.data.id
            this.user.name = res.data.name
            this.user.color = res.data.color
        },

        async getUserInfo() {
            let res = await apx.jcBase.ajax.request({
                url: "auth/getUserInfo",
            })

            this.user.id = res.data.id
            this.user.name = res.data.name
            this.user.color = res.data.color
        },

        openFullscreen() {
            utils.openFullscreen()
        },

        nextTask() {
            gameplay.nextTask()
        },

        // Присваиваем данные задания в глобальный контекст
        onLoadedUsrTask(usrTask) {
            this.usrTask = usrTask
            ctx.usrTask = this.usrTask
        },
    },

    created() {
        gameplay.init(this.dataState)
    },

    mounted() {
        ctx.eventBus.on("loadedUsrTask", this.onLoadedUsrTask)

        //
        this.getUserInfo()

        //
        this.nextTask()
    }

}

</script>


<style scoped>

.main-window-menu {
    display: flex;
    flex-direction: row;
}

</style>