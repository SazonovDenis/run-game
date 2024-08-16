<template>

    <div class="row q-gutter-x-xs">

        <template v-for="tagKey in tagsKeys">

            <q-btn
                no-caps
                :color="getBtnColor(tagKey)"
                :text-color="getTextColor(tagKey)"
                :outline="getOutline(tagKey)"
                :label="utils.Langs_text[tagKey]"
                @click="toggle_TrueNull(tagKey)"
            />

        </template>

    </div>

</template>

<script>

import utils from "../../utils"

export default {

    name: "Tags",

    props: {
        tagsKeys: {type: Array, default: []},
        onTagsChange: {type: Function, default: null},

        tags: {type: Object, default: {}},
    },

    setup() {
        return {utils}
    },

    methods: {

        getBtnColor(lang) {
            if (!this.tags) {
                return null
            }
            //
            if (this.tags[lang]) {
                return utils.Langs_color_bth[lang]
            } else {
                return utils.Langs_color_btn_outline[lang]
            }
        },

        getTextColor(lang) {
            if (!this.tags) {
                return null
            }
            //
            if (this.tags[lang]) {
                return utils.Langs_color_btn_outline_dark[lang]
            } else {
                return utils.Langs_color_btn_outline[lang]
            }
        },

        getOutline(lang) {
            if (!this.tags) {
                return false
            }
            //
            return !this.tags[lang]
        },

        toggle_TrueNull(tag) {
            let tags = this.tags
            //
            if (tags[tag] === true) {
                delete tags[tag]
            } else {
                tags[tag] = true
            }

            // Позволяет родительскому компоненту доработать напильником значения,
            // во исполнение его бизнес-логики
            if (this.onTagsChange) {
                this.onTagsChange(tags, tag)
            }

            //
            //this.updateParent("tags", tags)
            this.$emit("update:tags", tags)
        },

    },

}
</script>

<style scoped>

</style>