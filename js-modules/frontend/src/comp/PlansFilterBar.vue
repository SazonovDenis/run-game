<template>

    <div class="row q-mb-sm">

        <RgmInputText
            v-if="showFullFilter"
            v-model="inputFilterText"
            @update:modelValue="updateParent('filterText', $event)"

            class="q-mx-sm"
            style="max-width: 10em; flex-grow: 10;"
            placeholder="Название"
        />


        <!--
        todo: заменить на более коппактный, когда появится смысл сортировать
        (сейчас планов не так много)
        -->
        <q-btn-dropdown
            _v-if="showFullFilter"
            v-if="false"
            @click="sortFieldIsDropdown=true"
            v-model="sortFieldIsDropdown"

            class="q-mr-sm"
            style="width: 10em;"
            color="grey-2"
            text-color="black"
            no-caps split
            align="left"
            :icon="sortFieldIcon[inputSortField]"
            :label="sortFieldText[inputSortField]"
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

        <q-space/>

        <div class="row q-mr-sm q-gutter-x-xs">

            <q-btn
                @click="toggleTag('kaz')" color="primary"
                :outline="getOutline('kaz')"
                class="q-my-sm" _size="sm"
                label="Каз"
            />
            <q-btn
                @click="toggleTag('eng')" color="primary"
                :outline="getOutline('eng')"
                class="q-my-sm" _size="sm"
                label="Анг"
            />

            <q-btn
                @click="toggleTag('word-sound')" color="green-8"
                :outline="getOutline('word-sound')"
                class="q-my-sm" _size="sm"
                label="Звук"
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
        showFullFilter: Boolean,

        filterText: String,
        sortField: null,
        tags: Object,
    },

    data() {
        return {
            inputFilterText: this.filterText || "",
            inputSortField: this.sortField || "",
            inputTags: this.tags || {},

            sortFieldIsDropdown: false,
            sortFieldText: {
                ratingDesc: "Легкие",
                ratingAsc: "Сложные",
            },
            sortFieldIcon: {
                ratingDesc: "quasar.arrow.up",
                ratingAsc: "quasar.arrow.down",
            },

        }
    },

    methods: {

        getOutline(lang) {
            return !this.inputTags[lang]
        },

        toggleTag(tag) {
            if (this.inputTags[tag]) {
                delete this.inputTags[tag];
            } else {
                this.inputTags[tag] = true
            }
            //
            this.inputTags.clickedTag = tag
            //
            this.updateParent('tags', this.inputTags, tag)
        },

        updateParent(valueModelName, value) {
            this.$emit('update:' + valueModelName, value)
        },

        setSortField(value) {
            this.inputSortField = value
            this.updateParent('sortField', value)
        },

    },


}
</script>


<style scoped>

</style>