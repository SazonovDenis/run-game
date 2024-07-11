<template>

    <MenuContainer
        tabMenuName="StatisticPage"
    >

        <template v-if="!loaded">
            <LogoGame :_loading="true"/>
        </template>


        <template v-else-if="rating.wordCountRepeated > 0">

            <div class="message-statistic">

                <!-- -->

                <div class="result-words-header">
                    Слова
                </div>

                <div class="row">
                    <template v-if="rating.wordCountLearned != null">
                        <q-space/>

                        <StatisticWordsLearned :rating="rating"/>

                    </template>

                    <template v-if="rating.wordCountRepeated != null">

                        <StatisticWordsRepeated :rating="rating"/>

                        <q-space/>
                    </template>
                </div>


                <!-- -->

                <div class="q-mt-sm result-rating-header">
                    Баллы
                </div>

                <div class="row">
                    <q-space/>

                    <StatisticRating :rating="rating"/>

                    <q-space/>
                </div>

                <!-- -->

            </div>


            <div>

                <q-tabs
                    v-model="params.group"
                    no-caps
                    inline-label
                    class="bg-grey-1 q-mt-sm"

                    @update:model-value="onParams_group"
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

                </q-list>


            </div>

        </template>


        <template v-else>
            <div class="message-no-data q-mx-md q-mt-xl">
                Нет игр за {{ periodOptions_text[params.period] }}
            </div>
            <div v-if="params.period !== 'month3'"
                 class="message-no-data-hint q-ma-md">
                Выберите более длительный период
            </div>
        </template>


        <div class="q-ma-sm bottom-buttons">

            <q-btn-toggle
                v-model="params.period"
                no-caps
                rounded
                _unelevated

                class="rgm-toggle"
                toggle-color="blue-7"
                color="white"
                text-color="primary"

                :options="periodOptions"
                @update:model-value="onParams_period"
            />

        </div>


    </MenuContainer>


</template>

<script>

import {daoApi} from "run-game-frontend/src/dao"
import {apx} from './vendor'
import MenuContainer from "./comp/MenuContainer"
import LogoGame from "./comp/LogoGame"
import StatisticWordsLearned from "./comp/StatisticWordsLearned"
import StatisticWordsRepeated from "./comp/StatisticWordsRepeated"
import StatisticRating from "./comp/StatisticRating"
import StatisticPlanItem from "./comp/StatisticPlanItem"
import StatisticGameItem from "./comp/StatisticGameItem"
import TaskItem from "./comp/TaskItem"

export default {

    name: "StatisticPage",

    components: {
        MenuContainer, LogoGame,
        StatisticWordsLearned, StatisticWordsRepeated, StatisticRating,
        StatisticPlanItem, StatisticGameItem, TaskItem,
    },

    props: {
        period: String,
        group: String,
    },

    data() {
        return {
            items: [],
            loaded: false,

            rating: {},

            params: {
                period: "day",
                group: "plan",
                sort: null,
            },

            periodOptions: [
                {label: 'Сегодня', value: 'day'},
                {label: 'Неделя', value: 'week'},
                {label: 'Месяц', value: 'month'},
                {label: 'Три месяца', value: 'month3'},
            ],
            periodOptions_text: {
                day: "сегодня",
                week: "неделю",
                month: "месяц",
                month3: "три месяца",
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
                    frameReturn: "/statistic",
                    frameReturnProps: {
                        period: this.params.period,
                        group: this.params.group,
                    }
                }
            })
        },

        async load(x) {
            let today = apx.date.today()
            let dend = today
            let dbeg
            //
            if (this.params.period === "day") {
                dbeg = apx.date.add(dend, {days: 0})
            } else if (this.params.period === "week") {
                dbeg = apx.date.add(dend, {days: -6})
            } else if (this.params.period === "month") {
                dbeg = apx.date.add(dend, {days: -30})
            } else if (this.params.period === "month3") {
                dbeg = apx.date.add(dend, {days: -30 * 3})
            }

            //
            let resApi

            if (this.params.group === "plan") {
                resApi = await daoApi.loadStore(
                    "m/Statistic/byPlan", [dbeg, dend]
                )

            } else if (this.params.group === "game") {
                resApi = await daoApi.loadStore(
                    "m/Statistic/byGame", [dbeg, dend]
                )

            } else if (this.params.group === "word") {
                resApi = await daoApi.loadStore(
                    "m/Statistic/byWord", [dbeg, dend]
                )

            }

            //
            this.items = resApi.items.records
            this.rating = resApi.rating.records[0]

            //
            this.loaded = true
        },

    }

}
</script>


<style lang="less" scoped>

.rgm-toggle {
    _border: 1px solid #4072a8;
}

.items-container {
    _height: calc(100vh - 14rem);
    _overflow-y: scroll;
    _overflow-x: auto;
}

.message-statistic {
    text-align: center;
    color: #404040;
    font-size: 2em;
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

.bottom-buttons {
    position: fixed;
    bottom: 0.2rem;
}

/* --- */

.result-words-header {
    font-size: 1em;
}

/* --- */

.result-rating-header {
    font-size: 1em;
}

</style>