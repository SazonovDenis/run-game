<template>

    <MenuContainer :title="plan.planText"
                   :frameReturn="frameReturn"
                   :frameReturnProps="frameReturnProps"
    >

        <div v-show="dataLoaded">

            <PlanStatistic :planText="null"
                           :statistic="statistic"
                           :chartData="chartData"
                           :chartDataKey="chartDataKey"
            />

            <div class="q-my-sm bottom-buttons">

                <DateRangeInput
                    ref="dateRangeInput"
                    v-model="params.period"
                    :hiddenValues="['day']"
                    @update:modelValue="on_params_period"
                />

            </div>


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
import DateRangeInput from "./comp/DateRangeInput"
import gameplay from "./gameplay"
import {apx} from "./vendor"
import ctx from "./gameplayCtx"

export default {

    name: "PlanStatisticPage",

    props: {
        planId: null,
        period: null,
        frameReturn: null,
        frameReturnProps: null,

    },

    components: {
        MenuContainer, PlanStatistic, DateRangeInput
    },

    data() {
        return {
            params: {
                period: "week",
            },

            plan: {},
            statistic: {},
            statisticPeriod: {},

            chartData: {},
            chartDataKey: 0,

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

            //
            let statisticPeriod = res.statisticPeriod
            let xAxisData = []
            let dataRating = []
            let dataRatingInc = []
            let dataRatingDec = []
            let n = 0
            let displayFormat1 = "d MMMM yyyy"
            let displayFormat2 = "d MMMM"
            for (let rec of statisticPeriod) {
                n++
                if (n === 1) {
                    xAxisData.push(apx.date.toDateTime(rec.dbeg).toFormat(displayFormat1))
                } else if (n === statisticPeriod.length) {
                    xAxisData.push(apx.date.toDateTime(rec.dbeg).toFormat(displayFormat2))
                } else {
                    xAxisData.push('')
                }
                dataRating.push(rec.ratingTask - rec.ratingTaskInc + rec.ratingTaskDec)
                dataRatingInc.push(rec.ratingTaskInc)
                dataRatingDec.push(-rec.ratingTaskDec)
            }
            let chartData = {
                xAxis: {
                    type: 'category',
                    boundaryGap: true,
                    data: xAxisData
                },
                yAxis: {
                    type: 'value',
                    show: false,
                    splitLine: {
                        show: false
                    }
                },
                series: [
                    {
                        data: dataRating,
                        type: 'bar',
                        stack: 'ratingTask',
                        color: '#1d83d480',
                    },
                    {
                        data: dataRatingInc,
                        type: 'bar',
                        stack: 'ratingTask',
                        color: '#1d83d4f0',
                        label: {
                            show: true,
                            position: 'top',
                            formatter: function(item) {
                                if (item.value > 0) {
                                    return "+" + item.value
                                } else {
                                    return ''
                                }
                            }
                        },
                    },
                    {
                        data: dataRatingDec,
                        type: 'bar',
                        stack: 'ratingTask',
                        //color: '#006FFF11',
                        color: 'rgba(255,0,0,0.26)',
                    },
                ]

            }
            this.chartData = chartData
            this.chartDataKey = this.chartDataKey + 1


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

