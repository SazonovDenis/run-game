<template>

    <div class="row q-mb-sm">

        <!--
                <div>
                    <input type="text" v-model="input1" @input="$emit('update:value1', input1)">
                    <input type="text" v-model="inputSecond" @input="updateParentModel">
                    <input type="text" v-model="input3" @input="updateParentModel">
                    <input type="text" v-model="inputFour" @input="updateParentModel($event)">
                    <input type="text" v-model="inputFilterText" @input="updateParent('filterText', $event.target.value)">
                </div>
        -->

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

        /*
                value1: String,
                valueSecond: String,
                value3: String,
                valueFour: String,
        */
    },
    data() {
        return {
            /*
                        input1: this.value1,
                        inputSecond: this.valueSecond,
                        input3: this.value3,
                        inputFour: this.valueFour,
            */

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

        /*
        updateParentModel(event) {
            console.info(event)
            this.$emit('update:valueSecond', this.inputSecond);
            this.$emit('update:value3', this.input3);
            this.$emit('update:valueFour', this.inputFour);
        },
        */

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