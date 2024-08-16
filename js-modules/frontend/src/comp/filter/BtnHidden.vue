<template>

    <q-btn
        class="rgm-style"
        unelevated no-caps
        icon="quasar.chip.selected"
        :label="showHidden ? 'Скрыть известные' : 'Показать известные'"
        :disabled="hiddenCount === 0"
        :color="getColor_showHidden()"
        :text-color="getTextColor_showHidden()"
        :outline="getOutline_showHidden()"
        @click="toggleShowHidden()"
    >
        <q-badge rounded floating
                 v-if="hiddenCount !== 0 && showHidden !== true"
                 :color="getColor_showHidden()"
                 :label="'+' + hiddenCount"
        />
    </q-btn>

</template>

<script>

export default {

    name: "BtnHidden",

    props: {
        showHidden: {type: Boolean, default: false},
        hiddenCount: {type: Number, default: 0},
        emits: ["update:showHidden"]
    },

    methods: {

        getOutline_showHidden() {
            if (this.hiddenCount === 0) {
                return true
            }
            return this.showHidden !== true
        },

        getColor_showHidden() {
            if (this.showHidden !== true) {
                return "indigo-8"
            } else {
                return "indigo-2"
            }
        },

        getTextColor_showHidden() {
            return "indigo-10"
        },

        toggleShowHidden() {
            let showHidden = this.showHidden
            if (showHidden === true) {
                showHidden = false
            } else {
                showHidden = true
            }
            //
            this.$emit("update:showHidden", showHidden)
        },

    }


}

</script>

<style scoped>

</style>