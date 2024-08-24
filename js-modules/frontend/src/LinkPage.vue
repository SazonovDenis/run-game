<template>

    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
        :loaded="dataLoaded"
        :helpKey="getHelpKey()"
    >

        <!-- -->

        <HelpPanel class="q-mt-none q-mb-none" :helpKey="getHelpKey()"/>

        <!-- -->

        <template v-if="linkText">

            <div class="q-pt-xl q-pb-lg link-text">
                {{ linkText }}
            </div>

            <div class="row justify-center">

                <jc-btn
                    kind="primary"
                    label="Копировать ссылку"
                    @click="copyLink()">
                </jc-btn>

            </div>

        </template>

        <template v-else-if="frameMode === 'find'">

            <RgmInputText
                class="q-ma-sm"

                :loading="filterTextLoading"

                v-model="filterText"
                ref="filterText"

                placeholder="Поиск друзей"
            />

            <q-scroll-area class="q-scroll-area" style="height: calc(100% - 8rem);">

                <LinkList
                    v-if="filterTextShouldLoad"
                    :usrs="usrsFind"
                    :splitLinkType="false"
                    :messageNoItems="filterTextLoading ? '' : 'Пользователь не найден'"
                />

            </q-scroll-area>


            <q-page-sticky position="bottom-right"
                           :offset="[10, 10]">

                <q-btn v-if="filterText === ''"
                       rounded no-caps
                       dropdown-icon=""
                       color="grey-2"
                       text-color="grey-10"
                       size="1.3em"
                       icon="quasar.editor.hyperlink"
                       label="Пригласительная ссылка..."
                       @click.stop
                >
                    <q-menu>

                        <q-list>

                            <q-item v-for="link in utils.dictLinkTypeFromAdd"
                                    clickable v-close-popup
                                    class="q-ma-md"
                                    @click="makeLink(link)"
                            >
                                <q-item-section>
                                    <q-item-label>{{
                                            utils.dictLinkTypeFromLinkAdd_text[link]
                                        }}
                                    </q-item-label>
                                </q-item-section>
                            </q-item>

                        </q-list>

                    </q-menu>

                </q-btn>


            </q-page-sticky>

        </template>

        <template v-else-if="frameMode === 'list-blocked'">

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

                <div class="q-ml-md q-mt-lg q-mb-xl q-mb-xs rgm-link-soft"
                     @click="setFrameMode_list_blocked">Заблокированные пользователи
                </div>

            </q-scroll-area>


            <q-page-sticky position="bottom-right"
                           :offset="[10, 10]">

                <q-btn color="purple"
                       icon="add"
                       size="1.3em"
                       rounded no-caps
                       label="Добавить друзей"
                       @click="setFrameMode_find"
                />

            </q-page-sticky>


        </template>

    </MenuContainer>

</template>

<script>

import {useQuasar} from 'quasar'
import {daoApi} from "./dao"
import {jcBase} from "./vendor"
import ctx from "./gameplayCtx"
import auth from "./auth"
import dbConst from "./dao/dbConst"
import utils from "./utils"
import MenuContainer from "./comp/MenuContainer"
import RgmInputText from "./comp/RgmInputText"
import LinkList from "./comp/LinkList"
import HelpPanel from "./comp/HelpPanel"

export default {

    components: {
        useQuasar,
        MenuContainer,
        RgmInputText, LinkList, HelpPanel,
    },

    setup(props) {
        const $q = useQuasar()

        return {
            dbConst, utils,
            showNotify(message, info) {
                $q.notify({
                    type: 'positive',
                    message: message,
                    caption: info,
                    color: "purple"
                })
            }
        }

    },

    props: {},

    data() {
        return {
            dataLoaded: false,

            usrs: [],
            usrsFind: [],

            frameMode: "list",

            filterText: "",
            filterTextLoading: false,

            linkText: null,
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

        getHelpKey() {
            if (this.linkText) {
                return "help.link.linkText"
            }
        },

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

        makeLink(linkType) {
            let ui = auth.getUserInfo()
            let usrId = ui.id
            let href = "#/linkAdd?usrTo=" + usrId + "&linkType=" + linkType
            this.linkText = document.location.origin + jcBase.url.ref(href)
        },

        async copyLink() {
            await navigator.clipboard.writeText(this.linkText)
            this.linkText = null
            this.showNotify("Ссылка скопирована", "Отправьте её вашим друзьям")
        },

        setFrameMode_list() {
            this.frameMode = "list"

            //
            this.linkText = null
        },

        setFrameMode_list_blocked() {
            this.frameMode = "list-blocked"

            //
            this.linkText = null
        },

        async setFrameMode_find() {
            this.frameMode = "find"

            //
            this.linkText = null

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

.link-text {
    font-size: 1.2em;
    text-align: center;
    color: #6c6c6c;
    user-select: all;
}

</style>