<template>

    <div class="row">

        <template v-if="statistic.ratingTaskInc != 0">
            <div class="col result-rating result-rating-inc">
                <div class="result-value"><span
                    class="result-no-bold">&plus;</span>{{
                        statistic.ratingTaskInc
                    }}
                </div>
                <div class="result-title"
                     v-html="ratingText(statistic.ratingTaskInc) + '<br>' + ratingTextInc(statistic.ratingTaskInc)">
                </div>
            </div>
        </template>

        <template v-if="statistic.ratingTaskDec != 0">
            <div class="col result-rating result-rating-dec">
                <div class="result-value"><span
                    class="result-no-bold">&minus;</span>{{
                        -statistic.ratingTaskDec
                    }}
                </div>
                <div class="result-title"
                     v-html="ratingText(-statistic.ratingTaskDec) + '<br>' + ratingTextDec(statistic.ratingTaskDec)">
                </div>
            </div>
        </template>

    </div>

</template>

<script>
import utils from "run-game-frontend/src/utils"

export default {
    name: "StatisticRating",
    props: {
        statistic: Object,
    },
    methods: {
        ratingText(rating) {
            return utils.ratingText(rating)
        },
        ratingTextInc(rating) {
            return utils.ratingTextInc(rating)
        },
        ratingTextDec(rating) {
            return utils.ratingTextDec(rating)
        }
    }
}
</script>

<style scoped>

/* --- */

.result-value {
    font-weight: bold;
    font-size: 1.3em;
}

.result-title {
    font-weight: bold;
    font-size: 0.5em;
}

.result-rating .result-title {
    padding-left: 0.5em;
}

/* --- */

.result-rating {
    max-width: 4em;
    border-radius: 10em;
    padding: 0;
    margin: 0 0.3em;
}

.result-rating-inc {
    color: #003090;
}

.result-rating-dec {
    color: #a00020;
}

.result-no-bold {
    font-weight: normal;
}

</style>