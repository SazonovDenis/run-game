<template>

    <MenuContainer itle="Профиль игрока">

        <UserInfo
            :user="userInfo"
        />

        <q-btn @click="logout()">Logout</q-btn>

    </MenuContainer>

</template>

<script>

import {apx} from "./vendor"
import auth from "./auth"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import MenuContainer from "./comp/MenuContainer"
import UserInfo from "./comp/UserInfo"

export default {

    name: "UserInfoPage",

    components: {UserInfo, MenuContainer},

    data() {
        return {
            userInfo: auth.getUserInfo(),
            globalState: ctx.getGlobalState(),
        }
    },

    created() {
        gameplay.init(this.globalState)
    },

    methods: {
        async logout() {
            await gameplay.logout()

            apx.showFrame({
                frame: '/login',
            })
        },

    },

}

</script>

<style scoped>

</style>