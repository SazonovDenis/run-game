<template>

    <div class="row filter-bar _q-gutter-x-sm">

        <template v-if="edFilter">

            <q-btn
                v-show="!filterTextIsExpanded"
                @click="elFilterText_show()" color="primary"

                class="q-mx-sm filter-bar-btn-search"
                :outline="true"
                icon="search"
            />

            <RgmInputText
                class="q-mx-sm rgm-item-grow"
                placeholder="Поиск в плане"

                v-show="filterTextIsExpanded"
                :value="filterText"
                @update:modelValue="updateParent('filterText', $event)"

                @blur="elFilterText_blur"
                ref="elFilterText"
            />

            <q-space v-if="!filterTextIsExpanded"/>

        </template>


        <CbSelect
            style="width: 10em;"

            v-if="edSort"
            :options="cbValues"
            :value="sortField"
            @update:value="updateParent('sortField', $event)"
        />

        <Tags
            v-if="edTags"
            :tags="tags"
            :tagsKeys="tagsKeys"
            :onTagsChange="onTagsChange"
        />

        <Hidden
            class="q-mx-sm"
            v-if="edHidden"
            :hiddenCount="hiddenCount"
            :showHidden="showHidden"
            @update:showHidden="updateParent('showHidden', $event)"
        />


    </div>

</template>

<script>

import RgmInputText from "./RgmInputText"
import Tags from "./filter/Tags"
import CbSelect from "./filter/CbSelect"
import Hidden from "./filter/Hidden"

/**
 * Панель фильтрации по тексту с сортировкой.
 * Полезна при редактировании плана и поиске слов.
 */
export default {

    name: "TaskListFilterBar",

    components: {
        RgmInputText,
        Tags, CbSelect, Hidden,
    },


    props: {
        filterText: String,
        sortField: null,

        tags: {type: Object, default: {}},
        showHidden: {type: Boolean, default: false},

        onTagsChange: {type: Function, default: null},

        hiddenCount: 0,

        /* Какие части панели показать */
        edFilter: {type: Boolean, default: false},
        edSort: {type: Boolean, default: false},
        edHidden: {type: Boolean, default: false},
        edTags: {type: Boolean, default: false},

        emits: ["update:showHidden"]
    },

    data() {
        return {
            inputFilterText: this.filterText || "",
            inputSortField: this.sortField || "",

            tagsKeys: ["kaz", "eng"],

            cbValues: [
                {value: "question", text: "Слово", icon: "quasar.arrow.down"},
                {value: "answer", text: "Перевод", icon: "quasar.arrow.down"},
                {value: "ratingDesc", text: "Легкие", icon: "quasar.arrow.up"},
                {value: "ratingAsc", text: "Сложные", icon: "quasar.arrow.down"},
            ],

            filterTextIsExpanded: false,

            sortFieldIsDropdown: false,
            sortFieldText: {
                question: "Слово",
                answer: "Перевод",
                ratingDesc: "Легкие",
                ratingAsc: "Сложные",
            },
            sortFieldIcon: {
                question: "quasar.arrow.down",
                answer: "quasar.arrow.down",
                ratingDesc: "quasar.arrow.up",
                ratingAsc: "quasar.arrow.down",
            },

        }
    },

    methods: {

        elFilterText_blur() {
            if (!this.filterText) {
                this.filterTextIsExpanded = false
            }
        },

        async elFilterText_show() {
            this.filterTextIsExpanded = !this.filterTextIsExpanded
            if (this.filterTextIsExpanded) {
                await this.$nextTick()
                this.$refs.elFilterText.focus()
            }
        },

        updateParent(valueModelName, value) {
            this.$emit("update:" + valueModelName, value)
        },

        setSortField(value) {
            this.inputSortField = value
            this.updateParent("sortField", value)
        },

    },


}

</script>


<style scoped>

.filter-bar {
    height: 3.1em;
}

</style>