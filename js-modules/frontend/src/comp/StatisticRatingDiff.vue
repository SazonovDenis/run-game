<template>

    <div class="row">

        <div class="col result-rating result-rating-inc">
            <div :class="'result-value '+ classResultValue(statistic.ratingTaskInc)">
                <span
                    v-if="statistic.ratingTaskInc !== 0"
                    class="result-value-sign">&plus;</span>
                <span class="result-value-value">
                    {{ statistic.ratingTaskInc }}
                </span>
            </div>
            <div class="result-title"
                 v-html="ratingText(statistic.ratingTaskInc) + '<br>' + ratingTextInc(statistic.ratingTaskInc)">
            </div>
        </div>

        <div class="col result-rating result-rating-dec">
            <div :class="'result-value '+ classResultValue(statistic.ratingTaskDec)">
                <span
                    v-if="statistic.ratingTaskDec !== 0"
                    class="result-value-sign">&minus;</span>
                <span class="result-value-value">
                    {{ -statistic.ratingTaskDec }}
                </span>
            </div>
            <div class="result-title"
                 v-html="ratingText(-statistic.ratingTaskDec) + '<br>' + ratingTextDec(statistic.ratingTaskDec)">
            </div>
        </div>

    </div>

</template>

<script>
import utils from "run-game-frontend/src/utils"

export default {

    name: "StatisticRatingDiff",

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
        },
        classResultValue(value) {
            if (value !== 0) {
                return "padding-left"
            } else {
                ""
            }

        }
    }
}
</script>

<style scoped>

.result-rating {
    max-width: 8rem;
    min-width: 6rem;
    border-radius: 10rem;

    text-align: center;
    font-size: 1rem;
}

.result-value {
    font-weight: bold;
    font-size: 2em;
}

.result-title {
    font-weight: bold;
    font-size: 1em;
}

.padding-left {
    position: relative;
    left: -0.2em;
}

.result-rating-inc {
    color: #003090;
}

.result-rating-dec {
    color: #a00020;
}

.result-value-sign {
    font-weight: normal;
}

</style>