<template>

    <q-btn
        class="rgm-style"
        unelevated no-caps
        :icon="maskAnswer ? 'visibility-off' : 'visibility'"
        :label="getLabel_maskAnswer()"
        :color="getColor_maskAnswer()"
        :text-color="getTextColor_maskAnswer()"
        :outline="getOutline_maskAnswer()"
        @click="toggleMaskAnswer()"
    />

</template>

<script>

export default {

    name: "BtnMaskAnswer",

    props: {
        maskAnswer: {type: Boolean, default: false},
        compact: {type: Boolean, default: false},
        emits: ["update:maskAnswer", "click"],
    },

    methods: {

        getOutline_maskAnswer() {
            return this.maskAnswer !== true
        },

        getLabel_maskAnswer() {
            if (this.compact && this.isMobile()) {
                return null
            } else {
                return this.maskAnswer ? 'Показать ответы' : 'Скрыть ответы'
            }
        },

        getColor_maskAnswer() {
            if (this.maskAnswer !== true) {
                return "lime-7"
            } else {
                return "lime-2"
            }
        },

        getTextColor_maskAnswer() {
            return "lime-10"
        },

        isMobile() {
            return Jc.cfg.is.mobile
        },

        toggleMaskAnswer() {
            let maskAnswer = this.maskAnswer
            if (maskAnswer === true) {
                maskAnswer = false
            } else {
                maskAnswer = true
            }
            //
            this.$emit("update:maskAnswer", maskAnswer)
        },

    }


}

</script>

<style scoped>

</style>