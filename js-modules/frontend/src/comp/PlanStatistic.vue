<template>

    <div class="plan-statistic">

        <div class="plan-statistic__text">{{ planText }}</div>

        <div class="row">

            <StatisticWordsLearned class="q-ma-md" :statistic="statistic"/>

            <q-circular-progress
                show-value
                rounded
                size="10rem"
                font-size="1rem"
                :value="100*statistic.ratingTask/statistic.ratingMax"
                :thickness="0.2"
                color="blue-10"
                track-color="grey-3"
                class="q-ma-md plan-statistic__rating-info"
            >
                <div class="col">
                    <div>
                        <span class="plan-statistic__rating-value">{{
                                statistic.ratingTask
                            }}</span>
                    </div>
                    <div>
                        <span class="plan-statistic__rating-text">
                            {{ ratingText(statistic.ratingTask) }}
                        </span>
                    </div>
                    <div>
                        <span>из </span>
                        <span class="plan-statistic__rating-max">{{ statistic.ratingMax }}
                    </span>
                    </div>
                </div>
            </q-circular-progress>



        </div>

    </div>

</template>

<script>

import utils from "../utils"
import StatisticWordsLearned from "./StatisticWordsLearned"
import StatisticRating from "./StatisticRating"

export default {

    name: "PlanStatistic",

    components: {
        StatisticRating,
        StatisticWordsLearned,
    },

    props: {
        planText: null,
        statistic: null,
    },

    methods: {
        ratingText(statistic) {
            return utils.ratingText(statistic)
        }
    }

}

</script>

<style lang="less" scoped>

.plan-statistic {
    font-size: 1.5em;
    text-align: center;

    &__text {
        font-size: 1.5em;
        color: #34558b;
    }

    &__rating-info {
        color: #4c4c4c;
    }

    &__rating-value {
        font-size: 4em;
        color: #43a047;
        font-weight: bold;
    }

    &__rating-text {
        color: #43a047;
    }

    &__rating-max {
        font-size: 1.5em;
    }

    &__rating-quickness {
        font-size: 1.5em;
        color: #34558b;
    }

    &__rating-quickness-text {
        font-size: .6em;
        color: #34558b;
    }

    &__rating-inc {
        font-size: 1.5em;
        color: #43a047;
    }

    &__rating-dec {
        color: #850000;
    }

}

</style>