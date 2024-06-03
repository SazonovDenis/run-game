<template>

    <div class="row q-ma-sm">

        <RgmInputText
            :type="getTypeInput()"
            :class="getClassInput()"
            :loading="filterTextLoading"
            placeholder="Поиск в словаре"

            v-model="filterText"

            ref="filterText"
        >

            <!-- Если родительский компонент захочет поставить что-то в конец -->
            <template v-slot:append>
                <slot name="append"/>
            </template>

        </RgmInputText>

        <slot name="toolbar"></slot>

    </div>

</template>

<script>

import {daoApi} from "../dao"
import ctx from "../gameplayCtx"
import RgmInputText from "./RgmInputText"

export default {

    components: {
        RgmInputText,
    },

    props: {
        planId: null,
        items: {type: Array, default: []},
        isToolbarUsed: {type: Boolean},
    },

    data() {
        return {
            filterText: "",
            filterTextLoading: false,
        }
    },

    computed: {

        filterTextShouldLoad() {
            return this.filterText && this.filterText.length >= 2
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

        doFocus() {
            // Только через setTimeout удается добиться попадания фокуса на input,
            // т.к. появлению нашего компонента в DOM мешают async-вызовы у родителя,
            // из-за чего после нашего mounted родительский компонент еще не прорисован.
            // Если async-загрузка родителя будет очень долгой, то и 500мс не хватит!
            setTimeout(this.setFocusFilterText, 500);
        },

        setFocusFilterText() {
            this.$refs.filterText?.focus()
        },

    },

    async mounted() {
        ctx.eventBus.on('itemsCleared', this.onItemsCleared)

        //
        this.doFocus()
    },

    unmounted() {
        ctx.eventBus.off('itemsCleared', this.onItemsCleared)
    },

    watch: {

        async filterText(filterTextNow, filterTextPrior) {
            // Новый поиск
            let items = []

            //
            if (this.filterTextShouldLoad) {
                let resApi = await daoApi.loadStore("m/Game/findItems", [filterTextNow, this.planId], {
                    waitShow: false,
                    onRequestState: (requestState) => {
                        if (requestState === "start") {
                            this.filterTextLoading = true
                        } else {
                            this.filterTextLoading = false
                        }

                        // Уведомим родителя
                        this.$emit("itemsLoading", this.filterTextLoading)
                    }
                })

                // Нашли
                items = resApi.records

                // Внешние условия поменялись - отбросим результаты.
                // Это важно, если пользователь печатает чаще, чем приходит ответ от сервера,
                // (параметр debounce меньше, чем время ответа сервера).
                // Тогда сюда приходят УСТАРЕВШИЕ результаты, которые надо отбросить в надежде на то,
                // что ввод пользователя иницирует запрос к сервру с АКТУАЛЬНЫМИ параметрами.
                if (filterTextNow !== this.filterText) {
                    return
                }
            }

            // Заполним родительский список
            this.items.length = 0
            for (let item of items) {
                this.items.push(item)
            }

            // Уведомим родителя
            this.$emit("itemsChange", this.filterTextShouldLoad)
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