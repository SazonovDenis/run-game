<template>

    <div class="plan-statistic">

        <div class="plan-statistic__text">{{ planText }}</div>

        <div class="row">

            <StatisticWordsLearned class="q-ma-md" :rating="statistic"/>

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

<!--

            <div class="plan-statistic__rating-info col self-center">
                <div class="">

                    <div>
                        За скорость:
                    </div>
                    <div>
                    <span class="plan-statistic__rating-quickness">
                        {{ statistic.ratingQuickness }}
                    </span>
                    </div>
                    <div class="plan-statistic__rating-quickness-text">
                        {{ ratingText(statistic.ratingQuickness) }}
                    </div>

                </div>

            </div>
-->


            <jc-chart
                class="chart"
                :options="chartData"
                :key="chartDataKey"
            />


        </div>

    </div>

</template>

<script>

import utils from "../utils"
import StatisticWordsLearned from "./StatisticWordsLearned"

export default {

    name: "PlanStatistic",

    components: {
        StatisticWordsLearned,
    },

    props: {
        planText: null,
        statistic: null,
        chartData: null,
        chartDataKey: null,
    },

    methods: {
        ratingText(rating) {
            return utils.ratingText(rating)
        }
    }

}

</script>

<style lang="less" scoped>

.chart {
    width: 100%;
    height: 15rem;
    border: solid 1px rgba(255, 0, 0, 0.2);
}

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