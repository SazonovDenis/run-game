<template>

    <div class="row filter-bar">

        <q-btn
            v-show="!showSearch"
            @click="elFilterText_show()" color="primary"

            class="q-mx-sm filter-bar-btn-search"
            :outline="true"
            icon="search"
        />

        <RgmInputText
            v-show="showSearch"
            v-model="input_filterText"
            @update:modelValue="updateParent('filterText', $event)"
            @blur="elFilterText_blur"
            ref="elFilterText"

            class="q-mx-sm"
            style="max-width: 10em; flex-grow: 10;"
            placeholder="Название"
        />


        <!--
        todo: заменить на более компактный, когда появится смысл сортировать
        (сейчас планов не так много)
        <q-btn-dropdown
            v-if="showFullFilter"
            @click="sortFieldIsDropdown=true"
            v-model="sortFieldIsDropdown"

            class="q-mr-sm"
            style="width: 10em;"
            color="grey-2"
            text-color="black"
            no-caps split
            align="left"
            :icon="sortFieldIcon[input_sortField]"
            :label="sortFieldText[input_sortField]"
        >
            <q-list class="q-pa-sm">

                <q-item class="q-py-md" clickable v-close-popup
                        @click="setSortField('ratingDesc')">
                    Легкие
                </q-item>

                <q-item class="q-py-md" clickable v-close-popup
                        @click="setSortField('ratingAsc')">
                    Сложные
                </q-item>

            </q-list>
        </q-btn-dropdown>
        -->

        <q-space/>

        <div class="row q-mr-sm q-gutter-x-xs">

            <q-btn
                class="_q-my-sm"
                @click="toggleValueFavourite()"
                :color="this.input_favourite !== true ? 'green-9' : 'green'"
                :outline="this.input_favourite !== true"
                icon="star"
            />

            <q-separator vertical/>

            <q-btn
                class="_q-my-sm"
                @click="toggleTag_TrueNull('kaz')"
                color="primary"
                :outline="getOutline('kaz')"
                label="Каз"
            />
            <q-btn
                class="_q-my-sm"
                @click="toggleTag_TrueNull('eng')"
                color="primary"
                :outline="getOutline('eng')"
                label="Анг"
            />

            <q-btn
                class="_q-my-sm"
                @click="toggleTag_TrueFalseNull('word-sound')" color="orange-10"
                :outline="getOutline_WordSound()"
                :icon="getIcon_WordSound()"
            />

        </div>

    </div>

</template>

<script>

import RgmInputText from "./RgmInputText"

export default {

    name: "TaskListFilterBar",

    components: {
        RgmInputText,
    },

    props: {
        //showFullFilter: Boolean,

        filterText: String,
        sortField: String,
        tags: Object,
        favourite: Boolean,
    },

    data() {
        return {
            input_filterText: this.filterText || "",
            input_sortField: this.sortField || "",
            input_tags: this.tags || {},
            input_favourite: this.favourite || false,

            sortFieldIsDropdown: false,
            sortFieldText: {
                ratingDesc: "Легкие",
                ratingAsc: "Сложные",
            },
            sortFieldIcon: {
                ratingDesc: "quasar.arrow.up",
                ratingAsc: "quasar.arrow.down",
            },

            showSearch: false,
        }
    },

    methods: {

        elFilterText_blur() {
            if (!this.input_filterText) {
                this.showSearch = false
            }
        },
        async elFilterText_show() {
            this.showSearch = !this.showSearch
            if (this.showSearch) {
                await this.$nextTick()
                this.$refs.elFilterText.focus()
            }
        },

        getOutline(lang) {
            return !this.input_tags[lang]
        },

        getOutline_WordSound() {
            let tag = "word-sound"
            if (this.input_tags[tag] === false) {
                return false
            } else if (this.input_tags[tag] === true) {
                return false
            } else {
                return true
            }
        },

        getIcon_WordSound() {
            let tag = "word-sound"
            if (this.input_tags[tag] === false) {
                return "headphones-cross"
            } else if (this.input_tags[tag] === true) {
                return "headphones"
            } else {
                return "headphones"
            }
        },

        toggleTag_TrueFalseNull(tag) {
            if (this.input_tags[tag] === false) {
                delete this.input_tags[tag]
            } else if (this.input_tags[tag] === true) {
                this.input_tags[tag] = false
            } else {
                this.input_tags[tag] = true
            }
            //
            this.input_tags.tagLastClicked = tag
            //
            this.updateParent('tags', this.input_tags)
        },

        toggleTag_TrueNull(tag) {
            if (this.input_tags[tag] === true) {
                delete this.input_tags[tag]
            } else {
                this.input_tags[tag] = true
            }
            //
            this.input_tags.tagLastClicked = tag
            //
            this.updateParent('tags', this.input_tags)
        },

        toggleValueFavourite() {
            if (this.input_favourite === true) {
                this.input_favourite = false
            } else {
                this.input_favourite = true
            }
            //
            this.updateParent("favourite", this.input_favourite)
        },

        updateParent(valueModelName, value) {
            this.$emit('update:' + valueModelName, value)
        },

        setSortField(value) {
            this.input_sortField = value
            this.updateParent('sortField', value)
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