<template>

    <CircularValue
        :value="statistic.wordCountRepeatedDiff"
        :title="wordsText(statistic.wordCountRepeatedDiff)"
        valueType="repeated"
    />

</template>

<script>

import CircularValue from "./CircularValue"
import utils from "../utils"

export default {
    name: "StatisticWordsRepeated",

    components: {
        CircularValue,
    },

    props: {
        statistic: Object,
    },

    methods: {
        wordsText(rating) {
            return utils.wordsText(rating) + "<br>повторено"
        }
    }

}
</script>

<style scoped>

</style>