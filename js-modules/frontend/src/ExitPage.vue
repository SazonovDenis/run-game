<template>

    <MenuContainer title="Выход">

        <div class="col q-gutter-md">

            <LogoGame/>


            <q-separator/>


            <div class="col q-gutter-md doc-text">
                <h3>
                    Выйти из программы &laquo;Word&nbsp;strike&raquo;
                </h3>

                <div class="">
                    Нужно сыграть уровень несколько раз, чтобы набрать в нем максимальное
                    число очков.
                </div>
            </div>


            <q-separator/>


            <div class="row" style="margin-top: 1.5em;">

                <div class="q-pa-sm">
                    <jc-btn kind="secondary" label="Главная"
                            style="min-width: 12em;"
                            @click="main()">
                    </jc-btn>
                </div>

                <div class="q-pa-sm">
                    <jc-btn kind="secondary" label="Последняя игра"
                            style="min-width: 12em;"
                            @click="gameInfo()">
                    </jc-btn>
                </div>

                <div class="q-pa-sm">
                    <jc-btn kind="primary" label="Выбрать уровень"
                            style="min-width: 12em;"
                            @click="plans()">
                    </jc-btn>
                </div>
            </div>

        </div>

    </MenuContainer>

</template>


<script>

import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
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

        main() {
            window.location.replace("#/");
        },

        plans() {
            window.location.replace("#/plans");
            window.history.pushState({}, "", "#/exit");

        },

        gameInfo() {
            //
            window.location.replace("#/gameInfo");
            window.history.pushState({}, "", "#/gameInfo");

            /*
                        apx.showFrame({
                            frame: '/gameInfo'
                        })
            */
        },

    },

    computed: {},

    async mounted() {
        /*      if (!this.globalState.wasEnter) {
                  console.info("globalState.wasEnter = false")
                  //
                  this.globalState.wasEnter = true
                  //
                  window.history.pushState({}, "", "#/home");
                  window.location.replace("#/home");
              } else {
                  console.info("globalState.wasEnter = true")
              }
          */
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