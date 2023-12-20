<template>

    <MenuContainer itle="Итоги игры">

        <game-info :game="globalState.game">

        </game-info>

        <q-btn>Yes</q-btn>

        <q-btn>No</q-btn>

    </MenuContainer>

</template>

<script>

import GameInfo from "./comp/GameInfo"
import ctx from "./gameplayCtx"
import MenuContainer from "./comp/MenuContainer"

export default {
    name: "GameInfoPage",
    components: {MenuContainer, GameInfo},

    data() {
        return {
            globalState: ctx.getGlobalState(),
        }
    },

}

</script>

<style scoped>

</style>