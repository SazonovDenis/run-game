<template>

    <q-btn
        class="rgm-style"
        unelevated no-caps
        icon="visibility-off"
        :_label="maskAnswer ? 'Скрыть ответы' : 'Показать ответы'"
        :color="getColor_maskAnswer()"
        :text-color="getTextColor_maskAnswer()"
        :outline="getOutline_maskAnswer()"
        @click="clickMaskAnswer()"
    />

</template>

<script>

export default {

    name: "BtnMaskAnswer",

    props: {
        maskAnswer: {type: Boolean, default: false},
        emits: ["update:maskAnswer", "click"],
    },

    methods: {

        getOutline_maskAnswer() {
            return this.maskAnswer !== true
        },

        getColor_maskAnswer() {
            if (this.maskAnswer !== true) {
                return "indigo-8"
            } else {
                return "indigo-2"
            }
        },

        getTextColor_maskAnswer() {
            return "indigo-10"
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

        clickMaskAnswer() {
            this.toggleMaskAnswer()
            this.$emit("click")
        },

    }


}

</script>

<style scoped>

</style>