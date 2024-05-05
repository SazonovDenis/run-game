<template>

    <q-list>

        <template v-for="usrsLikType in usrsByLinkType">


            <q-item class="item-first">

                <q-item-section>
                    {{ dictLinkType[usrsLikType[0].linkType] }}
                </q-item-section>


            </q-item>

            <template v-for="usr in usrsLikType">


                <q-item class="_item-first"
                        clickable v-ripple
                >

                    <q-item-section top avatar>

                        <q-avatar
                            icon="star"
                            color="grey-2"
                            text-color="yellow-8">

                            <div class="plan-count-text">
                                {{ usr.linkType }}
                            </div>

                        </q-avatar>


                    </q-item-section>

                    <q-item-section>
                        {{ usr.text }}
                    </q-item-section>


                    <q-item-section top side>

                        <div class="text-grey-8 q-gutter-xs">

                            <q-btn
                                dense round flat
                                size="1.2em"
                                icon="del"
                                color="grey-8"
                                @click="requestIns(usr)"
                            />

                            <q-btn flat dense round
                                   icon="more-h"
                                   size="1.0em"
                            />

                        </div>

                    </q-item-section>

                </q-item>

            </template>

        </template>

    </q-list>

</template>

<script>

import MenuContainer from "../comp/MenuContainer"
import {daoApi} from "../dao"
import dbConst from "../dao/dbConst"

export default {

    components: {
        MenuContainer,
    },

    props: {
        usrs: {
            type: Array,
            default: [],
        },
    },

    data() {
        return {
            usrsByLinkType: {},

            dictLinkType: {
                [dbConst.LinkType_friend]: "Ваши друзья",
                [dbConst.LinkType_parent]: "Ваши родители",
                [dbConst.LinkType_child]: "Ваши дети",
                [dbConst.LinkType_teacher]: "Ваши учителя",
                [dbConst.LinkType_student]: "Ваши ученики",
            },
        }
    },

    watch: {
        usrs: {
            handler(val, oldVal) {
                let usrsByLinkType = this.splitBy(this.usrs, "linkType")
                this.usrsByLinkType = this.splitBy(this.usrs, "linkType")
            },
            immediate: true,
        }

    },

    computed: {
        isDesktop() {
            return Jc.cfg.is.desktop
        },
    },

    methods: {

        async requestIns(link) {
            await daoApi.invoke('m/Link/requestIns', [link])
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