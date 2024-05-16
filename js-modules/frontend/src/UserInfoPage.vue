<template>

    <MenuContainer title="Профиль игрока">

        <q-layout v-if="dataLoaded">
            <q-page-container>

                <q-card-section class="q-gutter-y-md">

                    <UserInfo
                        :user="userInfo"
                    />

                    <div class="row q-mt-lg q-mb-lg11d q-gutter-x-sm">
                        <jc-btn label="Выйти из программы"
                                style="min-width: 10em;"
                                @click="logout">
                        </jc-btn>

                        <q-space/>

                        <jc-btn kind="primary" label="Сохранить изменения"
                                style="min-width: 10em;"
                                @click="doUsrUpd">
                        </jc-btn>

                    </div>

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
            dataLoaded: false,
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
        this.dataLoaded = false

        // Текущий пользователь
        let userInfo = await gameplay.api_getCurrentUser()
        //
        this.userInfo.id = userInfo.id
        this.userInfo.login = userInfo.login
        this.userInfo.text = userInfo.text
        this.userInfo.guest = userInfo.guest
        this.userInfo.color = userInfo.color
        this.userInfo.planDefault = userInfo.planDefault

        //
        this.dataLoaded = true
    },

}

</script>

<style scoped>

</style>