<template>

    <MenuContainer
        :title="getTitle()"
        :frameReturn="getFrameReturn()"
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

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 11rem);">

                <LinkList :usrs="usrsLoaded"/>

            </q-scroll-area>

        </template>


        <template v-if="frameMode === 'list'">

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 11rem);">

                <LinkList :usrs="usrs"/>

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
            usrsLoaded: [],

            filterText: "",
            frameMode: "list",
        }
    },

    watch: {

        async filterText(valueNow, valuePrior) {
            this.usrsLoaded = []

            //
            if (valueNow && valueNow.length >= 2) {
                let resApi = await daoApi.loadStore("m/Link/usrFind", [valueNow], {waitShow: false})
                this.usrsLoaded = resApi.records
            }
        },

    },

    computed: {
        isDesktop() {
            return Jc.cfg.is.desktop
        },
    },

    methods: {

        getTitle() {
            if (this.frameMode === "list") {
                return "Друзья и связи"
            } else {
                return "Добавление друзей"
            }
        },

        getFrameReturn() {
            if (this.frameMode === "list") {
                return null
            } else {
                return this.setFrameMode_list
            }
        },

        async setFrameMode_list() {
            this.frameMode = "list"
        },

        setFrameMode_find() {
            this.frameMode = 'find'

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
        let resApi = await daoApi.loadStore('m/Link/getLinks', [])
        this.usrs = resApi.records
    },

}

</script>

<style>

</style>