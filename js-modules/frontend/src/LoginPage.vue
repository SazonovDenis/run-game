<template>
    <div class="col" style="margin: 5em">

        <span class="row">Имя</span>
        <input class="row" v-model="username">

        <span class="row">Пароль</span>
        <input class="row" v-model="password">

        <q-btn class="row" @click="login()">Вход</q-btn>

        <q-separator style="margin: 2em 0"/>

        <div>Предыдущие игроки</div>
        <div v-for="user in getLocalUserList">
            <q-btn
                color="white" text-color="black" :label="user.text"
                v-on:click="loginAsUser(user.login, '')"/>

            <q-btn
                color="white" text-color="black" label="Удалить"
                v-on:click="clearLocalUser(user.id)"/>
        </div>


        <div v-if="isEnvDev()">
            <q-separator style="margin: 2em 0"/>

            <div>Для отладки</div>

            <q-btn color="white" text-color="black" label="Денис"
                   v-on:click="loginAsUser('user1010', '')"/>
            <q-btn color="white" text-color="black" label="Паша"
                   v-on:click="loginAsUser('user1012', '')"/>
            <q-btn color="white" text-color="black" label="Admin"
                   v-on:click="loginAsUser('admin', '111')"/>
        </div>

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
            username: null,
            password: null,
            globalState: ctx.getGlobalState(),
        }
    },

    created() {
        gameplay.init(this.globalState)
    },

    computed: {
        getLocalUserList() {
            // Хак реактивности. Это написано исключительно чтобы заставить
            // перерисоваться при изменении cookie
            this.globalState.flag

            //
            let res = []

            //
            let list = utils.getCookiesKeys()
            for (let key of list) {
                if (this.isLocalUserCookeName(key)) {
                    let userInfo = utils.getCookie(key, true)
                    res.push({
                        id: userInfo.id, login: userInfo.login, text: userInfo.text
                    })
                }
            }

            return res;
        },
    },

    methods: {

        isLocalUserCookeName(name) {
            return name && name.startsWith("user_")
        },

        getLocalUserCookeName(iserId) {
            return "user_" + iserId
        },

        clearLocalUser(userId) {
            utils.deleteCookie(this.getLocalUserCookeName(userId))
            // Хак реактивности. Это написано исключительно чтобы заставить
            // перерисоваться при изменении cookie
            this.globalState.flag = {}
        },

        async loginAsUser(username, password) {
            if (!jcBase.cfg.envDev) {
                utils.openFullscreen()
            }

            //
            await gameplay.login(username, password)

            //
            if (auth.isAuth()) {
                let ui = auth.getUserInfo()
                utils.setCookie(this.getLocalUserCookeName(ui.id), ui)
            }

            //
            if (auth.isAuth()) {
                apx.showFrame({
                    frame: '/', props: {prop1: 1}
                })
            }

        },

        isEnvDev() {
            return jcBase.cfg.envDev
        },

        async login() {
            this.loginAsUser(this.username, this.password)
        }
    }
}

</script>

<style scoped>

</style>