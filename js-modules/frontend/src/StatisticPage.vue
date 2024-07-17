<template>

    <MenuContainer
        tabMenuName="StatisticPage"
    >


        <template v-show="!loaded">
            <LogoGame :_loading="true"/>
        </template>


        <div v-show="loaded" class="text-h3 text-center q-mt-sm">
            За {{ periodText() }}
        </div>

        <div v-show="loaded && statisticPeriod.wordCountRepeated > 0">

            <div class="justify-center row q-mt-md">

                <!-- -->

                <div class="result-words">

                    <div class="row">

                        <template v-if="statisticPeriod.wordCountLearned != null">

                            <StatisticWordsLearned :statistic="statisticPeriod"/>

                        </template>

                        <template v-if="statisticPeriod.wordCountRepeated != null">

                            <StatisticWordsRepeated :statistic="statisticPeriod"/>

                        </template>

                    </div>

                </div>

                <!-- -->

                <div class="result-words">

                    <div class="row q-my-md">

                        <StatisticRatingDiff
                            class="q-mx-md q-mt-sm"
                            :statistic="statisticPeriod"/>

                    </div>

                </div>


                <!-- -->

                <PeriodStatisticChart
                    v-show="params.period !== 'day'"
                    :statistic="statisticByDay"
                />

                <!-- -->

            </div>


            <div>

                <q-tabs
                    v-model="params.group"
                    no-caps
                    inline-label
                    class="bg-grey-1 q-mt-sm"

                    @update:modelValue="onParams_group"
                >
                    <q-tab name="plan" label="Уровни"/>
                    <q-tab name="game" label="Игры"/>
                    <q-tab name="word" label="Слова"/>

                </q-tabs>

            </div>


            <div class="items-container">

                <q-list>

                    <template v-for="item in items">

                        <template v-if="params.group==='plan'">
                            <StatisticPlanItem
                                :item="item"
                                @click="onPlanClick(item)"
                            />
                        </template>

                        <template v-if="params.group==='game'">
                            <StatisticGameItem
                                :item="item"
                                @click="onGameClick(item)"
                            />
                        </template>

                        <template v-if="params.group==='word'">
                            <TaskItem
                                :taskItem="item"
                                :showAnswerResult="false"
                                :showRating="true"
                            />
                        </template>


                    </template>

                    <!-- Последний, для прокрутки -->
                    <q-item style="min-height: 4rem"
                    />

                </q-list>


            </div>

        </div>


        <div v-show="loaded && statisticPeriod.wordCountRepeated === 0">

            <div class="message-no-data q-mx-md q-mt-xl">
                Нет игр за {{ periodText() }}
            </div>
            <div v-if="params.period !== 'month3'"
                 class="message-no-data-hint q-ma-md">
                Выберите более длительный период
            </div>
        </div>


        <div class="q-my-sm bottom-buttons">

            <DateRangeInput
                ref="dateRangeInput"
                v-model="params.period"
                @update:modelValue="onParams_period"
            />

        </div>


    </MenuContainer>


</template>

<script>

import {daoApi} from "./dao"
import {apx} from './vendor'
import MenuContainer from "./comp/MenuContainer"
import LogoGame from "./comp/LogoGame"
import StatisticWordsLearned from "./comp/StatisticWordsLearned"
import DateRangeInput from "./comp/DateRangeInput"
import PeriodStatisticChart from "./comp/PeriodStatisticChart"
import StatisticWordsRepeated from "./comp/StatisticWordsRepeated"
import StatisticRatingDiff from "./comp/StatisticRatingDiff"
import StatisticPlanItem from "./comp/StatisticPlanItem"
import StatisticGameItem from "./comp/StatisticGameItem"
import TaskItem from "./comp/TaskItem"
import utils from "./utils"

export default {

    name: "StatisticPage",

    components: {
        MenuContainer, LogoGame,
        PeriodStatisticChart, StatisticWordsLearned, StatisticWordsRepeated,
        StatisticRatingDiff,
        DateRangeInput, StatisticPlanItem, StatisticGameItem, TaskItem,
    },

    props: {
        period: String,
        group: String,
    },

    data() {
        return {
            items: [],
            loaded: false,

            statisticPeriod: {},
            statisticByDay: [],

            params: {
                period: "day",
                group: "plan",
            },

            groupOptions: [
                {label: 'Планы', value: 'plan'},
                {label: 'Игры', value: 'game'},
                {label: 'Слова', value: 'word'},
            ],
        }
    },

    async mounted() {
        if (this.period) {
            this.params.period = this.period
        }
        if (this.group) {
            this.params.group = this.group
        }

        await this.load()
    },

    methods: {

        periodText() {
            return utils.getPeriodText(this.params.period)
        },

        async onParams_period() {
            this.loaded = false
            await this.load()
        },

        async onParams_group() {
            // Перед сменой group обязательно очистить this.items,
            // иначе до момента окончания this.load() рендеринг будет для нового режима
            // группировки использовать старый список (для предыдущего режима группировки),
            // где поля совсем другие, что будет приводить к ошибке рендеринга.
            this.items = []

            //
            this.loaded = false
            await this.load()
        },

        onGameClick(item) {
            apx.showFrame({
                frame: '/gameStatistic',
                props: {
                    gameId: item.game,
                    frameReturn: "/statistic",
                    frameReturnProps: {
                        period: this.params.period,
                        group: this.params.group,
                    }
                }
            })
        },

        onPlanClick(item) {
            apx.showFrame({
                frame: '/planStatistic',
                props: {
                    planId: item.plan,
                    period: this.params.period,

                    frameReturn: "/statistic",
                    frameReturnProps: {
                        period: this.params.period,
                        group: this.params.group,
                    }
                }
            })
        },

        async load() {
            let resApi

            let period = this.$refs.dateRangeInput.createParamsPeriod(this.params.period)

            if (this.params.group === "plan") {
                resApi = await daoApi.loadStore(
                    "m/Statistic/byPlan", [period.dbeg, period.dend]
                )

            } else if (this.params.group === "game") {
                resApi = await daoApi.loadStore(
                    "m/Statistic/byGame", [period.dbeg, period.dend]
                )

            } else if (this.params.group === "word") {
                resApi = await daoApi.loadStore(
                    "m/Statistic/byWord", [period.dbeg, period.dend]
                )

            }

            //
            this.items = resApi.items.records
            this.statisticPeriod = resApi.statisticPeriod
            this.statisticByDay = resApi.statisticByDay.records

            //
            this.loaded = true
        },

    }

}
</script>


<style lang="less" scoped>

.items-container {
    _height: calc(100vh - 14rem);
    _overflow-y: scroll;
    _overflow-x: auto;
}

.message-no-data {
    text-align: center;
    color: #404040;
    font-size: 2em;
}

.message-no-data-hint {
    text-align: center;
    color: gray;
    font-size: 1.5em;
}


/* --- */

.result-words {
    max-width: 20rem;
}

.result-words-header {
    font-size: 1em;
}

/* --- */

.result-statistic-header {
    font-size: 1em;
}

</style>