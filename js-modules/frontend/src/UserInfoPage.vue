<template>

    <MenuContainer title="Профиль игрока">

        <q-layout>
            <q-page-container>

                <q-card-section class="q-gutter-y-md">

                    <UserInfo
                        :user="userInfo"
                    />

                    <div class="row q-mt-lg q-mb-lg11d q-gutter-x-sm">

                        <jc-btn kind="secondary" label="Выйти"
                                style="min-width: 10em;"
                                @click="logout">
                        </jc-btn>

                        <jc-btn kind="primary" label="Сохранить изменения"
                                style="min-width: 10em;"
                                @click="doUsrUpd">
                        </jc-btn>

                    </div>

                </q-card-section>

                <q-card-section>

                    <jc-btn kind="primary" label="На главную"
                            style="min-width: 15em;"
                            @click="goMain">
                    </jc-btn>

                </q-card-section>

            </q-page-container>
        </q-layout>
    </MenuContainer>

</template>

<script>

import {apx} from "./vendor"
import auth from "./auth"
import gameplay from "./gameplay"
import MenuContainer from "./comp/MenuContainer"
import UserInfo from "./comp/UserInfo"
import utils from "./utils"

export default {

    name: "UserInfoPage",

    components: {UserInfo, MenuContainer},

    data() {
        return {
            userInfo: {},
        }
    },

    methods: {
        async logout() {
            await gameplay.api_logout()

            apx.showFrame({
                frame: '/login',
            })
        },

        async goMain() {
            apx.showFrame({
                frame: '/',
            })
        },

        async doUsrUpd() {
            if (this.userInfo.loginNewValue) {
                this.userInfo.login = this.userInfo.loginNewValue
            }
            await gameplay.api_updUsr(this.userInfo)

            //
            if (auth.isAuth()) {
                let ui = auth.getUserInfo()
                utils.setCookie(utils.getLocalUserCookeName(ui.id), ui)
            }

            //
            this.goMain()
        },

    },

    async mounted() {
        // Есть текущий пользователь?
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login', props: {prop1: 1}
            })
        }

        //
        let authUserInfo = auth.getUserInfo()
        this.userInfo.id = authUserInfo.id
        this.userInfo.login = authUserInfo.login
        this.userInfo.text = authUserInfo.text
        this.userInfo.guest = authUserInfo.guest
        this.userInfo.color = authUserInfo.color
        this.userInfo.planDefault = authUserInfo.planDefault
    },

}

</script>

<style scoped>

</style>