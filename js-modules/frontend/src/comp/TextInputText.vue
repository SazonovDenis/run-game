<template>

    <RgmInputText
        class="q-ma-sm rgm-item-grow"
        placeholder="Поиск в словаре"

        :modelValue="text"
        :loading="loading"
        :inputType="getInputType()"
        @update:modelValue="updateParent('text', $event)"

        ref="text"
    >

        <!-- Если родительский компонент захочет поставить что-то в конец -->
        <template v-slot:append>
            <slot name="append"/>
        </template>

    </RgmInputText>

</template>

<script>

import RgmInputText from "./RgmInputText"

export default {

    name: "TextInputText",

    components: {
        RgmInputText,
    },

    props: {
        text: "",
        loading: false,
    },

    methods: {

        getInputType() {
            if (Jc.cfg.is.desktop) {
                return "textarea"
            } else {
                return "text"
            }
        },

        doFocus() {
            // Только через setTimeout удается добиться попадания фокуса на input,
            // т.к. появлению нашего компонента в DOM мешают async-вызовы у родителя,
            // из-за чего после нашего mounted родительский компонент еще не прорисован.
            // Если async-загрузка родителя будет очень долгой, то и 500мс не хватит!
            setTimeout(this.setFocustext, 500);
        },

        setFocustext() {
            this.$refs.text?.focus()
        },

        updateParent(valueModelName, value) {
            this.$emit("update:" + valueModelName, value)
        },

    },

    async mounted() {
        this.doFocus()
    },

}

</script>

<style scoped>

</style>