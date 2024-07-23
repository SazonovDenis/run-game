<template>

    <q-item class="item-first"
            clickable v-ripple
    >
        <q-item-section>
            <div>
                {{ item.planText }}
            </div>
            <div class="game-info">
                {{ dbeg }}
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
                    :label="item.wordCountLearnedDiff + ' выучено'"
                />

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

import {apx} from "../vendor"
import utils from "../utils"

export default {

    name: "StatisticGameItem",

    props: {
        item: Object
    },

    computed: {
        dbeg() {
            let displayFormat2 = "d MMMM, HH:MM"
            return apx.date.toDateTime(this.item.dbeg).toFormat(displayFormat2)
        }
    },

    methods: {
        ratingText(rating) {
            return utils.ratingText(rating)
        }
    }
}

</script>

<style scoped>

.game-info {
    font-size: 0.9em;
    color: gray;
}

</style>