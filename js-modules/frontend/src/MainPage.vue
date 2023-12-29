<template>

    <MenuContainer title="Главная">

        <template v-slot:footer>
            <GameState :game="globalState.game"/>
        </template>


        <div class="q-gutter-md">

            <div class="main-window-img">
                <img v-bind:src="backgroundImage">
            </div>

            <div class="col justify-center q-mt-lg q-mb-lg11 q-gutter-md">
                <div>
                    <jc-btn kind="secondary" label="Последняя игра"
                            @click="gameInfo()">
                    </jc-btn>
                </div>

                <div>
                    <jc-btn kind="primary" label="Выбрать уровень"
                            style="min-width: 15em;"
                            @click="levels()">
                    </jc-btn>
                </div>
            </div>

        </div>

    </MenuContainer>

</template>


<script>

import UserTaskPanel from "./comp/UserTaskPanel"
import UserInfo from "./comp/UserInfo"
import GameState from "./comp/GameState"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import utils from './utils'
import {apx} from './vendor'
import MenuContainer from "./comp/MenuContainer"
import auth from "./auth"

export default {
    name: "MainPage",

    components: {
        MenuContainer, UserTaskPanel, UserInfo, GameState, gameplay
    },

    // Состояние игрового мира
    data() {
        return {
            globalState: ctx.getGlobalState(),
        }
    },

    methods: {

        isAuth() {
            return auth.isAuth()
        },

        levels() {
            apx.showFrame({
                frame: '/levels', props: {prop1: 1}
            })
        },

        gameInfo() {
            apx.showFrame({
                frame: '/gameInfo', props: {prop1: 1}
            })
        },

        openFullscreen() {
            utils.openFullscreen()
        },

    },

    computed: {
        backgroundImage() {
            return apx.url.ref("run/game/web/img/cube.png")
        },
    },

    async mounted() {
        // Есть текущий пользователь?
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login', props: {prop1: 1}
            })
            return
        }

        // Есть текущая игра?
        if (this.globalState.game.id) {
            // Переходим на текущую игру
            this.gameInfo()
        } else {
            // Загружаем текущую  игру
            await gameplay.loadActiveGame()

            // Есть текущая игра?
            if (this.globalState.game.id) {
                // Переходим на текущую игру
                this.gameInfo()
            }
        }
    },

    unmounted() {
    },

}

</script>


<style scoped>

.main-window-img img {
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 5em;
}

.main-window-img {
    padding-top: 1em;
    display: flex;
    flex-direction: column;
    align-items: center;

    height: 5em;
}

</style>