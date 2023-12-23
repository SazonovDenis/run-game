<template>
    <div class="col" style="margin: 5em">

        <span class="row">Имя</span>
        <input class="row" v-model="username">

        <span class="row">Пароль</span>
        <input class="row" v-model="password">

        <q-btn class="row" @click="login()">Вход</q-btn>

        <q-separator style="margin: 2em 0"/>

        <q-btn color="white" text-color="black" label="Денис"
               v-on:click="loginAsUser('user1010', '')"/>
        <q-btn color="white" text-color="black" label="Паша"
               v-on:click="loginAsUser('user1012', '')"/>
        <q-btn color="white" text-color="black" label="Admin"
               v-on:click="loginAsUser('admin', '111')"/>
    </div>

</template>

<script>

import {apx, jcBase} from "./vendor"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import utils from './utils'
import auth from "./auth"

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

            //
            if (auth.isAuth()) {
                apx.showFrame({
                    frame: '/', props: {prop1: 1}
                })
            }

        },

        async login() {
            if (!jcBase.cfg.envDev) {
                utils.openFullscreen()
            }

            //
            this.loginAsUser(this.username, this.password)
        }
    }
}

</script>

<style scoped>

</style>