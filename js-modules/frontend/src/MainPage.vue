<template>

    <MenuContainer title="Главная">

        <template v-slot:footer>
            <GameState :game="globalState.game" :tasksResult="globalState.tasksResult"/>
        </template>


        <div class="q-gutter-md">

            <LogoGame/>

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
import LogoGame from "./comp/LogoGame"
import auth from "./auth"

export default {
    name: "MainPage",

    components: {
        MenuContainer, UserTaskPanel, UserInfo, GameState, LogoGame, gameplay
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
            if (!this.globalState.game.done) {
                // Переходим на текущую игру
                this.gameInfo()
            }
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

