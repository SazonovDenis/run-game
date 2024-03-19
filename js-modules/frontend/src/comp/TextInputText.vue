<template>

    <div class="row q-ma-sm">

        <q-input
            dense outlined clearable
            debounce="300"
            :type="getTypeInput()"
            :class="getClassInput()"
            ref="filterText"
            v-model="filterText"
            placeholder="Поиск в словаре"
        >

            <template v-slot:append v-if="!filterText">
                <q-icon name="search"/>
            </template>

        </q-input>


        <slot name="toolbar"></slot>

    </div>

</template>

<script>

import {daoApi} from "../dao"
import MenuContainer from "./MenuContainer"
import {apx} from "../vendor"

export default {

    components: {
        MenuContainer,
    },

    props: {
        planId: null,
        items: {type: Array, default: []},
        itemsOnChange: {type: Function},
    },

    data() {
        return {
            filterText: "",
            searchDone: false,
        }
    },

    methods: {

        getTypeInput() {
            if (apx.cfg.is.desktop) {
                return "textarea"
            } else {
                return "text"
            }
        },

        getClassInput() {
            if (apx.cfg.is.desktop) {
                return "input-desktop"
            } else {
                return "input-mobile"
            }
        },

    },

    async mounted() {
        this.$refs.filterText.focus()
    },

    watch: {

        async filterText(valueNow, valuePrior) {
            this.searchDone = false

            let items = []
            if (valueNow && valueNow.length >= 2) {
                // Установка daoApi.waitShow = false и оборачивание в try/finally - чтобы не дергался фокус
                try {
                    daoApi.waitShow = false
                    //
                    let resApi = await daoApi.loadStore("m/Game/findItems", [valueNow, this.planId])
                    items = resApi.records
                    //
                    this.searchDone = true
                } finally {
                    daoApi.waitShow = true
                }
            }

            // Заполним родительский список
            this.items.length = 0
            for (let item of items) {
                this.items.push(item)
            }

            // Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange(this.searchDone)
            }
        },

    },

}

</script>

<style scoped>

.input-desktop {
    width: 100%;
}

.input-mobile {
    max-width: 12em;
}

</style>