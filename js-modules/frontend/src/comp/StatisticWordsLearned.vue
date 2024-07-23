<template>

    <CircularValue
        :value="statistic.wordCountLearnedDiff"
        :title="wordsText(statistic.wordCountLearnedDiff)"
        :valueTotal="statistic.wordCount"
        valueType="learned"
    />

</template>

<script>

import CircularValue from "./CircularValue"
import utils from "../utils"

export default {

    name: "StatisticWordsLearned",

    components: {
        CircularValue,
    },

    props: {
        statistic: Object,
    },

    methods: {
        wordsText(rating) {
            return utils.wordsText(rating) + "<br>выучено"
        }
    }

}
</script>

<style scoped>

</style>