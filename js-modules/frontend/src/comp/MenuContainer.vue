<template>


    <q-layout view="hHh lpR fFf" style="user-select: none;">


        <!-- Заголовок с меню -->

        <q-header elevated style="z-index: 3000">

            <q-toolbar class="el-with-absolute bg-primary text-white"
                       style="padding-left: 0; padding-right: 0;">

                <q-btn
                    style="z-index: 1000;"
                    v-if="!title"
                    flat round icon="menu"
                    @click="toggleRightDrawer"
                >
                    <!--
                                        <q-badge style="z-index: 1000"
                                                 color="red" floating>
                                        </q-badge>
                    -->

                </q-btn>

                <div
                    v-if="!title && hasMenuAlert"
                    class="el-absolute"
                    style="left: 1.4rem; top: 0.6rem; z-index: 10000"
                    @click="toggleRightDrawer"
                >
                    <q-icon
                        name="circle"
                        color="red"
                        size="1rem"
                    />
                </div>

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


            <template v-if="contextUser">

                <div class="q-pa-sm" style="background-color: #d61818">

                    <span class="q-mr-md">
                    Текущий пользователь: {{ contextUser.text }}
                    </span>

                    <q-btn unelevated no-caps color="green-7"
                           label="Вернуться"
                           @click="clearContextUser()"
                    />

                </div>

            </template>


        </q-header>


        <!-- Содержимое -->

        <q-page-container>

            <!-- slot default -->
            <slot>
                <LogoGame/>
            </slot>
            <!-- -->

        </q-page-container>


        <!-- Подвал -->

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


        <!-- Боковое всплывающее меню -->

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


                            <div
                                class="q-mt-md q-item__label q-item__label--header"
                                @click="onLink()"
                            >
                                <span class="el-with-absolute">
                                    Друзья и связи

                                <div
                                    class="el-absolute"
                                    v-if="menuAlert?.link"
                                    style="right: -0.9rem; top: -0.6rem;"
                                >
                                    <q-icon
                                        name="circle"
                                        color="red"
                                        size="1rem"
                                    />
                                </div>

                                </span>

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


        <!-- Кнопка FullScreen -->

        <div v-if="!isFullScreen()"
             style="position: fixed; left: 0; bottom: 0; z-index: 10000;">

            <q-btn unelevated color="grey-7"
                   style="opacity: 0.7; width: 3rem; height: 3rem"
                   icon="fullscreen-open"
                   size="1.3em"
                   @click="onFullscreen()"
            />

        </div>


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
    },

    created() {
        gameplay.init()

        //
        this.mainTab = this.tabMenuName

        //
        let quasar = useQuasar()
        Jc.cfg.is = quasar.platform.is
    },


    data: function() {
        return {
            mainTab: this.tabMenuName,
            rightDrawerOpen: false,

            userInfo: auth.getUserInfo(),
            contextUser: auth.getUserInfo().contextUser,
            globalState: ctx.getGlobalState(),
        }
    },


    computed: {
        menuAlert() {
            let userInfo = auth.getUserInfo()
            if (userInfo.linksToWait && userInfo.linksToWait.length > 0) {
                return {link: true}
            }

            return {}
        },

        hasMenuAlert() {
            let userInfo = auth.getUserInfo()
            if (userInfo.linksToWait && userInfo.linksToWait.length > 0) {
                return true
            }
        },

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

        async clearContextUser() {
            let currentFrame = this.getCurrentFrame()
            let currentFramePath = currentFrame.routeInfo.path
            let returnFrame = this.getReturnFrame(currentFramePath)
            if (!returnFrame) {
                returnFrame = "/"
            }

            //
            await gameplay.api_logoutContextUser()

            //
            let userInfo = auth.getUserInfo()
            ctx.eventBus.emit("contextUserChanged", userInfo.contextUser)

            //
            apx.showFrame({
                frame: returnFrame
            })
        },

        getCurrentFrame() {
            let frames = apx.app.frameManager._showers.main._frames
            if (frames.length > 0) {
                return frames[frames.length - 1]
            }
            return null
        },

        getReturnFrame(currentPath) {
            let returnPaths = {
                "/game": "/game",
                "/levels": "/levels",
                "/gameInfo": "/gameInfo",
                "/link": "/link",
                "/plan": "/levels",
                "/planEdit": "/levels",
            }

            return returnPaths[currentPath]
        },

        onContextUserChanged() {
            this.contextUser = this.userInfo.contextUser
        },

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
            if (!this.frameReturn) {
                apx.showFrame({
                    frame: '/',
                })
                return
            }

            if (this.frameReturn instanceof Function) {
                this.frameReturn(this.frameReturnProps)
            } else {
                apx.showFrame({
                    frame: this.frameReturn,
                    props: this.frameReturnProps,
                })
            }
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
            apx.showFrame({
                frame: '/user',
            })
        },

        onLink: function() {
            apx.showFrame({
                frame: '/link',
            })
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
        ctx.eventBus.on("contextUserChanged", this.onContextUserChanged)
    },

    unmounted() {
        ctx.eventBus.off("contextUserChanged", this.onContextUserChanged)
    },


}
</script>


<style lang="less" scoped>

.menuBarDropdown {
}

.el-with-absolute {
    position: relative;
}

.el-absolute {
    position: absolute;
}

</style>