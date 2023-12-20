<template>
    <div class="col" style="margin: 5em">

        <span class="row">Имя</span>
        <input class="row" v-model="username">

        <span class="row">Пароль</span>
        <input class="row" v-model="password">

        <q-btn class="row" @click="login()">Вход</q-btn>

        <q-separator style="margin: 2em 0"/>

        <q-btn color="white" text-color="black" label="Денис"
               v-if="globalState.user.id == 0"
               v-on:click="loginAsUser('user1010', '')"/>
        <q-btn color="white" text-color="black" label="Паша"
               v-if="globalState.user.id == 0"
               v-on:click="loginAsUser('user1012', '')"/>
        <q-btn color="white" text-color="black" label="Admin"
               v-if="globalState.user.id == 0"
               v-on:click="loginAsUser('admin', '111')"/>
    </div>

</template>

<script>

import {apx, jcBase} from "./vendor"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import utils from './utils'

export default {
    name: "LoginPage",

    data: function() {
        return {
            username: "admin",
            password: "111",
            globalState: ctx.getGlobalState(),
        }
    },

    created() {
        gameplay.init(this.globalState)
    },

    methods: {
        async loginAsUser(username, password) {
            await gameplay.login(username, password)

            if (this.globalState.user.id > 0) {
                //window.location = "/"
                apx.showFrame({
                    frame: '/', props: {prop1: 1}
                })
            }

            //
            if (!jcBase.cfg.envDev) {
                utils.openFullscreen()
            }
        },

        async login() {
            await gameplay.login(this.username, this.password)

            if (this.globalState.user.id > 0) {
                //window.location = "/"
                apx.showFrame({
                    frame: '/', props: {prop1: 1}
                })
            }

            //
            if (!jcBase.cfg.envDev) {
                utils.openFullscreen()
            }
        }
    }
}

</script>

<style scoped>

</style>