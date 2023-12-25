<template>
    <q-layout view="hHh lpR fFf" style="user-select: none;">

        <q-header reveal elevated class="bg-primary text-white">
            <q-toolbar>
                <q-toolbar-title>
                    <q-avatar @click="onMain()">
                        <img src="https://cdn.quasar.dev/logo-v2/svg/logo-mono-white.svg">
                    </q-avatar>
                    <span v-if="title">
                        Word strike - {{ title }}
                    </span>
                    <span v-else>
                        Word strike
                    </span>
                </q-toolbar-title>

                <q-btn dense flat round icon="menu" @click="toggleRightDrawer"/>
            </q-toolbar>
        </q-header>

        <q-drawer v-model="rightDrawerOpen" side="right" overlay behavior="mobile"
                  elevated>
            <!-- drawer content -->
            <div class="q-drawer__content fit scroll">
                <div class="q-scrollarea"
                     style="height: calc(100% - 204px); margin-top: 204px;">
                    <div
                        class="q-scrollarea__container scroll relative-position fit hide-scrollbar">
                        <div class="q-scrollarea__content absolute"><!---->
                            <div class="q-item__label q-item__label--header"
                                 @click="onMain()">Главная страница
                            </div>
                            <div class="q-item__label q-item__label--header"
                                 @click="onGameInfo()">Текущая игра
                            </div>
                            <div class="q-item__label q-item__label--header"
                                 @click="onLevels()">Выбрать уровень
                            </div>
                            <div class="text-grey" style="padding: 25px 16px 16px;">
                                <p><em>Right Drawer has intended scroll</em></p>
                                <p><em>Right Drawer has intended scroll</em></p>
                                <!--                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                                                <p><em>Right Drawer has intended scroll</em></p>
                                -->
                                <p><em>Right Drawer has intended scroll</em></p>
                            </div>
                        </div><!----></div><!---->
                    <div
                        class="q-scrollarea__bar q-scrollarea__bar--v absolute-right q-scrollarea__bar--invisible"
                        aria-hidden="true"></div>
                    <div
                        class="q-scrollarea__bar q-scrollarea__bar--h absolute-bottom q-scrollarea__bar--invisible"
                        aria-hidden="true"></div>
                    <div
                        class="q-scrollarea__thumb q-scrollarea__thumb--v absolute-right q-scrollarea__thumb--invisible"
                        aria-hidden="true" style="top: 365.517px; height: 341px;"></div>
                    <div
                        class="q-scrollarea__thumb q-scrollarea__thumb--h absolute-bottom q-scrollarea__thumb--invisible"
                        aria-hidden="true" style="left: 0px; width: 299px;"></div>
                </div>
                <div class="q-img q-img--menu absolute-top" role="img"
                     style="height: 204px;">
                    <div style="padding-bottom: 82.5195%;"></div>
                    <div class="q-img__container absolute-full"><img
                        class="q-img__image q-img__image--with-transition q-img__image--loaded"
                        loading="lazy" fetchpriority="auto" aria-hidden="true"
                        draggable="false" src="https://cdn.quasar.dev/img/material.png"
                        style="object-fit: cover; object-position: 50% 50%;"></div>
                    <div class="q-img__content absolute-full q-anchor--skip">
                        <div class="absolute-bottom bg-transparent"
                             @click="onUser()">
                            <div class="q-avatar q-mb-sm" style="font-size: 56px;">
                                <div
                                    class="q-avatar__content row flex-center overflow-hidden">
                                    <img src="https://cdn.quasar.dev/img/boy-avatar.png">
                                </div>
                            </div>
                            <div class="text-weight-bold">{{ userInfo.text }}
                            </div>
                            <div>{{ userInfo.id }}</div>
                        </div>
                    </div>
                </div>
            </div>

        </q-drawer>

        <q-page-container>

            <!-- slot default -->
            <slot>
                <div class="main-window-img">
                    <img v-bind:src="backgroundImage">
                </div>
            </slot>
            <!-- -->

        </q-page-container>

        <q-footer reveal bordered class="bg-grey-8 text-white">
            <q-toolbar>
                <q-toolbar-title>
                    <q-avatar>
                        <img src="https://cdn.quasar.dev/logo-v2/svg/logo-mono-white.svg">
                    </q-avatar>
                    <div></div>
                </q-toolbar-title>

                <!-- slot footer -->
                <slot name="footer"></slot>
                <!-- -->

            </q-toolbar>
        </q-footer>

    </q-layout>
</template>


<script>
import {ref} from 'vue'
import ctx from "../gameplayCtx"
import {apx} from "run-game-frontend/src/vendor"
import auth from "run-game-frontend/src/auth"

export default {
    name: "MenuContainer",

    props: {
        title: null,
        menu: [],
    },

    data: function() {
        return {
            userInfo: auth.getUserInfo(),
            globalState: ctx.getGlobalState(),
        }
    },

    computed: {
        backgroundImage() {
            return apx.url.ref("run/game/web/cube.png")
        },
    },

    methods: {
        onUser: function() {
            if (auth.isAuth()) {
                apx.showFrame({
                    frame: '/user',
                })
            } else {
                apx.showFrame({
                    frame: '/login',
                })
            }
        },
        onLevels: function() {
            apx.showFrame({
                frame: '/levels',
            })
        },
        onGameInfo: function() {
            apx.showFrame({
                frame: '/gameInfo',
            })
        },
        onMain: function() {
            apx.showFrame({
                frame: '/',
            })
        },
    },

    setup() {
        const rightDrawerOpen = ref(false)

        //
        return {
            rightDrawerOpen,
            toggleRightDrawer() {
                rightDrawerOpen.value = !rightDrawerOpen.value
            }
        }
    }
}
</script>


<style lang="less">

.menu-container {
    &--content {
        border: 5px dotted rgba(0, 62, 255, 0.3);
    }
}

.main-window-img img {
    padding-top: 10em;
    height: 10em;
}

.main-window-img {
    display: flex;
    flex-direction: column;
    align-items: center;

    height: 10em;
}

</style>