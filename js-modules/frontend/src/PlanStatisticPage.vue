<template>

    <MenuContainer :title="plan.planText"
                   :frameReturn="frameReturn"
                   :frameReturnProps="frameReturnProps"
    >

        <div v-show="dataLoaded">

            <div class="text-h3 text-center q-mt-sm">
                За {{ periodText() }}
            </div>

            <div class="justify-center row q-mt-md">

                <!-- -->

                <div class="result-words">

                    <div class="row">

                        <StatisticWordsLearned :statistic="statisticPeriod"/>

                        <StatisticWordsRepeated :statistic="statisticPeriod"/>

                    </div>

                </div>

                <!-- -->

                <div class="result-words">

                    <div class="row q-mt-md">

                        <StatisticRatingDiff
                            class="q-mx-md q-my-sm q-mb-md"
                            :statistic="statisticPeriod"/>

                    </div>

                </div>

            </div>


            <PeriodStatisticChart
                v-show="params.period !== 'day'"
                :statistic="statisticByDay"
            />


            <div class="text-h3 text-center q-mt-md">
                Всего по уровню
            </div>

            <div v-show="dataLoaded" class="justify-center row q-mt-ms">

                <StatisticWordsLearned class="q-mx-md q-mt-md" :statistic="statistic"/>

                <StatisticRating class="q-mx-md q-mt-md" :statistic="statistic"/>

            </div>


            <div class="row q-my-lg">
                <jc-btn class="q-ma-sm"
                        kind="primary"
                        label="Играть уровень"
                        style="min-width: 10em;"
                        @click="gameStart()">
                </jc-btn>

                <jc-btn class="q-ma-sm"
                        kind="secondary"
                        label="Состав уровня"
                        style="min-width: 12em;"
                        @click="onPlanView()">
                </jc-btn>
            </div>


            <div class="q-my-sm bottom-buttons">

                <DateRangeInput
                    ref="dateRangeInput"
                    v-model="params.period"
                    :__hiddenValues="['day']"
                    @update:modelValue="on_params_period"
                />

            </div>


        </div>

    </MenuContainer>

</template>


<script>

import MenuContainer from "./comp/MenuContainer"
import PeriodStatisticChart from "./comp/PeriodStatisticChart"
import StatisticRating from "./comp/StatisticRating"
import StatisticRatingDiff from "./comp/StatisticRatingDiff"
import StatisticWordsLearned from "./comp/StatisticWordsLearned"
import StatisticWordsRepeated from "./comp/StatisticWordsRepeated"
import DateRangeInput from "./comp/DateRangeInput"
import gameplay from "./gameplay"
import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import utils from "./utils"

export default {

    name: "PlanStatisticPage",

    components: {
        MenuContainer, DateRangeInput,
        StatisticWordsLearned, StatisticWordsRepeated, PeriodStatisticChart,
        StatisticRating, StatisticRatingDiff,
    },

    props: {
        planId: null,
        period: {type: String, default: "week"},
        frameReturn: null,
        frameReturnProps: null,

    },

    data() {
        return {
            params: {
                period: null,
            },

            plan: {},
            statistic: {},
            statisticPeriod: {},
            statisticByDay: [],

            dataLoaded: false,
        }
    },


    async mounted() {
        this.params.period = this.period

        await this.load()
    },


    methods: {

        periodText() {
            return utils.getPeriodText(this.params.period)
        },

        async gameStart() {
            await gameplay.gameStart(this.planId)

            apx.showFrame({
                frame: '/game'
            })
        },

        onPlanView() {
            apx.showFrame({
                frame: '/plan',
                props: {
                    planId: this.planId,
                    frameReturn: "/statistic",
                }
            })
        },

        async on_params_period() {
            this.loaded = false
            await this.load()
        },

        async load() {
            let period = this.$refs.dateRangeInput.createParamsPeriod(this.params.period)

            //
            let res = await ctx.gameplay.api_getPlanStatistic(this.planId, period.dbeg, period.dend)

            //
            this.plan = res.plan
            this.statistic = res.statistic
            this.statisticPeriod = res.statisticPeriod
            this.statisticByDay = res.statisticByDay

            //
            this.dataLoaded = true
        },

    },


}

</script>


<style scoped>

</style>

