<template>
    <q-layout>
        <q-page-container>
            <q-page class="flex flex-center">

                <!-- loginType==='full' -->

                <q-card v-if="loginType==='full'"
                        style="min-width: 25rem; _box-shadow: none">

                    <LogoGame/>

                    <q-card-section class="q-pa-lg text-center">
                        <div class="text-grey-9 text-h4 text-weight-bold">
                            Вход / регистрация
                        </div>
                    </q-card-section>

                    <q-card-section class="q-pa-lg q-gutter-y-md">

                        <q-input v-model="text"
                                 label="Имя*"
                                 outlined
                                 stack-label :dense="true">
                            <template #prepend>
                                <q-icon name="user" size="1em" color="gray-600"/>
                            </template>
                        </q-input>

                        <q-input v-model="login"
                                 label="Почта или телефон"
                                 outlined
                                 stack-label
                                 :dense="true">
                            <template #prepend>
                                <q-icon name="mail" size="1em" color="gray-600"/>
                            </template>
                        </q-input>

                        <q-input v-model="password"
                                 outlined
                                 stack-label
                                 :dense="true"
                                 label="Пароль"
                                 type="password">
                            <template #prepend>
                                <q-icon name="password" size="1em" color="gray-600"/>
                            </template>
                        </q-input>

                        <div class="row justify-center q-mt-lg q-mb-lg11">
                            <jc-btn kind="primary" :label="loginModeText"
                                    style="min-width: 10em;"
                                    @click="execLogin">
                            </jc-btn>
                        </div>

                        <div v-if="localUserList.length>0"
                             class="row justify-left q-mt-lg q-mb-lg11"
                             style="padding-top: 1em"
                             @click="checkLocalUserLogin">

                            <span class="rgm-link-soft">Выбрать пользователя</span>
                        </div>

                    </q-card-section>

                </q-card>


                <!-- loginType==='localOneUser' -->

                <q-card v-if="loginType==='localOneUser'"
                        style="min-width: 25rem; _box-shadow: none">

                    <LogoGame/>

                    <q-card-section class="q-page flex flex-center text-center">
                        <div class="text-grey-9 text-h4 text-weight-bold">
                            Вход
                        </div>
                    </q-card-section>

                    <q-card-section class="q-pa-lg q-gutter-y-md">

                        <div>
                            <q-icon name="user"
                                    style="padding: 0 0.3em;" size="1.5em"
                                    color="gray-600"/>
                            <span> {{ text }} </span>
                        </div>

                        <div>
                            <q-icon name="mail"
                                    style="padding: 0 0.3em;" size="1.5em"
                                    color="gray-600"/>
                            <span> {{ login }} </span>
                        </div>

                        <q-input v-model="password"
                                 outlined
                                 stack-label
                                 :dense="true"
                                 label="Пароль"
                                 type="password">
                            <template #prepend>
                                <q-icon name="password" size="1em" color="gray-600"/>
                            </template>
                        </q-input>

                        <div class="row justify-center q-mt-lg q-mb-lg11 q-gutter-x-sm">
                            <div>
                                <jc-btn kind="secondary" label="Забыть пользователя"
                                        @click="clearLocalUser(id)">
                                </jc-btn>
                            </div>

                            <div>
                                <jc-btn kind="primary" label="Войти"
                                        style="min-width: 10em;"
                                        @click="this.execLoginAsUser(this.login, this.password)">
                                </jc-btn>
                            </div>
                        </div>

                    </q-card-section>

                    <q-card-section>

                        <div @click="doFullLogin">
                            <span class="rgm-link-soft">Войти другим пользователем</span>
                        </div>

                    </q-card-section>

                </q-card>


                <!-- loginType==='localUserList' -->

                <q-card v-if="loginType==='localUserList'"
                        style="min-width: 25rem; _box-shadow: none">

                    <LogoGame/>

                    <q-card-section class="q-pa-lg text-center">
                        <div class="text-grey-9 text-h4 text-weight-bold">
                            Выбор пользователя
                        </div>
                    </q-card-section>

                    <template v-for="user in localUserList">

                        <q-card-section class="q-pa-lg q-gutter-y-md">

                            <div>
                                <q-icon name="user" style="padding: 0 0.3em;"
                                        size="1.5em" color="gray-600"/>
                                <span> {{ user.text }} </span>
                            </div>

                            <div>
                                <q-icon name="mail" style="padding: 0 0.3em;"
                                        size="1.5em" color="gray-600"/>
                                <span> {{ user.login }} </span>
                            </div>


                            <q-input v-model="user.password"
                                     outlined
                                     stack-label
                                     :dense="true"
                                     label="Пароль"
                                     type="password">
                                <template #prepend>
                                    <q-icon name="password" size="1em" color="gray-600"/>
                                </template>
                            </q-input>


                            <div
                                class="row justify-center q-mt-lg q-mb-lg11d q-gutter-x-sm">
                                <div>
                                    <jc-btn kind="secondary" label="Забыть пользователя"
                                            @click="clearLocalUser(user.id)">
                                    </jc-btn>
                                </div>

                                <div>
                                    <jc-btn kind="primary"
                                            :label="'Войти как: ' + user.text"
                                            style="min-width: 10em;"
                                            @click="execLoginAsUser(user.login, user.password)">
                                    </jc-btn>
                                </div>
                            </div>

                        </q-card-section>

                    </template>

                    <q-card-section>

                        <div @click="doFullLogin">
                            <span class="rgm-link-soft">Войти другим пользователем</span>
                        </div>

                    </q-card-section>

                </q-card>

            </q-page>
        </q-page-container>
    </q-layout>

