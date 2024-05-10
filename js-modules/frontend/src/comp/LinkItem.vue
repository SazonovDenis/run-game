<template>


    <q-item class=""
            clickable v-ripple
    >

        <q-item-section top avatar>

            <q-avatar
                v-if="usr.usrFrom"
                icon="star"
                color="grey-2"
                text-color="yellow-8">
            </q-avatar>

            <q-avatar
                v-else="usr.usrFrom"
                _icon="star"
                color="grey-2"
                text-color="yellow-8">
            </q-avatar>


        </q-item-section>

        <q-item-section>
            <q-item-label>
                {{ usr.text }}
            </q-item-label>

            <q-item-label caption v-if="usr.confirmState !== 1002">
                <template v-if="usr.usrFrom !== userId">
                    Просит добавления в {{ dictLinkType[usr.linkType] }}
                </template>

                <template v-if="usr.usrFrom === userId && usr.confirmState === 1001">
                    Вы ждете добавления в {{ dictLinkType[usr.linkType] }}
                </template>

                <template v-if="usr.usrFrom === userId && usr.confirmState === 1003">
                    Отказал в добавлении в {{ dictLinkType[usr.linkType] }}
                </template>
            </q-item-label>
        </q-item-section>


        <q-item-section top side>

            <div class="text-grey-8 q-gutter-xs">

                <template v-if="usr.usrFrom !== userId && usr.confirmState === 1001">

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
                    v-if="usr.usrFrom === userId && (usr.confirmState === 1001 || usr.confirmState === 1003)">

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
                    <!--
                                        <q-btn
                                            label="Добавить"
                                            no-caps dense rounded unelevated
                                            size="0.9em"
                                            icon="quasar.chip.selected"
                                            color="green"
                                            @click="requestIns(usr)"

                                        />
                    -->

                    <q-btn
                        fab-mini no-caps unelevated
                        dropdown-icon=""
                        color="grey-6"
                        _icon="quasar.chip.selected"
                        label="Добавить..."
                    >
                        <q-menu>


                            <q-list>
                                <q-item
                                    clickable v-close-popup
                                    @click="request_friend(usr)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как друга</q-item-label>
                                    </q-item-section>
                                </q-item>

                                <q-item
                                    clickable v-close-popup
                                    @click="request_parent(usr)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как родителя
                                        </q-item-label>
                                    </q-item-section>
                                </q-item>

                                <q-item
                                    clickable v-close-popup
                                    @click="request_student(usr, dbConst)"
                                >
                                    <q-item-section>
                                        <q-item-label>Как ученика
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
                            clickable v-close-popup
                            @click="usrBreakLink(usr)"
                        >
                            <q-item-section>
                                <q-item-label>Удалить</q-item-label>
                            </q-item-section>
                        </q-item>

                        <q-item
                            clickable v-close-popup
                            @click="usrBlock(usr)"
                        >
                            <q-item-section>
                                <q-item-label>Удалить и заблокировать
                                </q-item-label>
                            </q-item-section>
                        </q-item>

                        <q-item
                            clickable v-close-popup
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
import dbConst from "../dao/dbConst"
import auth from "../auth"

export default {

    components: {},

    props: {
        usr: {
            type: Object,
            default: null,
        },
    },

    data() {
        return {
            dictLinkType: {
                [dbConst.LinkType_friend]: "Ваши друзья",
                [dbConst.LinkType_parent]: "Ваши родители",
                [dbConst.LinkType_child]: "Ваши дети",
                [dbConst.LinkType_teacher]: "Ваши учителя",
                [dbConst.LinkType_student]: "Ваши ученики",
                [dbConst.LinkType_blocked]: "Заблокированные",
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

        async request_friend(usr) {
            this.request(usr, dbConst.LinkType_friend)
        },

        async request_parent(usr) {
            this.request(usr, dbConst.LinkType_parent)
        },

        async request_student(usr) {
            this.request(usr, dbConst.LinkType_student)
        },

        async request(usr, linkType) {
            let link = {
                usrTo: usr.id,
                linkType: linkType,
            }
            await daoApi.invoke('m/Link/request', [link])
        },

        async cancel(usr) {
            let link = {
                usrTo: usr.id,
            }
            await daoApi.invoke('m/Link/cancel', [link])
        },

        async refuse(usr) {
            let link = {
                usrFrom: usr.id,
            }
            await daoApi.invoke('m/Link/refuse', [link])
        },

        async accept(usr) {
            let link = {
                usrFrom: usr.id,
            }
            await daoApi.invoke('m/Link/accept', [link])
        },

        async usrBreakLink(usr) {
            await daoApi.invoke('m/Link/usrBreakLink', [usr.id])
        },

        async usrBlock(usr) {
            await daoApi.invoke('m/Link/usrBlock', [usr.id])
        },

        async usrUnblock(usr) {
            await daoApi.invoke('m/Link/usrUnblock', [usr.id])
        },


    },

    async mounted() {
    },

}

</script>

<style>

.item-first {
    border-top: 1px solid silver;
}

.plan-count-text {
    padding: 5px 4px 15px 4px;
    margin-top: 2px;
    width: 6em;
    text-align: center;
    font-size: .6em;
    color: #202020;
    background-color: #dbecfb;
    border-radius: 10px;
}

</style>