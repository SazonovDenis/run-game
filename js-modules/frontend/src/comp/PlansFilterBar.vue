<template>

    <div class="row q-mb-sm">

        <RgmInputText
            class="q-mx-sm"
            style="max-width: 12em; flex-grow: 10;"
            placeholder="Поиск уровня"

            v-model="inputFilterText"
            @update:modelValue="updateParent('filterText', $event)"
        />


        <q-btn-dropdown
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
        filterText: String,
        sortField: null,
    },

    data() {
        return {
            inputFilterText: this.filterText || "",
            inputSortField: this.sortField || "",

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