<template>

    <MenuContainer :title="plan.planText"
                   :frameReturn="frameReturn"
                   :frameReturnProps="frameReturnProps"
    >

        <div v-show="dataLoaded">

            <PlanStatistic
                :planText="null"
                :statistic="statistic"
            />

            <PeriodStatisticChart
                :statistic="statisticPeriod"
            />

            <div class="q-my-sm bottom-buttons">

                <DateRangeInput
                    ref="dateRangeInput"
                    v-model="params.period"
                    :hiddenValues="['day']"
                    @update:modelValue="on_params_period"
                />

            </div>

            <StatisticWordsLearned class="q-mx-md q-mt-md" :statistic="statistic"/>
            <StatisticWordsLearned class="q-mx-md q-mt-md" :statistic="statistic"/>


            <q-separator/>


            <div class="row">
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


        </div>

    </MenuContainer>

</template>


<script>

import MenuContainer from "./comp/MenuContainer"
import PlanStatistic from "./comp/PlanStatistic"
import PeriodStatisticChart from "./comp/PeriodStatisticChart"
import StatisticWordsLearned from "./comp/StatisticWordsLearned"
import DateRangeInput from "./comp/DateRangeInput"
import gameplay from "./gameplay"
import {apx} from "./vendor"
import ctx from "./gameplayCtx"

export default {

    name: "PlanStatisticPage",

    components: {
        MenuContainer, PlanStatistic, StatisticWordsLearned, PeriodStatisticChart, DateRangeInput
    },

    props: {
        planId: null,
        period: null,
        frameReturn: null,
        frameReturnProps: null,

    },

    data() {
        return {
            params: {
                period: "week",
            },

            plan: {},
            statistic: {},
            statisticPeriod: [],

            dataLoaded: false,
        }
    },


    async mounted() {
        if (this.period && this.period !== "day") {
            this.params.period = this.period
        }

        await this.load()
    },


    methods: {

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

            //
            this.dataLoaded = true
        },

    },


}

</script>


<style lang="less" scoped>

.bottom-buttons {
    width: 100vw;
    display: flex;
    flex-direction: row;
    flex-wrap: nowrap;
    align-content: center;
    justify-content: center;
}

</style>

