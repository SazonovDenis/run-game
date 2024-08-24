<template>
    <q-layout>
        <q-page-container>
            <q-page class="flex" style="padding: 5px 0; justify-content: center;">

                <!-- loginType==='full' -->

                <q-card v-if="loginType==='full'"
                        style="min-width: 25rem; box-shadow: none">

                    <LogoGame/>

                    <q-card-section class="q-pb-none text-center">
                        <div class="text-grey-9 text-h4 text-weight-bold">
                            <div v-if="frameMode==='login'">Вход существующим игроком
                            </div>
                            <div v-if="frameMode==='register'">Создание нового игрока
                            </div>
                        </div>
                    </q-card-section>

                    <q-card-section class="q-px-lg q-gutter-y-md">

                        <q-input v-if="isModeRegister"
                                 v-model="text"
                                 label="Имя*"
                                 outlined
                                 stack-label :dense="true">
                            <template #prepend>
                                <q-icon name="user" size="0.8em" color="gray-600"/>
                            </template>
                        </q-input>

                        <q-input _v-if="!loginIsGenerated(login)"
                                 v-model="login"
                                 label="Почта или телефон"
                                 outlined
                                 stack-label
                                 :dense="true">
                            <template #prepend>
                                <q-icon name="mail" size="0.9em" color="gray-600"/>
                            </template>
                        </q-input>

                        <q-input _v-if="!loginIsGenerated(login)"
                                 v-model="password"
                                 outlined
                                 stack-label
                                 :dense="true"
                                 label="Пароль"
                                 :type="user_password_isHidden ? 'password' : 'text'">
                            <template #prepend>
                                <q-icon
                                    name="password"
                                    size="1em"
                                    color="gray-600"/>
                            </template>
                            <template v-slot:append>
                                <q-icon
                                    :name="user_password_isHidden ? 'visibility-off' : 'visibility'"
                                    class="cursor-pointer"
                                    color="gray-600"
                                    @click="user_password_isHidden = !user_password_isHidden"
                                />
                            </template>
                        </q-input>


                        <div v-if="isModeLogin" class="row justify-center q-mt-lg">
                            <jc-btn kind="primary" label="Вход"
                                    :disable="!isValidLogin()"
                                    style="min-width: 10em;"
                                    @click="execLogin">
                            </jc-btn>
                        </div>

                        <div v-if="isModeRegister" class="row justify-center q-mt-lg">
                            <jc-btn kind="primary" label="Создать нового игрока"
                                    :disable="!isValidRegistration()"
                                    style="min-width: 10em;"
                                    @click="execRegister">
                            </jc-btn>
                        </div>

                        <div class="">

                            <div v-if="isModeLogin"
                                 class="justify-left q-pt-lg"
                                 @click="setModeRegister"
                            >

                                <span class="rgm-link-soft">
                                    Создание нового игрока
                                </span>
                            </div>

                            <div v-if="isModeRegister"
                                 class="justify-left q-pt-lg"
                                 @click="frameMode = 'login'">

                                <span class="rgm-link-soft">
                                    Вход существующим игроком
                                </span>
                            </div>

                            <div v-if="localUserList.length>0"
                                 class="justify-left q-pt-lg"
                                 @click="setModeLocalUserLogin">

                                <span class="rgm-link-soft">
                                    Выбрать игрока
                                </span>
                            </div>

                        </div>


                    </q-card-section>

                </q-card>


                <!-- loginType==='localOneUser' -->

                <q-card v-if="loginType==='localOneUser'"
                        style="min-width: 25rem; box-shadow: none">

                    <LogoGame/>

                    <q-card-section class="q-pb-none text-center">
                        <div class="text-grey-9 text-h4 text-weight-bold">
                            Вход существующим игроком
                        </div>
                    </q-card-section>

                    <q-card-section class="q-px-lg q-gutter-y-md">

                        <div>
                            <q-icon name="user"
                                    style="padding: 0 0.3em;" size="1.5em"
                                    color="gray-600"/>
                            <span> {{ text }} </span>
                        </div>

                        <div v-if="!loginIsGenerated(login)">
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
                                 :type="user_password_isHidden ? 'password' : 'text'">
                            <template #prepend>
                                <q-icon
                                    name="password"
                                    size="1em"
                                    color="gray-600"/>
                            </template>
                            <template v-slot:append>
                                <q-icon
                                    :name="user_password_isHidden ? 'visibility-off' : 'visibility'"
                                    class="cursor-pointer"
                                    color="gray-600"
                                    @click="user_password_isHidden = !user_password_isHidden"
                                />
                            </template>
                        </q-input>

                        <div class="row justify-center q-mt-lg q-gutter-sm">
                            <div>
                                <jc-btn kind="secondary" label="Забыть игрока"
                                        v-if="!loginIsGenerated(this.login)"
                                        @click="clearLocalUser({id: this.id, login: this.login})">
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

                        <div class="">

                            <div @click="setModeFullLogin">
                                <span class="rgm-link-soft">Войти другим игроком</span>
                            </div>

                        </div>

                    </q-card-section>

                </q-card>


                <!-- loginType==='localUserList' -->

                <q-card v-if="loginType==='localUserList'"
                        style="min-width: 25rem; box-shadow: none">

                    <LogoGame/>

                    <q-card-section class="q-pb-none text-center">
                        <div class="text-grey-9 text-h4 text-weight-bold">
                            Выбор игрока
                        </div>
                    </q-card-section>

                    <template v-for="user in localUserList">

                        <q-card-section class="q-px-lg q-mt-xl q-gutter-y-md">

                            <div>
                                <q-icon name="user" style="padding: 0 0.3em;"
                                        size="1.5em" color="gray-600"/>
                                <span> {{ user.text }} </span>
                            </div>

                            <div v-if="!loginIsGenerated(user.login)">
                                <q-icon name="mail" style="padding: 0 0.3em;"
                                        size="1.5em" color="gray-600"/>
                                <span> {{ user.login }} </span>
                            </div>


                            <q-input v-model="user.password"
                                     outlined
                                     stack-label
                                     :dense="true"
                                     label="Пароль"
                                     :type="user_password_isHidden ? 'password' : 'text'">
                                <template #prepend>
                                    <q-icon
                                        name="password"
                                        size="1em"
                                        color="gray-600"/>
                                </template>
                                <template v-slot:append>
                                    <q-icon
                                        :name="user_password_isHidden ? 'visibility-off' : 'visibility'"
                                        class="cursor-pointer"
                                        color="gray-600"
                                        @click="user_password_isHidden = !user_password_isHidden"
                                    />
                                </template>
                            </q-input>


                            <div
                                class="row justify-center q-mt-lg q-gutter-sm">
                                <div>
                                    <jc-btn kind="secondary" label="Забыть игрока"
                                            :disable="loginIsGenerated(user.login)"
                                            @click="clearLocalUser(user)">
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

                        <div class="">

                            <div @click="setModeFullLogin">
                                <span class="rgm-link-soft">Войти другим игроком</span>
                            </div>

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
import utilsCookies from './utilsCookies'
import auth from "./auth"
import LogoGame from "./comp/LogoGame"

