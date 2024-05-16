<template>


    <q-item class=""
            clickable v-ripple
            @click="onUsrClick(usr)"
    >

        <q-item-section top avatar>

            <q-avatar
                v-if="usr.linkType === LinkType_blocked"
                icon="cancel"
                color="grey-2"
                text-color="grey-5">
            </q-avatar>

            <q-avatar
                v-else-if="usr.confirmState === ConfirmState_accepted"
                icon="star"
                color="grey-2"
                text-color="yellow-8">
            </q-avatar>

            <q-avatar
                v-else
                color="grey-2"
                text-color="yellow-8">
            </q-avatar>

        </q-item-section>


        <q-item-section>

            <q-item-label>
                {{ usr.text }}
            </q-item-label>

            <q-item-label
                caption
                v-if="usr.confirmState && usr.confirmState !== ConfirmState_accepted">

                <template v-if="usr.usrFrom !== userId">
                    Просит добавления в {{ dictLinkTypeTo[usr.linkType] }}
                </template>

                <template
                    v-if="usr.usrFrom === userId && usr.confirmState === ConfirmState_waiting">
                    Вы ждете добавления в {{ dictLinkTypeFrom[usr.linkType] }}
                </template>

                <template
                    v-if="usr.usrFrom === userId && usr.confirmState === ConfirmState_refused">
                    Отказал в добавлении в {{ dictLinkTypeFrom[usr.linkType] }}
                </template>
            </q-item-label>

        </q-item-section>


        <q-item-section top side>

            <div class="text-grey-8 q-gutter-xs">

                <template
                    v-if="usr.usrFrom !== userId && usr.confirmState === ConfirmState_waiting">

                    <q-btn
                        label="Отказать"
                        no-caps dense rounded unelevated
                        size="0.9em"

                        icon="del"
                        color="red"
                        @click="refuse(usr)"

                    />

                    <q-btn
                        label="Принять"
                        no-caps dense rounded unelevated
                        size="0.9em"
                        icon="quasar.chip.selected"
                        color="green"
                        @click="accept(usr)"

                    />
                </template>

                <template
                    v-if="usr.usrFrom === userId && (usr.confirmState === ConfirmState_waiting || usr.confirmState === ConfirmState_refused)">

                    <q-btn
                        label="Отменить"
                        no-caps dense rounded unelevated
                        size="0.9em"

                        icon="del"
                        color="blue-8"
                        @click="cancel(usr)"

                    />

                </template>

                <template v-if="!usr.usrFrom">

                    <q-btn
                        fab-mini no-caps unelevated
                        dropdown-icon=""
                        color="primary"
                        _icon="quasar.chip.selected"
                        label="Добавить..."
                    >
                        <q-menu>


                            <q-list>
                                <q-item
                                    clickable v-close-popup
                                    class="q-ma-md"
                                    @click="request_friend(usr)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как друга</q-item-label>
                                    </q-item-section>
                                </q-item>

                                <q-separator class="q-ma-md"/>

                                <q-item
                                    clickable v-close-popup
                                    class="q-ma-md"
                                    @click="request_parent(usr)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как родителя
                                        </q-item-label>
                                    </q-item-section>
                                </q-item>

                                <q-item
                                    clickable v-close-popup
                                    class="q-ma-md"
                                    @click="request_child(usr)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как ребенка
                                        </q-item-label>
                                    </q-item-section>
                                </q-item>


                                <q-separator class="q-ma-md"/>

                                <q-item
                                    clickable v-close-popup
                                    class="q-ma-md"
                                    @click="request_student(usr)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как ученика
                                        </q-item-label>
                                    </q-item-section>
                                </q-item>

                                <q-item
                                    clickable v-close-popup
                                    class="q-ma-md"
                                    @click="request_teacher(usr)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как учителя
                                        </q-item-label>
                                    </q-item-section>
                                </q-item>
                            </q-list>

                        </q-menu>

                    </q-btn>

                </template>


                <q-btn-dropdown
                    dropdown-icon="more-h"
                    fab-mini unelevated
                    _color="grey-2"
                >
                    <q-list>
                        <q-item
                            v-if="usr.linkType && usr.linkType !== LinkType_blocked && usr.confirmState === ConfirmState_accepted"
                            clickable v-close-popup
                            class="q-ma-md"
                            @click="usrBreakLink(usr)"
                        >
                            <q-item-section>
                                <q-item-label>Удалить</q-item-label>
                            </q-item-section>
                        </q-item>

                        <q-item
                            v-if="usr.linkType !== LinkType_blocked"
                            clickable v-close-popup
                            class="q-ma-md"
                            @click="usrBlock(usr)"
                        >
                            <q-item-section>
                                <q-item-label>Заблокировать
                                </q-item-label>
                            </q-item-section>
                        </q-item>

                        <q-item
                            v-if="usr.linkType === LinkType_blocked"
                            clickable v-close-popup
                            class="q-ma-md"
                            @click="usrUnblock(usr)"
                        >
                            <q-item-section>
                                <q-item-label>Разблокировать</q-item-label>
                            </q-item-section>
                        </q-item>
                    </q-list>
                </q-btn-dropdown>


            </div>

        </q-item-section>

    </q-item>


