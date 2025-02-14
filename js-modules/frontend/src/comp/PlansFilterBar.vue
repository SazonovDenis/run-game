<template>

    <div class="row filter-bar">

        <q-btn
            v-show="!filterTextIsExpanded"
            @click="elFilterText_show()" color="primary"

            class="q-mx-sm filter-bar-btn-search"
            :outline="true"
            icon="search"
        />

        <RgmInputText
            v-show="filterTextIsExpanded"
            :value="filterText"
            @update:modelValue="updateParent('filterText', $event)"
            @blur="elFilterText_blur"
            ref="elFilterText"

            class="q-mx-sm rgm-item-grow"
            placeholder="Название"
        />

        <q-space v-if="!filterTextIsExpanded"/>

        <div class="row q-mr-sm q-gutter-x-xs">

            <q-btn
                style="min-width: 3rem;"
                @click="toggleFavourite()"
                :color="this.favourite !== true ? 'green-9' : 'green'"
                :outline="this.favourite !== true"
                icon="star"
            />

            <Tags
                :tags="tags"
                :tagsKeys=utils.Langs_keys
                :onTagsChange="onTagsChange"
            />

            <q-btn
                style="min-width: 3rem;"
                @click="toggleTag_TrueFalseNull('word-sound')" color="orange-10"
                :outline="getOutline_WordSound()"
                :icon="getIcon_WordSound()"
            />

        </div>

    </div>

</template>

<script>

import utils from "../utils"
import RgmInputText from "./RgmInputText"
import Tags from "./filter/Tags"
import CbSelect from "./filter/CbSelect"

export default {

    name: "PlansFilterBar",

    components: {
        CbSelect,
        RgmInputText, Tags,
    },

    setup() {
        return {utils}
    },

    props: {
        filterText: {type: String, default: ""},
        sortField: {type: String, default: ""},

        tags: {type: Object, default: {}},
        favourite: {type: Boolean, default: false},

        onTagsChange: {type: Function, default: null},
    },

    data() {
        return {
            filterTextIsExpanded: false,
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

        getOutline_WordSound() {
            let tag = "word-sound"
            if (this.tags[tag] === false) {
                return false
            } else if (this.tags[tag] === true) {
                return false
            } else {
                return true
            }
        },

        getIcon_WordSound() {
            let tag = "word-sound"
            if (this.tags[tag] === false) {
                return "headphones-cross"
            } else if (this.tags[tag] === true) {
                return "headphones"
            } else {
                return "headphones"
            }
        },

        toggleTag_TrueFalseNull(tag) {
            //
            let tags = this.tags
            //
            if (tags[tag] === false) {
                delete tags[tag]
            } else if (tags[tag] === true) {
                tags[tag] = false
            } else {
                tags[tag] = true
            }
            //
            this.updateParent("tags", tags)
        },

        toggleFavourite() {
            let favourite = this.favourite
            if (favourite === true) {
                favourite = false
            } else {
                favourite = true
            }
            //
            this.updateParent("favourite", favourite)
        },

        setSortField(value) {
            this.updateParent("sortField", value)
        },

        updateParent(valueModelName, value) {
            this.$emit("update:" + valueModelName, value)
        },

    },


}
</script>


<style scoped>

.filter-bar {
    height: 3.1em;
}

.filter-bar-btn-search {
    height: 3rem !important;
}

</style>