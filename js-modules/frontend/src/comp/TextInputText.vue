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
import ctx from "run-game-frontend/src/gameplayCtx"

export default {

    components: {
        MenuContainer,
    },

    props: {
        planId: null,
        items: {type: Array, default: []},
        itemsOnChange: {type: Function},
        isToolbarUsed: {type: Boolean},
    },

    data() {
        return {
            filterText: "",
            searchDone: false,
        }
    },

    methods: {

        getTypeInput() {
            if (Jc.cfg.is.desktop) {
                return "textarea"
            } else {
                return "text"
            }
        },

        getClassInput() {
            if (Jc.cfg.is.desktop || !this.isToolbarUsed) {
                return "input-wide"
            } else {
                return "input-narrow"
            }
        },

        onItemsCleared() {
            // Фильтр очищаем, ведь ищем заново
            this.filterText = ""
        },

        doFocusFilterText() {
            let elFilterText = this.$refs.filterText
            if (elFilterText) {
                elFilterText.focus()
            }
        },

    },

    async mounted() {
        ctx.eventBus.on('itemsCleared', this.onItemsCleared)

        // Только через setTimeout удается добиться попадания фокуса на input
        setTimeout(this.doFocusFilterText, 500);
    },

    unmounted() {
        ctx.eventBus.off('itemsCleared', this.onItemsCleared)
    },

    watch: {

        async filterText(valueNow, valuePrior) {
            this.searchDone = false

            let items = []
            if (valueNow && valueNow.length >= 2) {
                let resApi = await daoApi.loadStore("m/Game/findItems", [valueNow, this.planId], {waitShow: false})
                items = resApi.records
                //
                this.searchDone = true
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

.input-wide {
    width: 100%;
}

.input-narrow {
    max-width: 12em;
}

</style>