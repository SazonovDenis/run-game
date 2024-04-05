<template>


    <q-layout view="hHh lpR fFf" style="user-select: none;">


        <q-header elevated _class="bg-primary text-white">


            <q-toolbar class="bg-primary text-white"
                       style="padding-left: 0; padding-right: 0;">

                <q-btn
                    v-if="!title"
                    _dense flat round icon="menu"
                    @click="toggleRightDrawer"
                />

                <div v-if="title" class="row">

                    <div
                        style="width: 3em; height: 2em; text-align: center;"
                        @click="onTopLeftIcon()">
                        <img
                            style="width: 2em; height: 2em;"
                            :src="iconTopLeft">
                    </div>

                    <q-toolbar-title>

                        <div class="">{{ title }}</div>

                    </q-toolbar-title>

                </div>


                <div v-if="!title">

                    <q-space/>

                    <q-tabs
                        v-model="mainTab"
                        no-caps
                        dense
                        mobile-arrows
                        inline-label
                        shrink
                        class="q-gutter-x-md bg-primary text-white"
                    >

                        <q-tab name="PlanEditPage" label="Слова" @click="onMainPage()"/>
                        <q-tab name="LevelsPage" label="Уровни" @click="onMyLevels()"/>
                        <q-tab name="GameInfoPage" label="Игры" @click="onGameInfo()"/>


                    </q-tabs>

                </div>

                <q-space/>


                <!-- slot menuBarRight -->
                <slot name="menuBarRight">

                </slot>
                <!-- -->


            </q-toolbar>


        </q-header>


        <q-drawer v-model="rightDrawerOpen" side="left" overlay behavior="mobile"
                  elevated>
            <!-- drawer content -->
            <div class="q-drawer__content fit scroll">
                <div class="q-scrollarea"
                     style="height: calc(100% - 204px); margin-top: 204px;">
                    <div
                        class="q-scrollarea__container scroll relative-position fit hide-scrollbar">
                        <div class="q-scrollarea__content absolute">

                            <div class="q-item__label q-item__label--header"
                                 style="margin-top: 1em"
                                 @click="onGameInfo()">Последняя игра
                            </div>

                            <div class="q-item__label q-item__label--header"
                                 @click="onMyLevels()">Уровни
                            </div>


                            <div class="q-mt-md q-item__label q-item__label--header"
                                 @click="onUser()">Данные игрока
                            </div>


                            <q-separator/>


                            <div class="q-mt-md q-item__label q-item__label--header"
                                 @click="onAbout()">Об игре
                            </div>

                        </div>
                    </div>

                    <div
                        class="q-scrollarea__bar q-scrollarea__bar--v absolute-right q-scrollarea__bar--invisible"
                        aria-hidden="true">
                    </div>

                    <div
                        class="q-scrollarea__bar q-scrollarea__bar--h absolute-bottom q-scrollarea__bar--invisible"
                        aria-hidden="true">

                    </div>

                    <div
                        class="q-scrollarea__thumb q-scrollarea__thumb--v absolute-right q-scrollarea__thumb--invisible"
                        aria-hidden="true" style="top: 365.517px; height: 341px;">

                    </div>

                    <div
                        class="q-scrollarea__thumb q-scrollarea__thumb--h absolute-bottom q-scrollarea__thumb--invisible"
                        aria-hidden="true" style="left: 0px; width: 299px;">
                    </div>
                </div>

                <div class="q-img q-img--menu absolute-top" role="img"
                     style="height: 204px;">

                    <div style="padding-bottom: 82.5195%;">

                    </div>

                    <div class="q-img__container absolute-full">
                        <img
                            class="q-img__image q-img__image--with-transition q-img__image--loaded"
                            loading="lazy" fetchpriority="auto" aria-hidden="true"
                            draggable="false" v-bind:src="material"
                            style="object-fit: cover; object-position: 50% 50%;">
                    </div>

                    <div class="q-img__content absolute-full q-anchor--skip">
                        <div class="absolute-bottom bg-transparent"
                             @click="onUser()">
                            <div class="q-avatar q-mb-sm" style="font-size: 56px;">
                                <div
                                    class="q-avatar__content row flex-center overflow-hidden">
                                    <img v-bind:src="avatar">
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
                <LogoGame/>
            </slot>
            <!-- -->

        </q-page-container>


        <q-page-sticky
            v-if="!isFullScreen()"
            position="bottom-left"
            :offset="[5, 5]">
            <q-btn color="grey-7"
                   style="opacity: 0.4"
                   icon="fullscreen-open"
                   size="1.3em"
                   @click="onFullscreen()"
            />
        </q-page-sticky>


        <q-footer reveal :style="{display: getFooterDisplay}">

            <!-- slot footer -->
            <slot name="footer">

                <q-toolbar :class="getFooterClass">
                    <q-toolbar-title>