export default {

    name: "LoginPage",

    components: {LogoGame},

    props: {
        returnUrl: null,
        returnFrame: null,
        returnProps: null,
    },

    data: function() {
        return {
            frameMode: null,
            loginType: null,
            localUserList: [],
            //
            id: null,
            text: null,
            login: null,
            password: null,
            //
            globalState: ctx.getGlobalState(),
            //
            user_password_isHidden: true,
        }
    },

    watch: {},

    created() {
        gameplay.init()

        //
        this.localUserList = this.getLocalUserList()
        this.setModeLocalUserLogin()
    },

    computed: {
        isModeRegister() {
            return this.frameMode === "register"
        },
        isModeLogin() {
            return this.frameMode === "login"
        },
    },

    methods: {

        isValidRegistration() {
            return this.text && this.text != ""
        },

        isValidLogin() {
            return this.login && this.login != ""
        },

        loginIsGenerated(login) {
            return utils.loginIsGenerated(login)
        },

        getLocalUserList() {
            let res = []

            //
            let list = utilsCookies.getCookiesKeys()
            for (let key of list) {
                if (utilsCookies.isLocalUserCookeName(key)) {
                    let userInfo = utilsCookies.getCookie(key, true)
                    res.push({
                        id: userInfo.id, login: userInfo.login, text: userInfo.text
                    })
                }
            }

            return res;
        },

        saveLocalUser(ui) {
            let expires = new Date("3333-12-31")
            utilsCookies.setCookie(utilsCookies.getLocalUserCookeName(ui.id), ui, {expires: expires})
        },

        clearLocalUser(user) {
            let userId = user.id
            let userLogin = user.login

            //
            if (this.loginIsGenerated(userLogin)) {
                return
            }

            utilsCookies.deleteCookie(utilsCookies.getLocalUserCookeName(userId))

            //
            this.localUserList = this.getLocalUserList()
            this.setModeLocalUserLogin()
        },

        execLogin() {
            this.execLoginAsUser(this.login, this.password)
        },

        execRegister() {
            this.execRegisterUser(this.text, this.login, this.password)
        },

        redirect() {
            if (this.returnUrl) {
                window.location.replace(this.returnUrl)
            } else if (this.returnFrame) {
                apx.showFrame({
                    frame: this.returnFrame,
                    props: this.returnProps,
                })
            } else {
                apx.showFrame({
                    frame: "/",
                })
            }
        },

        async execLoginAsUser(login, password) {
            if (!jcBase.cfg.envDev && Jc.cfg.is.mobile) {
                utils.openFullscreen()
            }

            //
            await gameplay.api_login(login, password)

            //
            if (auth.isAuth()) {
                let ui = auth.getUserInfo()
                this.saveLocalUser(ui)

                //
                this.redirect()
            }
        },

        async execRegisterUser(text, login, password) {
            await gameplay.api_register(text, login, password)

            //
            if (auth.isAuth()) {
                let ui = auth.getUserInfo()
                this.saveLocalUser(ui)

                //
                this.redirect()
            }
        },

        setModeFullLogin() {
            this.frameMode = "login"
            this.loginType = "full"
            this.id = null
            this.text = null
            this.login = null
            this.password = null
        },

        setModeRegister() {
            this.frameMode = "register"
            this.loginType = "full"
            this.id = null
            this.text = null
            this.login = null
            this.password = null
        },

        setModeLocalUserLogin() {
            if (this.localUserList.length > 1) {
                this.frameMode = "login"
                this.loginType = "localUserList"
            } else if (this.localUserList.length === 1) {
                let userItem = this.localUserList[0]
                this.frameMode = "login"
                this.loginType = "localOneUser"
                this.id = userItem.id
                this.text = userItem.text
                this.login = userItem.login
                this.password = userItem.password
            } else {
                this.setModeRegister()
            }
        },

    }
}

</script>

<style>


</style>