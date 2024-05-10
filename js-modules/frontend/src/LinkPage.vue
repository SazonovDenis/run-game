<template>

    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
    >

        <template v-if="frameMode === 'find'">

            <q-input
                v-model="filterText"
                ref="filterText"

                class="q-ma-sm"

                dense outlined clearable
                placeholder="Поиск друзей"
            >

                <template v-slot:append v-if="!filterText">
                    <q-icon name="search"/>
                </template>

            </q-input>

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 4rem);">

                <LinkList v-if="usrsFindLoaded"
                          :usrs="usrsFind"
                          messageNoItems="Пользователей не найдено"
                />

            </q-scroll-area>

        </template>

        <template v-if="frameMode === 'list-blocked'">

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 4rem);">

                <LinkList v-if="dataLoaded"
                          :usrs="[]"
                />

            </q-scroll-area>

        </template>


        <template v-if="frameMode === 'list' && dataLoaded">

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 4rem);">

                <LinkList v-if="dataLoaded" :usrs="usrs"/>

                <div class="q-ml-md q-mt-lg q-mb-xs rgm-link-soft"
                     @click="setFrameMode_list_blocked">Заблокированные пользователи
                </div>

            </q-scroll-area>

            <q-page-sticky position="bottom-right"
                           :offset="[10, 10]">
                <q-btn color="purple"
                       icon="add"
                       size="1.3em"
                       round no-caps
                       @click="setFrameMode_find"
                >
                </q-btn>

            </q-page-sticky>

<!--
            <q-page-sticky position="bottom-left"
                           :offset="[0, 25]">



            </q-page-sticky>
-->

        </template>

    </MenuContainer>

</template>

<script>

import MenuContainer from "./comp/MenuContainer"
import {daoApi} from "./dao"
import LinkList from "./comp/LinkList"

export default {

    components: {
        LinkList,
        MenuContainer,
    },

    props: {},

    data() {
        return {
            usrs: [],
            usrsFind: [],

            dataLoaded: false,
            usrsFindLoaded: false,

            filterText: "",
            frameMode: "list",
        }
    },

    watch: {

        async filterText(valueNow, valuePrior) {
            //
            this.usrsFindLoaded = false
            this.usrsFind = []

            //
            if (valueNow && valueNow.length >= 2) {
                let resApi = await daoApi.loadStore("m/Link/usrFind", [valueNow], {waitShow: false})
                this.usrsFind = resApi.records

                this.usrsFindLoaded = true
            }
        },

    },

    computed: {

        title() {
            if (this.frameMode === "list") {
                return "Друзья и связи"
            } else if (this.frameMode === "find") {
                return "Добавление друзей"
            } else if (this.frameMode === "list-blocked") {
                return "Заблокированные"
            }
        },

        frameReturn() {
            if (this.frameMode === "list") {
                return null
            } else {
                return this.setFrameMode_list
            }
        },

    },

    methods: {

        setFrameMode_list() {
            this.frameMode = "list"
        },

        setFrameMode_list_blocked() {
            this.frameMode = "list-blocked"
        },

        setFrameMode_find() {
            this.frameMode = "find"

            //
            this.filterText = ""
            this.usrsFindLoaded = false
            this.usrsFind = []

            //
            this.doFocus()
        },

        doFocus() {
            // Только через setTimeout удается добиться попадания фокуса на input
            setTimeout(this.doFocusFilterText, 500);
        },

        doFocusFilterText() {
            let elFilterText = this.$refs.filterText
            if (elFilterText) {
                elFilterText.focus()
            }
        },

        async requestIns(link) {
            await daoApi.invoke('m/Link/requestIns', [link])
        },


    },

    async mounted() {
        this.dataLoaded = false

        let resApi = await daoApi.loadStore('m/Link/getLinks', [])
        this.usrs = resApi.records

        this.dataLoaded = true
    },

}

</script>

<style>

</style>