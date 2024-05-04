<template>

    <MenuContainer title="Об игре">

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


                <div class="">
                    Нужно сыграть уровень несколько раз, чтобы набрать в нем максимальное
                    число очков.
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


            <q-separator/>


            <div class="q-pa-sm" _class="row" style="margin-top: 1.5em;">

                <div class="rgm-link-soft q-pa-sm"
                     @click="about_isShown = true">
                    Создатели
                </div>

                <template v-if="about_isShown">
                    <div class="q-pa-sm">
                        <span class="value-label">Идея и разработка: </span> <span
                        class="value-value">ТОО &laquo;Wordsrike&raquo;</span>
                    </div>
                    <div class="q-pa-sm">
                        <span class="value-label">Библиотека: </span> <span
                        class="value-value">Кравченко Сергей</span>
                    </div>
                    <div class="q-pa-sm">
                        <span class="value-label">Графика и анимация: </span> <span
                        class="value-value">Сазонова Наталья</span>
                    </div>
                    <div class="q-pa-sm">
                        <span class="value-label">Тестирование: </span> <span
                        class="value-value">Сазонов Павел</span>
                    </div>
                </template>
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

            about_isShown: false,
        }
    },

    methods: {

        levels() {
            apx.showFrame({
                frame: '/levels'
            })
        },

        gameInfo() {
            apx.showFrame({
                frame: '/gameInfo'
            })
        },

    },

    computed: {},

    async mounted() {
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

.value-label {
    color: #4e4e4e;
}

.value-value {
    color: #34558b;
}


</style>