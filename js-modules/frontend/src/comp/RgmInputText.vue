<template>

    <q-input
        dense outlined clearable
        debounce="300"

        v-model="valueInternal"
        @update:modelValue="updateParent('modelValue', $event)"

        :type="inputType"
        :loading="loading"
        :placeholder="placeholder"

        ref="inputText"
    >

        <template v-slot:prepend v-if="!valueInternal">
            <q-icon name="search"/>
        </template>

        <!-- Если родительский компонент захочет поставить что-то в конец -->
        <template v-slot:append v-if="!loading && !valueInternal">
            <slot name="append"/>
        </template>

    </q-input>

</template>

<script>

/**
 * Имеет кнопку "очистить", с правильной реакцией фокуса.
 * Не допускает во введенном значении пробелов по краям и null.
 * Умеет ставить фокус.
 */
export default {

    name: "RgmInputText",


    props: {
        modelValue: String,

        inputType: {type: String, default: "text"},

        loading: {type: Boolean, default: false},
        placeholder: {type: String, default: "Поиск"},
    },


    data() {
        return {
            valueInternal: this.modelValue || "",
        }
    },


    computed: {

    },


    watch: {

        valueInternal(valueNow, valuePrior) {
            // Использование атрибута clearable в q-input даёт кнопку "стереть",
            // которая не возвращает фокус в инпут - вернем сами
            if (valueNow === null) {
                this.setFocusInputText()
            }
        }

    },


    methods: {

        focus() {
            this.setFocusInputText()
        },

        setFocusInputText() {
            this.$refs.inputText?.focus()
        },

        updateParent(valueModelName, value) {
            if (value === null) {
                value = ""
            }
            value = value.trim();

            //
            this.$emit('update:' + valueModelName, value)
        },


    },

}

</script>

<style scoped>

</style>