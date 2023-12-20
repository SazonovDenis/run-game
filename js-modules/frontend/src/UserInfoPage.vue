<template>

    <MenuContainer itle="Профиль игрока">

        <UserInfo
            :user="globalState.user"
        />

        <q-btn @click="logout()">Logout</q-btn>

    </MenuContainer>

</template>

<script>

import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
import MenuContainer from "./comp/MenuContainer"
import UserInfo from "./comp/UserInfo"
import {apx} from "run-game-frontend/src/vendor"

export default {

    name: "UserInfoPage",

    components: {UserInfo, MenuContainer},

    data() {
        return {
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