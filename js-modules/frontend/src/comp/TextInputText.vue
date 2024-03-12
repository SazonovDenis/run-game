<template>

    <div class="row q-ma-sm">

        <q-input
            dense outlined clearable
            debounce="300"
            ref="filterText"
            v-model="filterText"
            placeholder="Поиск"
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
        }
    },

    methods: {},

    async mounted() {
        this.$refs.filterText.focus()
    },

    watch: {

        async filterText(valueNow, valuePrior) {
            // Установка daoApi.waitShow = false и оборачивание в try/finally - чтобы не дергался фокус
            let resApi
            try {
                daoApi.waitShow = false
                resApi = await daoApi.loadStore("m/Game/findItems", [valueNow, this.planId])
            } finally {
                daoApi.waitShow = true
            }

            // Заполним родительский список
            let items = resApi.records
            //
            this.items.length = 0
            for (let item of items) {
                this.items.push(item)
            }

            // Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange()
            }
        },

    },

}

</script>

<style scoped>

</style>