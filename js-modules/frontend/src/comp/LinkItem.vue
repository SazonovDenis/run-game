<template>


    <q-item class=""
            clickable v-ripple
            @click="onUsrClick(usr)"
    >

        <q-item-section top avatar>

            <q-avatar
                v-if="usr.linkType === dbConst.LinkType_blocked"
                icon="cancel"
                color="grey-2"
                text-color="grey-5">
            </q-avatar>

            <q-avatar
                v-else-if="usr.confirmState === dbConst.ConfirmState_accepted"
                icon="usr-link"
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
                <div class="text-caption text-grey-9"
                     v-if="usr.confirmState === dbConst.ConfirmState_accepted">
                    {{ getDictLinkTypeFromRec(usr) }}
                </div>
            </q-item-label>

            <q-item-label>

                <div class="text-caption text-grey-9"
                     v-if="usr.confirmState && usr.confirmState !== dbConst.ConfirmState_accepted">

                    <template v-if="usr.usrFrom !== userId">
                        Просит добавления в {{ getDictLinkTypeTo(usr) }}
                    </template>

                    <template
                        v-if="usr.usrFrom === userId && usr.confirmState === dbConst.ConfirmState_waiting">
                        Вы ждете добавления в {{ getDictLinkTypeFrom(usr) }}
                    </template>

                    <template
                        v-if="usr.usrFrom === userId && usr.confirmState === dbConst.ConfirmState_refused">
                        <span class="text-red-10">
                        Отказал в добавлении в {{ getDictLinkTypeFrom(usr) }}
                        </span>
                    </template>

                </div>

            </q-item-label>

        </q-item-section>


        <q-item-section top side>

            <div class="text-grey-8 q-gutter-xs">

                <template
                    v-if="usr.usrFrom !== userId && usr.confirmState === dbConst.ConfirmState_waiting">

                    <q-btn
                        class="item-list-bitton"
                        label="Отказать"
                        no-caps dense rounded unelevated

                        icon="del"
                        color="red"
                        @click.stop="refuse(usr)"

                    />

                    <q-btn
                        class="item-list-bitton"
                        label="Принять"
                        no-caps dense rounded unelevated
                        icon="quasar.chip.selected"
                        color="green"
                        @click.stop="accept(usr)"

                    />
                </template>

                <template
                    v-if="usr.usrFrom === userId && (usr.confirmState === dbConst.ConfirmState_waiting || usr.confirmState === dbConst.ConfirmState_refused)">

                    <q-btn
                        no-caps dense rounded unelevated
                        class="item-list-bitton"
                        :label="usr.confirmState === dbConst.ConfirmState_refused ? 'Ok' : 'Отменить'"
                        :icon="usr.confirmState === dbConst.ConfirmState_refused ? undefined : 'del'"
                        color="blue-8"
                        @click.stop="cancel(usr)"

                    />

                </template>

                <template v-if="!usr.usrFrom">

                    <q-btn
                        fab-mini no-caps unelevated
                        dropdown-icon=""
                        color="primary"
                        label="Добавить..."
                        @click.stop
                    >
                        <q-menu>

                            <q-list>

                                <q-item v-for="link in utils.dictLinkTypeFromAdd"
                                        clickable v-close-popup
                                        class="q-ma-md"
                                        @click="request(usr, link)"
                                >
                                    <q-item-section>
                                        <q-item-label>
                                            {{ utils.dictLinkTypeFromAdd_text[link] }}
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
                    @click.stop
                >
                    <q-list>
                        <q-item
                            v-if="usr.linkType && usr.linkType !== dbConst.LinkType_blocked && usr.confirmState === dbConst.ConfirmState_accepted"
                            clickable v-close-popup
                            class="q-ma-md"
                            @click="usrBreakLink(usr)"
                        >
                            <q-item-section>
                                <q-item-label>Удалить</q-item-label>
                            </q-item-section>
                        </q-item>

                        <q-item
                            v-if="usr.linkType !== dbConst.LinkType_blocked"
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
                            v-if="usr.linkType === dbConst.LinkType_blocked"
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
import utils from "../utils"

export default {

    components: {},

    // mixins
    setup(props) {
        return {dbConst, utils}
    },

    props: {
        usr: {
            type: Object,
            default: null,
        },
    },

    data() {
        return {}
    },

    watch: {},

    computed: {
        userId() {
            let userInfo = auth.getUserInfo()
            return userInfo.id
        }
    },

    methods: {

        getDictLinkTypeFromRec(usr) {
            return utils.dictLinkTypeFromRec[usr.linkType]
        },
        getDictLinkTypeFrom(usr) {
            return utils.dictLinkTypeFrom_text[usr.linkType]
        },
        getDictLinkTypeTo(usr) {
            return utils.dictLinkTypeTo_text[usr.linkType]
        },

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