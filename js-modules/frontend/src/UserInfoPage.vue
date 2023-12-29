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
import ctx from "./gameplayCtx"
import MenuContainer from "./comp/MenuContainer"
import UserInfo from "./comp/UserInfo"
import utils from "./utils"

export default {

    name: "UserInfoPage",

    components: {UserInfo, MenuContainer},

    data() {
        return {
            userInfo: auth.getUserInfo(),
            globalState: ctx.getGlobalState(),
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
            await gameplay.api_updUsr(this.userInfo)

            //
            if (auth.isAuth()) {
                let ui = auth.getUserInfo()
                utils.setCookie(utils.getLocalUserCookeName(ui.id), ui)
            }
        },

    },

    async mounted() {
        // Есть текущий пользователь?
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login', props: {prop1: 1}
            })
        }
    },

}

</script>

<style scoped>

</style>