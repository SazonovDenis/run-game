<template>

    <MenuContainer title="Главная">

        <template v-slot:footerContent>
            <GameState :game="globalState.game" :tasksResult="globalState.tasksResult"/>
        </template>


        <div class="col q-gutter-md">

            <LogoGame/>


            <q-separator/>


            <div class="col q-gutter-md doc-text">
                <h3>
                    Игровой тренажер &laquo;Word&nbsp;strike&raquo;
                </h3>

                <div class="">
                    Слова собраны в уровни разного размера и сложности.
                    Ваша цель &ndash; набрать максимальное количество очков для каждого
                    уровня.
                </div>

                <div class="">
                    Игра делится на раунды по 10 заданий, по итогам которого вы можете
                    заработать или потерять очки.
                    Три правильных ответа в разных раундах принесут вам одно очко на
                    каждое слово.
                </div>

<!--
                <div style="margin-top: 1.5em;">

                </div>
-->

                <div class="">
                    Нужно сыграть уровень несколько раз, чтобы набрать в нем максимальное число очков.
                </div>
            </div>


            <q-separator/>


            <div class="row" style="margin-top: 1.5em;">

                <div class="q-pa-sm">
                    <jc-btn kind="secondary" label="Последняя игра"
                            style="min-width: 12em;"
                            @click="gameInfo()">
                    </jc-btn>
                </div>

                <div class="q-pa-sm">
                    <jc-btn kind="primary" label="Выбрать уровень"
                            style="min-width: 12em;"
                            @click="levels()">
                    </jc-btn>
                </div>
            </div>

        </div>

    </MenuContainer>

</template>


<script>

import {apx} from './vendor'
import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"
import GameState from "./comp/GameState"
import LogoGame from "./comp/LogoGame"

export default {
    name: "MainPage",

    components: {
        MenuContainer, GameState, LogoGame, gameplay
    },

    // Состояние игрового мира
    data() {
        return {
            globalState: ctx.getGlobalState(),
        }
    },

    methods: {

        levels() {
            apx.showFrame({
                frame: '/LevelChoice'/*, props: {prop1: 1}*/
            })
        },

        gameInfo() {
            apx.showFrame({
                frame: '/gameInfo', props: {prop1: 1}
            })
        },

    },

    computed: {},

    async mounted() {
        // Есть текущий пользователь?
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login', props: {prop1: 1}
            })
            return
        }

        // Есть текущая игра?
        if (this.globalState.game) {
            if (!this.globalState.game.done) {
                // Переходим на текущую игру
                this.gameInfo()
            }
        } else {
            // Загружаем текущую  игру
            // todo: при перезагрузке страницы вызывается отсюда, а потом в gameInfoPage - два раза получается, что избыточно
            await gameplay.loadActiveGame()

            // Есть текущая игра?
            if (this.globalState.game) {
                // Переходим на текущую игру
                this.gameInfo()
            }
        }
    },

    unmounted() {
    },

}

</script>

<style lang="less" scoped>

.doc-text {

    h3 {
        font-size: 2em;
    }

    div {
        font-size: 1.2em;
    }
}

hr {
    margin: 1.5em 0;
}


</style>