<template>


    <template v-if="this.splitLinkType === true">

        <q-list v-if="usrsLength > 0">


            <template v-for="(usrsLikType) in usrsByLinkType">

                <q-item>

                    <q-item-section class="q-mt-md">
                        {{ dictLinkType[usrsLikType[0].linkType] }}
                    </q-item-section>


                </q-item>

                <q-separator/>

                <template v-for="usr in usrsLikType">

                    <LinkItem :usr="usr"/>

                </template>

            </template>


            <template v-if="usrsTo.length > 0">

                <q-item>

                    <q-item-section class="q-mt-md">
                        Запросы на добавление
                    </q-item-section>

                </q-item>

                <q-separator/>

                <template v-for="usr in usrsTo">

                    <LinkItem :usr="usr"/>

                </template>

            </template>


            <template v-if="usrsFrom.length > 0">

                <q-item>

                    <q-item-section class="q-mt-md">
                        Ожидают ответа
                    </q-item-section>

                </q-item>

                <q-separator/>

                <template v-for="usr in usrsFrom">

                    <LinkItem :usr="usr"/>

                </template>

            </template>


        </q-list>


        <div v-else
             class="q-pt-md rgm-state-text">
            {{ messageNoItems }}
        </div>

    </template>


    <template v-if="this.splitLinkType === false">

        <q-list v-if="usrsLength > 0">

            <template v-for="usr in usrsOther">

                <LinkItem :usr="usr"/>

            </template>

        </q-list>


        <div v-else
             class="q-pt-md rgm-state-text">
            {{ messageNoItems }}
        </div>

    </template>


</template>

<script>

import dbConst from "../dao/dbConst"
import LinkItem from "./LinkItem"
import auth from "../auth"

export default {

    components: {
        LinkItem
    },

    props: {
        usrs: {
            type: Array,
            default: [],
        },
        messageNoItems: {
            type: String,
            default: "Список пуст"
        },
        linkTypes: {
            type: Array,
            default: null,
        },
        splitLinkType: {
            type: Boolean,
            default: true,
        },
    },

    data() {
        return {
            usrsByLinkType: {},
            usrsFrom: [],
            usrsTo: [],

            usrsOther: [],

            usrsLength: 0, // облегчает показ, если показывать из нескольких источников

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

    watch: {
        usrs: {
            handler(val, oldVal) {
                this.prepareUserData()
            },
            immediate: true,
            //////////////////////////////////
            //////////////////////////////////
            //////////////////////////////////
            //////////////////////////////////
            // todo написать, зачем immediate: true,
        }

    },

    computed: {},

    methods: {

        prepareUserData() {
            this.usrsFrom = []
            this.usrsTo = []
            this.usrsOther = []
            this.usrsLength = 0

            if (this.splitLinkType) {

                let userInfo = auth.getUserInfo()

                for (let usr of this.usrs) {
                    // Ести указаны конкретные linkTypes - фильтруем по ним
                    if (this.linkTypes && !this.linkTypes.includes(usr.linkType)) {
                        continue
                    }

                    if (usr.confirmState === dbConst.ConfirmState_accepted) {
                        this.usrsOther.push(usr)
                    } else {
                        if (usr.usrFrom === userInfo.id) {
                            this.usrsFrom.push(usr)
                        } else if (usr.usrTo === userInfo.id) {
                            this.usrsTo.push(usr)
                        }
                    }

                    this.usrsLength = this.usrsLength + 1
                }

                //
                this.usrsByLinkType = this.splitBy(this.usrsOther, "linkType")

            } else {

                for (let usr of this.usrs) {
                    // Ести указаны конкретные linkTypes - фильтруем по ним
                    if (this.linkTypes && !this.linkTypes.includes(usr.linkType)) {
                        continue
                    }

                    this.usrsOther.push(usr)

                    this.usrsLength = this.usrsLength + 1
                }

            }
        },

        splitBy(recs, fieldName) {
            let res = {}

            for (let rec of recs) {
                let byVal = rec[fieldName]
                let byValRecs = res[byVal]
                if (!byValRecs) {
                    res[byVal] = []
                    byValRecs = res[byVal]
                }
                byValRecs.push(rec)
            }

            return res;
        },

    },

    async mounted() {
    },

}

</script>


<style>

</style>