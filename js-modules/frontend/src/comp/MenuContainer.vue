<template>
    <q-layout view="hHh lpR fFf" style="user-select: none;">

        <q-header elevated _class="bg-primary text-white">

            <q-toolbar class="bg-primary text-white">


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

                        <q-tab name="PlanEditPage" label="Слова" @click="onMainPage()"
                               _icon="user"/>
                        <q-tab name="LevelsPage" label="Уровни" @click="onMyLevels()"
                               _icon="mail"/>
                        <q-tab name="GameInfoPage" label="Игры" @click="onGameInfo()"
                               _icon="del"/>
                        <!--
                                        <q-tab name="deleted1" label="Данные игрока" _icon="del"/>
                                        <q-tab name="deleted2" label="Настройки" _icon="del"/>
                                        <q-tab name="deleted3" label="О программе" _icon="del"/>
                        -->


                    </q-tabs>

                </div>

                <q-space/>


                <slot name="menuBarRight">

                    <div class="menuBarDropdown">

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


            </q-toolbar>
        </q-header>

        <!--
        <q-drawer v-model="rightDrawerOpen" side="right" overlay behavior="mobile"
                  elevated>
            &lt;!&ndash; drawer content &ndash;&gt;
            <div class="q-drawer__content fit scroll">
                <div class="q-scrollarea"
                     style="height: calc(100% - 204px); margin-top: 204px;">
                    <div
                        class="q-scrollarea__container scroll relative-position fit hide-scrollbar">
                        <div class="q-scrollarea__content absolute">

                            <div class="q-item__label q-item__label&#45;&#45;header"
                                 style="margin-top: 1em"
                                 @click="onGameInfo()">Последняя игра
                            </div>
                            &lt;!&ndash;
                                                        <div class="q-item__label q-item__label&#45;&#45;header"
                                                             @click="onLevels()">Общие уровни
                                                        </div>
                            &ndash;&gt;
                            <div class="q-item__label q-item__label&#45;&#45;header"
                                 @click="onMyLevels()">Уровни
                            </div>
                            &lt;!&ndash;
                                                        <div class="q-item__label q-item__label&#45;&#45;header"
                                                             @click="onStill()">Still
                                                        </div>
                                                        <div class="q-item__label q-item__label&#45;&#45;header"
                                                             @click="onStill1()">Still1
                                                        </div>
                            &ndash;&gt;

                            <div class="q-mt-md q-item__label q-item__label&#45;&#45;header"
                                 @click="onUser()">Данные игрока
                            </div>

                            <q-separator/>

                            <div class="q-mt-md q-item__label q-item__label&#45;&#45;header"
                                 @click="onAbout()">Об игре
                            </div>


                            <div class="text-grey" style="padding: 25px 16px 16px;">
                            </div>
                        </div>
                    </div>
                    <div
                        class="q-scrollarea__bar q-scrollarea__bar&#45;&#45;v absolute-right q-scrollarea__bar&#45;&#45;invisible"
                        aria-hidden="true"></div>
                    <div
                        class="q-scrollarea__bar q-scrollarea__bar&#45;&#45;h absolute-bottom q-scrollarea__bar&#45;&#45;invisible"
                        aria-hidden="true"></div>
                    <div
                        class="q-scrollarea__thumb q-scrollarea__thumb&#45;&#45;v absolute-right q-scrollarea__thumb&#45;&#45;invisible"
                        aria-hidden="true" style="top: 365.517px; height: 341px;"></div>
                    <div
                        class="q-scrollarea__thumb q-scrollarea__thumb&#45;&#45;h absolute-bottom q-scrollarea__thumb&#45;&#45;invisible"
                        aria-hidden="true" style="left: 0px; width: 299px;"></div>
                </div>
                <div class="q-img q-img&#45;&#45;menu absolute-top" role="img"
                     style="height: 204px;">
                    <div style="padding-bottom: 82.5195%;"></div>
                    <div class="q-img__container absolute-full"><img
                        class="q-img__image q-img__image&#45;&#45;with-transition q-img__image&#45;&#45;loaded"
                        loading="lazy" fetchpriority="auto" aria-hidden="true"
                        draggable="false" v-bind:src="material"
                        style="object-fit: cover; object-position: 50% 50%;"></div>
                    <div class="q-img__content absolute-full q-anchor&#45;&#45;skip">
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

        -->

        <q-page-container>

            <!-- slot default -->
            <slot>
                <LogoGame/>
            </slot>
            <!-- -->

        </q-page-container>


        <q-footer reveal bordered :style="{display: getFooterDisplay}">

            <!-- slot footer -->
            <slot name="footer">

                <q-toolbar class="bg-grey-8 text-white">
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

import {ref} from 'vue'
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
        menu: [],
    },

    //^c разобрал главное меню

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
            console.info(v1, v2)
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
            //this.mainTab = "games"

            apx.showFrame({
                frame: '/gameInfo',
            })
        },
    },

    mounted() {
        console.info("this.tabMenuName: " + this.tabMenuName)
        this.mainTab = this.tabMenuName
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
    },

}
</script>


<style lang="less" scoped>

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

.menuBarDropdown {
    width: 2em;
    display: flex;
    align-content: center;
}

</style>