<!--
                        <q-avatar
                            style="width: 0.8em; height: 0.8em; margin-right: 0.2em">
                            <img v-bind:src="iconBottomLeft">
                        </q-avatar>
-->

                    </q-toolbar-title>

                    <!-- slot footerContent -->
                    <slot name="footerContent"></slot>
                    <!-- -->

                </q-toolbar>

            </slot>

        </q-footer>


    </q-layout>
</template>


<script>

import ctx from "../gameplayCtx"
import {apx} from "../vendor"
import auth from "../auth"
import gameplay from "../gameplay"
import utils from "../utils"
import LogoGame from "./LogoGame"

import {useQuasar} from 'quasar'

export default {

    components: {
        LogoGame
    },

    props: {
        tabMenuName: null,
        frameReturn: null,
        frameReturnProps: null,
        title: null,
        showFooter: false,
        footerMode: "",
        menu: [],
    },

    created() {
        let globalState = ctx.getGlobalState()
        gameplay.init(globalState)
    },


    data: function() {
        return {
            mainTab: this.tabMenuName,
            rightDrawerOpen: false,

            userInfo: auth.getUserInfo(),
            globalState: ctx.getGlobalState(),
        }
    },


    computed: {
        getFooterDisplay() {
            if (this.showFooter) {
                return "block"
            } else {
                return "none"
            }
        },
        getFooterClass() {
            if (this.footerMode === "error") {
                return "bg-red-5 text-yellow-12"
            } else {
                return "bg-grey-7 text-white"
            }
        },
        iconTopLeft() {
            if (this.frameReturn) {
                return apx.url.ref("run/game/web/img/back-arrow.svg")
            } else {
                return apx.url.ref("run/game/web/img/logo-mono-white.svg")
            }
        },
        iconBottomLeft() {
            return apx.url.ref("run/game/web/img/logo-mono-white.svg")
        },
        avatar() {
            return apx.url.ref("run/game/web/img/boy-avatar.png")
        },
        material() {
            return apx.url.ref("run/game/web/img/material.png")
        },
    },


    watch: {
        mainTab(v1, v2) {
        }
    },


    methods: {
        isFullScreen() {
            return document.fullscreenElement != null
        },
        onFullscreen() {
            utils.openFullscreen()
        },

        toggleRightDrawer() {
            this.rightDrawerOpen = !this.rightDrawerOpen
        },

        onTopLeftIcon: function() {
            if (this.frameReturn) {
                apx.showFrame({
                    frame: this.frameReturn,
                    props: this.frameReturnProps,
                })
                return
            }

            apx.showFrame({
                frame: '/',
            })
        },

        onMainPage: function() {
            apx.showFrame({
                frame: '/',
            })
        },

        onAbout: function() {
            apx.showFrame({
                frame: '/about',
            })
        },

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
                frame: '/planList',
            })
        },

        onMyLevels: function() {
            apx.showFrame({
                frame: '/levels',
            })
        },

        onGameInfo: function() {
            apx.showFrame({
                frame: '/gameInfo',
            })
        },
    },


    mounted() {
        console.info("this.tabMenuName: " + this.tabMenuName)
        this.mainTab = this.tabMenuName


        let quasar = useQuasar()
        console.log(quasar.platform.is)
        apx.cfg.is = quasar.platform.is

    },


}
</script>


<style lang="less" scoped>

.menuBarDropdown {
}

</style>