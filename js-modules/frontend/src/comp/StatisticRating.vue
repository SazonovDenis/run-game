<template>

    <CircularValue
        :value="statistic.ratingTask"
        :title="ratingText(statistic.ratingTask)"
        :valueTotal="statistic.ratingMax"
    />

</template>

<script>

import CircularValue from "./CircularValue"
import utils from "../utils"

export default {

    name: "StatisticRating",

    components: {
        CircularValue,
    },

    props: {
        statistic: Object,
    },

    methods: {
        ratingText(rating) {
            return utils.ratingText(rating)
        }
    }

}
</script>

<style scoped>

</style>