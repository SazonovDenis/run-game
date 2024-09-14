<template>

    <q-btn
        class="rgm-style"
        unelevated no-caps
        icon="quasar.chip.selected"
        :disabled="disabled || hiddenCount === 0"
        :label="getLabel_showHidden()"
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
        disabled: false,
        showHidden: {type: Boolean, default: false},
        hiddenCount: {type: Number, default: 0},
        compact: {type: Boolean, default: false},
        emits: ["update:showHidden"]
    },

    methods: {

        getOutline_showHidden() {
            if (this.hiddenCount === 0) {
                return true
            }
            return this.showHidden !== true
        },

        getLabel_showHidden() {
            if (this.compact && this.isMobile()) {
                return null
            } else {
                return this.showHidden ? 'Скрыть известные' : 'Показать известные'
            }
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

        isMobile() {
            return Jc.cfg.is.mobile
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