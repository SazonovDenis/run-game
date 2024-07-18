<template>

    <jc-chart
        class="chart"
        :options="chartData"
        :key="chartDataKey"
    />


</template>

<script>

import {apx} from "../vendor"

export default {

    name: "PeriodStatisticChart",

    props: {
        statistic: {
            type: Array,
            default: [],
        },
    },

    data() {
        return {
            chartData: {},
            chartDataKey: 0,
        }
    },

    watch: {
        statistic(statisticByDay) {
            let xAxisData = []
            let dataRating = []
            let dataRatingInc = []
            let dataRatingDec = []

            //
            let n = 0
            let cnt = statisticByDay.length
            let displayFormat1 = "d MMMM yyyy"
            let displayFormat2 = "d MMMM"
            for (let rec of statisticByDay) {
                n++
                if (n === 1) {
                    xAxisData.push(apx.date.toDateTime(rec.dbeg).toFormat(displayFormat1))
                } else if (n === cnt) {
                    xAxisData.push(apx.date.toDateTime(rec.dbeg).toFormat(displayFormat2))
                } else {
                    xAxisData.push('')
                }
                dataRating.push(rec.ratingTask - rec.ratingTaskInc + rec.ratingTaskDec)
                dataRatingInc.push(rec.ratingTaskInc)
                dataRatingDec.push(-rec.ratingTaskDec)
            }

            //
            let chartData = {
                grid: {
                    top: '7%',
                    bottom: '10%',
                    left: '15px',
                    right: '15px'
                },
                xAxis: {
                    type: 'category',
                    data: xAxisData,
                    axisTick: {
                        show: false
                    },
                    axisLabel: {
                        interval: 0,
                        align: 'center',
                        formatter: function(value, index) {
                            // Такие танцы с подбором отступа сделаны пототму, что
                            // rich: {alignLeft: {align: 'left'}}
                            // не срабатывает
                            let charsPafddingL
                            let charsPafddingR
                            if (cnt < 20) {
                                charsPafddingL = 5
                                charsPafddingR = 0
                            } else if (cnt < 50) {
                                charsPafddingL = 18
                                charsPafddingR = 10
                            } else {
                                charsPafddingL = 22
                                charsPafddingR = 12
                            }
                            if (index === 0) {
                                return " ".repeat(charsPafddingL) + value
                            } else if (index === cnt - 1) {
                                return value + " ".repeat(charsPafddingR)
                            } else {
                                return ''
                            }
                        },
                    },
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
        },
    },

    async mounted() {
        let option = {
            grid: {
                top: '5%',
                bottom: '10%',
                left: '15px',
                right: '15px'
            },
            xAxis: {
                type: 'category',
                boundaryGap: false,
                data: ['Mon', 'Tue', 'Wed', 'Thu', 'Fri', 'Sat', 'Sun']
            },
            yAxis: {
                type: 'value'
            },
            series: [
                {
                    data: [820, 932, 901, 934, 1290, 1330, 1320],
                    type: 'line',
                    areaStyle: {}
                }
            ]
        };
        this.chartData = option
    },

}
</script>

<style scoped>

.chart {
    width: 100%;
    height: 15rem;
    _border: solid 1px rgba(255, 0, 0, 0.2);
}

</style>