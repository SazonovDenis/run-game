<template>

    <q-btn-dropdown
        v-model="optionsIsDropdown"
        @click="optionsIsDropdown=true"

        color="grey-2"
        text-color="black"
        no-caps split unelevated
        align="left"
        :icon="icon"
        :label="text"
    >
        <q-list class="q-pa-sm">

            <template v-for="option in options">
                <q-item
                    class="q-py-md" clickable v-close-popup
                    @click="setValue(option.value)">
                    {{ option.text }}
                </q-item>
            </template>

        </q-list>
    </q-btn-dropdown>

</template>

<script>

export default {

    name: "CbSelect",

    props: {
        value: String,
        options: {type: Array, default: []}
    },

    data() {
        return {
            optionsKeys: {},
            optionsIsDropdown: false,
        }
    },

    computed: {

        text() {
            if (!this.value) {
                return null
            }

            let option = this.optionsKeys[this.value]
            if (!option) {
                return null
            }

            return option.text
        },

        icon() {
            if (!this.value) {
                return null
            }

            let option = this.optionsKeys[this.value]
            if (!option) {
                return null
            }

            return option.icon
        },

    },

    methods: {

        updateParent(valueModelName, value) {
            this.$emit("update:" + valueModelName, value)
        },

        setValue(value) {
            this.updateParent("value", value)
        },

        fillOptionsKeys() {
            this.optionsKeys = {}
            for (let option of this.options) {
                this.optionsKeys[option.value] = option
            }
        },

    },

    mounted() {
        this.fillOptionsKeys()
    },

}

</script>

<style scoped>

</style>