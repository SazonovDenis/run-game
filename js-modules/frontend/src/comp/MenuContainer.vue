<template>
    <q-layout view="hHh lpR fFf" style="user-select: none;">

        <q-header elevated _class="bg-primary text-white">

            <q-toolbar class="bg-primary text-white"
                       style="padding-right: 0;">


                <div v-if="title" class="row">

                    <div style="width: 3em; height: 2em;" @click="onTopLeftIcon()">
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

                    <div
                        v-if="!this.frameReturn"
                        class="menuBarDropdown">

                        <q-btn-dropdown
                            auto-close
                            flat
                            icon="menu">

                            <q-list>
                                <q-item class="q-mt-md" clickable
                                        @click="onUser()">
                                    <q-item-section>Данные игрока</q-item-section>
                                </q-item>

                                <q-item class="q-my-md" clickable
                                        @click="onAbout()">
                                    <q-item-section>О программе</q-item-section>
                                </q-item>
                            </q-list>

                        </q-btn-dropdown>

                    </div>

                </slot>
                <!-- -->


            </q-toolbar>
        </q-header>


        <q-page-container>

            <!-- slot default -->
            <slot>
                <LogoGame/>
            </slot>
            <!-- -->

        </q-page-container>


        <q-footer reveal :style="{display: getFooterDisplay}">

            <!-- slot footer -->
            <slot name="footer">

                <q-toolbar :class="getFooterClass">
                    <q-toolbar-title>

                        <q-avatar
                            style="width: 0.8em; height: 0.8em; margin-right: 0.2em">
                            <img v-bind:src="iconBottomLeft">
                        </q-avatar>

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
import LogoGame from "./LogoGame"

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
    },


}
</script>


<style lang="less" scoped>

.menuBarDropdown {
}

</style>