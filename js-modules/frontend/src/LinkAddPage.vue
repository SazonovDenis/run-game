<template>

    <MenuContainer
        title="Добавление связи"
    >
        <div class="q-mt-lg q-mb-lg rgm-state-text">
            <div>
                Добавить пользователя &laquo;{{ this.usr.text }}&raquo;
            </div>
            <div>
                в {{ utils.dictLinkTypeFrom_text[this.linkType].toLowerCase() }}?
            </div>
        </div>

        <div class="row q-mx-md q-gutter-sm justify-center">

            <jc-btn
                no-caps
                kind="secondary"
                label="Отмена"
                @click="cancelLink()"
            />

            <jc-btn
                no-caps
                kind="primary"
                label="Установить связь"
                @click="addLink()"
            />

        </div>

    </MenuContainer>

</template>


<script>

import {daoApi} from "./dao"
import {apx} from "./vendor"
import gameplay from "./gameplay"
import dbConst from "./dao/dbConst"
import utils from "./utils"
import MenuContainer from "./comp/MenuContainer"

export default {

    name: "LinkAddPage",

    components: {
        MenuContainer, gameplay
    },

    setup(props) {
        return {dbConst, utils}
    },

    props: {
        usrTo: {type: String, default: null},
        linkType: {type: String, default: null},
    },

    data() {
        return {
            usr: {},
        }
    },

    methods: {
        async addLink() {
            let link = {
                usrTo: this.usrTo,
                linkType: this.linkType,
            }
            await daoApi.invoke('m/Link/request', [link])

            apx.showFrame({
                frame: "/link",
                props: {}
            })
        },

        cancelLink() {
            apx.showFrame({
                frame: "/link",
                props: {}
            })
        },
    },

    computed: {},

    async mounted() {
        let res = await daoApi.invoke("m/Usr/getUserPublicInfo", [this.usrTo])
        this.usr.id = res.id
        this.usr.text = res.text
    },

    unmounted() {
    },

}

</script>

<style lang="less" scoped>


</style>