<template>

    <q-item class="statistic-plan-item item-first"
            clickable v-ripple
    >

        <q-item-section top avatar>

            <q-avatar>

                <PlanRaitingCircularProgress :statistic="item"/>

            </q-avatar>

        </q-item-section>

        <q-item-section>
            <div class="plan-text">
                {{ item.planText }}
            </div>
            <div class="plan-info">
                {{ item.wordCount }} {{ wordsText(item.wordCount) }}
            </div>
        </q-item-section>

        <q-item-section
            top side
        >

            <div>

                <q-badge
                    v-if="item.wordCountLearnedDiff > 0"
                    class="rgm-bage statistic-type-learned"
                    color="white"
                    :label="item.wordCountLearnedDiff + ' выучено'"/>

            </div>

            <div>

                <q-badge
                    v-if="item.ratingTaskDiff > 0"
                    class="rgm-bage statistic-type-rating"
                    color="white"
                    :label="'+' + item.ratingTaskDiff + ' ' + ratingText(item.ratingTaskDiff)"
                />
                <q-badge
                    v-if="item.ratingTaskDiff < 0"
                    class="rgm-bage result-rating-dec"
                    color="white"
                    :label="item.ratingTaskDiff + ' ' + ratingText(item.ratingTaskDiff)"
                />

            </div>

        </q-item-section>

    </q-item>

</template>

<script>

import PlanRaitingCircularProgress from "./PlanRaitingCircularProgress"
import utils from "../utils"

export default {

    name: "StatisticPlanItem",

    components: {
        PlanRaitingCircularProgress
    },

    props: {
        item: Object
    },

    methods: {
        ratingText(rating) {
            return utils.ratingText(rating)
        },
        wordsText(wordCount) {
            return utils.wordsText(wordCount)
        },
    }

}

</script>

<style scoped>

.statistic-plan-item {
    min-height: 3rem;
}

</style>