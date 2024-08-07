<template>

    <div class="row q-mb-sm">

        <RgmInputText
            v-model="inputFilterText"
            @update:modelValue="updateParent('filterText', $event)"

            class="q-mx-sm"
            style="max-width: 8em"
            placeholder="Поиск"
        />


        <q-btn-dropdown
            @click="sortFieldIsDropdown=true"
            v-model="sortFieldIsDropdown"

            style="width: 10em;"
            color="grey-2"
            text-color="black"
            no-caps split
            align="left"
            :icon="sortFieldIcon[inputSortField]"
            :label="sortFieldText[inputSortField]"
        >
            <q-list class="q-pa-sm">

                <q-item
                    class="q-py-md" clickable v-close-popup
                    @click="setSortField('question')">
                    Слово
                </q-item>

                <q-item class="q-py-md" clickable v-close-popup
                        @click="setSortField('answer')">
                    Перевод
                </q-item>

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


        <q-toggle
            v-if="hiddenCount > 0"
            :label="'Известные (' + hiddenCount + ')'"
            v-model="inputShowHidden"
            @update:modelValue="updateParent('showHidden', $event)"
        />

    </div>

</template>

<script>

import RgmInputText from "./RgmInputText"

/**
 * Панель фильтрации по тексту с сортировкой.
 * Полезна при редактировании плана.
 */
export default {

    name: "TaskListFilterBar",

    components: {
        RgmInputText
    },


    props: {
        filterText: String,
        sortField: null,
        showHidden: false,
        hiddenCount: 0,
    },

    data() {
        return {
            inputFilterText: this.filterText || "",
            inputSortField: this.sortField || "",
            inputShowHidden: this.showHidden || false,

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