</template>

<script>

import {apx, jcBase} from "./vendor"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import utils from './utils'
import auth from "./auth"
import LogoGame from "./comp/LogoGame"

export default {

    name: "LoginPage",

    components: {LogoGame},

    data: function() {
        return {
            loginMode: null,
            loginModeText: null,
            loginType: null,
            localUserList: [],
            //
            id: null,
            text: null,
            login: null,
            password: null,
            //
            globalState: ctx.getGlobalState(),
        }
    },

    watch: {
        "text": function(oldVal, newVal) {
            this.checkLoginMode()
        }
    },

    created() {
        gameplay.init(this.globalState)

        //
        this.localUserList = this.getLocalUserList()
        this.checkLocalUserLogin()

        //
        this.checkLoginMode()
    },

    computed: {},

    methods: {

        checkLoginMode() {
            if (this.text) {
                this.loginMode = "register"
                this.loginModeText = "Регистрация нового пользователя"
            } else {
                this.loginMode = "login"
                this.loginModeText = "Вход"
            }
        },

        getLocalUserList() {
            let res = []

            //
            let list = utils.getCookiesKeys()
            for (let key of list) {
                if (utils.isLocalUserCookeName(key)) {
                    let userInfo = utils.getCookie(key, true)
                    res.push({
                        id: userInfo.id, login: userInfo.login, text: userInfo.text
                    })
                }
            }

            return res;
        },

        clearLocalUser(userId) {
            utils.deleteCookie(utils.getLocalUserCookeName(userId))

            //
            this.localUserList = this.getLocalUserList()
            this.checkLocalUserLogin()
        },

        execLogin() {
            if (this.loginMode === "login") {
                this.execLoginAsUser(this.login, this.password)
            } else if (this.loginMode === "register") {
                this.execRegisterUser(this.text, this.login, this.password)
            }
        },

        async execLoginAsUser(login, password) {
            if (!jcBase.cfg.envDev) {
                utils.openFullscreen()
            }

            //
            await gameplay.api_login(login, password)

            //
            if (auth.isAuth()) {
                let ui = auth.getUserInfo()
                utils.setCookie(utils.getLocalUserCookeName(ui.id), ui)
            }

            //
            if (auth.isAuth()) {
                apx.showFrame({
                    frame: '/', props: {prop1: 1}
                })
            }

        },

        async execRegisterUser(text, login, password) {
            await gameplay.api_register(text, login, password)

            //
            if (auth.isAuth()) {
                let ui = auth.getUserInfo()
                utils.setCookie(utils.getLocalUserCookeName(ui.id), ui)
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

        doFullLogin() {
            this.loginModeText = "Регистрация нового пользователя"
            this.loginType = "full"
            this.id = null
            this.text = null
            this.login = null
            this.password = null
        },

        checkLocalUserLogin() {
            if (this.localUserList.length > 1) {
                this.loginType = "localUserList"
            } else if (this.localUserList.length === 1) {
                let userItem = this.localUserList[0]
                this.loginType = "localOneUser"
                this.id = userItem.id
                this.text = userItem.text
                this.login = userItem.login
                this.password = userItem.password
            } else {
                this.doFullLogin()
            }
        },

    }
}

</script>

<style scoped>

.rgm-link-soft {
    text-decoration: underline;
    text-decoration-style: dashed;
    text-decoration-color: rgb(56 134 203);
    text-underline-offset: 0.3em;
}

</style>