</template>

<script>

import {daoApi} from "../dao"
import {apx} from "../vendor"
import auth from "../auth"
import ctx from "../gameplayCtx"
import gameplay from "../gameplay"
import dbConst from "../dao/dbConst"

export default {

    components: {},

    // mixins
    setup(props) {
        return dbConst
    },

    props: {
        usr: {
            type: Object,
            default: null,
        },
    },

    data() {
        return {
            // Названия для ссылок ОТ пользователя
            dictLinkTypeFrom: {
                [dbConst.LinkType_friend]: "Ваши друзья",
                [dbConst.LinkType_parent]: "Ваши родители",
                [dbConst.LinkType_child]: "Ваши дети",
                [dbConst.LinkType_teacher]: "Ваши учителя",
                [dbConst.LinkType_student]: "Ваши ученики",
                [dbConst.LinkType_blocked]: "Заблокированные",
            },
            // Названия для ссылок К пользователю
            dictLinkTypeTo: {
                [dbConst.LinkType_friend]: "Ваши друзья",
                [dbConst.LinkType_parent]: "Ваши дети",
                [dbConst.LinkType_child]: "Ваши родители",
                [dbConst.LinkType_teacher]: "Ваши ученики",
                [dbConst.LinkType_student]: "Ваши учителя",
            },
        }
    },

    watch: {},

    computed: {
        userId() {
            let userInfo = auth.getUserInfo()
            return userInfo.id
        }
    },

    methods: {

        async onUsrClick(usr) {
            if (usr.linkType === dbConst.LinkType_child || usr.linkType === dbConst.LinkType_student) {
                await gameplay.api_loginContextUser(usr.id)

                //
                let userInfo = auth.getUserInfo()
                ctx.eventBus.emit("contextUserChanged", userInfo.contextUser)

                //
                apx.showFrame({
                    frame: '/'
                })
            }
        },

        async request_friend(usr) {
            this.request(usr, dbConst.LinkType_friend)
        },

        async request_parent(usr) {
            this.request(usr, dbConst.LinkType_parent)
        },

        async request_child(usr) {
            this.request(usr, dbConst.LinkType_child)
        },

        async request_student(usr) {
            this.request(usr, dbConst.LinkType_student)
        },

        async request_teacher(usr) {
            this.request(usr, dbConst.LinkType_teacher)
        },

        async request(usr, linkType) {
            let link = {
                usrTo: usr.id,
                linkType: linkType,
            }
            await daoApi.invoke('m/Link/request', [link])

            //
            usr.linkType = linkType
            usr.confirmState = dbConst.ConfirmState_waiting
            usr.usrFrom = this.userId
            //
            ctx.eventBus.emit("change:link", usr)
        },

        async cancel(usr) {
            let link = {
                usrTo: usr.id,
            }
            await daoApi.invoke('m/Link/cancel', [link])

            //
            ctx.eventBus.emit("change:link", usr)
        },

        async refuse(usr) {
            let link = {
                usrFrom: usr.id,
            }
            await daoApi.invoke('m/Link/refuse', [link])

            //
            ctx.eventBus.emit("change:link", usr)
        },

        async accept(usr) {
            let link = {
                usrFrom: usr.id,
            }
            await daoApi.invoke('m/Link/accept', [link])

            //
            ctx.eventBus.emit("change:link", usr)
        },

        async usrBreakLink(usr) {
            await daoApi.invoke('m/Link/usrBreakLink', [usr.id])

            //
            ctx.eventBus.emit("change:link", usr)
        },

        async usrBlock(usr) {
            await daoApi.invoke('m/Link/usrBlock', [usr.id])

            //
            ctx.eventBus.emit("change:link", usr)
        },

        async usrUnblock(usr) {
            await daoApi.invoke('m/Link/usrUnblock', [usr.id])

            //
            ctx.eventBus.emit("change:link", usr)
        },


    },

    async mounted() {
    },

}

</script>


<style>

</style>