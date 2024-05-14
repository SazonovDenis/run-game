<template>

    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
    >

        <template v-if="frameMode === 'find'">

            <RgmInputText
                class="q-ma-sm"

                :loading="filterTextLoading"

                v-model="filterText"
                ref="filterText"

                placeholder="Поиск друзей"
            />

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 4rem);">

                <LinkList
                    v-if="filterTextShouldLoad"
                    :usrs="usrsFind"
                    :splitLinkType="false"
                    :messageNoItems="filterTextLoading ? '' : 'Пользователь не найден'"
                />

            </q-scroll-area>

        </template>

        <template v-if="frameMode === 'list-blocked'">

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 4rem);">

                <LinkList
                    v-if="dataLoaded"
                    :usrs="usrs"
                    :splitLinkType="false"
                    :linkTypes="[2000]"
                    messageNoItems="У вас нет заблокированных пользователей"
                />

            </q-scroll-area>

        </template>


        <template v-if="frameMode === 'list' && dataLoaded">


            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 4rem);">

                <LinkList
                    v-if="dataLoaded"
                    :usrs="usrs"
                    :splitLinkType="true"
                    :linkTypes="[1001,1002,1003,1004,1005]"
                    messageNoItems="У вас пока нет связей"
                />

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


        </template>

    </MenuContainer>

</template>

<script>

import MenuContainer from "./comp/MenuContainer"
import LinkList from "./comp/LinkList"
import RgmInputText from "./comp/RgmInputText"
import {daoApi} from "./dao"
import ctx from "./gameplayCtx"
import auth from "./auth"

export default {

    components: {
        MenuContainer,
        RgmInputText,
        LinkList,
    },

    props: {},

    data() {
        return {
            usrs: [],
            usrsFind: [],

            dataLoaded: false,

            frameMode: "list",

            filterText: "",
            filterTextLoading: false,
        }
    },

    watch: {

        async filterText(valueNow, valuePrior) {
            // Новый поиск
            this.doFind(valueNow)
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

        filterTextShouldLoad() {
            return this.filterText && this.filterText.length >= 2
        }

    },

    methods: {

        async doFind(filterText) {
            // Ищем
            let usrsFind = []

            //
            if (this.filterTextShouldLoad) {
                let resApi = await daoApi.loadStore("m/Link/usrFind", [filterText], {
                    waitShow: false,
                    onRequestState: (requestState) => {
                        if (requestState === "start") {
                            this.filterTextLoading = true
                        } else {
                            this.filterTextLoading = false
                        }
                    }
                })

                // Нашли
                usrsFind = resApi.records

                // Внешние условия поменялись - отбросим результаты.
                // Это важно, если пользователь печатает чаще, чем приходит ответ от сервера,
                // (параметр debounce меньше, чем время ответа сервера).
                // Тогда сюда приходят УСТАРЕВШИЕ результаты, которые надо отбросить в надежде на то,
                // что ввод пользователя иницирует запрос к сервру с АКТУАЛЬНЫМИ параметрами.
                if (filterText !== this.filterText) {
                    return
                }
            }


            // Запишем в data
            this.usrsFind = usrsFind
        },

        async doLoad() {
            let resApi = await daoApi.loadStore('m/Link/getLinks', [])
            this.usrs = resApi.records
        },

        async onChangeLink(usr) {
            if (this.frameMode === "find") {
                this.doFind(this.filterText)
            }
            this.doLoad()

            // Еще остались неотвеченные входящие запросы?
            // Это нужно для показа уведомлений в главном меню.
            let resApi = await daoApi.loadStore('m/Link/getLinksToWaiting', [])
            //
            let userInfo = auth.getUserInfo()
            userInfo.linksToWait = resApi.records
        },

        setFrameMode_list() {
            this.frameMode = "list"
        },

        setFrameMode_list_blocked() {
            this.frameMode = "list-blocked"
        },

        async setFrameMode_find() {
            this.frameMode = "find"

            //
            this.filterText = ""
            this.usrsFind = []

            // Только после $nextTick filterText появится в DOM
            // и тогда удастся добиться попадания фокуса на input
            await this.$nextTick()
            this.$refs.filterText.focus()
        },

    },

    async mounted() {
        this.dataLoaded = false
        this.usrs = []

        //
        await this.doLoad()

        //
        this.dataLoaded = true


        //
        ctx.eventBus.on("change:link", this.onChangeLink)
    },

    async unmounted() {
        ctx.eventBus.off("change:link", this.onChangeLink)
    },

}

</script>

<style>

</style>