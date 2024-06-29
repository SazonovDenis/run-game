<template>

    <MenuContainer
        tabMenuName="GameInfoPage"
    >

        <template v-if="!loaded">
            <LogoGame :_loading="true"/>
        </template>


        <template v-else-if="rating.wordCountAll > 0">

            <div class="message-statistic">
                <!-- -->

                <div class="result-words-header">
                    Слова
                </div>

                <div class="row">
                    <template v-if="rating.wordCountDone != null">
                        <q-space/>

                        <q-circular-progress
                            show-value
                            rounded
                            size="8rem"
                            :value="100*rating.wordCountDone/rating.wordCountAll"
                            :thickness="0.2"
                            color="green-7"
                            track-color="grey-4"
                            class="q-mx-sm result-words-done"
                        >
                            <div class="col">
                                <div class="result-value">{{
                                        rating.wordCountDone
                                    }}
                                </div>
                                <div class="result-title">
                                    выучено
                                </div>
                                <div class="result-words2" v-if="false">
                                    <span class="result-words2-title">из </span>
                                    <span class="result-words2-value">{{
                                            rating.wordCountAll
                                        }}
                                    </span>
                                </div>
                            </div>
                        </q-circular-progress>

                    </template>

                    <template v-if="rating.wordCountAll != null">

                        <q-circular-progress
                            show-value
                            rounded
                            size="8rem"
                            :thickness="0"
                            _color="green-7"
                            track-color="white"
                            class="q-mx-sm result-words-all"
                        >
                            <div class="col">
                                <div class="result-value">{{
                                        rating.wordCountAll
                                    }}
                                </div>
                                <div class="result-title">
                                    повторено
                                </div>
                            </div>
                        </q-circular-progress>


                        <q-space/>
                    </template>
                </div>


                <!-- -->

                <div class="q-mt-sm result-rating-header">
                    Баллы
                </div>

                <div class="row">
                    <q-space/>

                    <template v-if="rating.ratingTaskInc != 0">
                        <div class="col result-rating result-rating-inc">
                            <div class="result-value"><span
                                class="result-no-bold">&plus;</span>{{
                                    rating.ratingTaskInc
                                }}
                            </div>
                            <div class="result-title">заработано</div>
                        </div>
                    </template>

                    <template v-if="rating.ratingTaskDec != 0">
                        <div class="col result-rating result-rating-dec">
                            <div class="result-value"><span
                                class="result-no-bold">&minus;</span>{{
                                    rating.ratingTaskDec
                                }}
                            </div>
                            <div class="result-title">потеряно</div>
                        </div>
                    </template>

                    <q-space/>
                </div>

                <!-- -->

            </div>

            <!--
                        <div class="message-statistic-hint">
                            <template v-if="rating.ratingTaskInc != 0 || rating.ratingTaskDec != 0">
                                <span>{{ "(" }}</span>

                                <template v-if="rating.ratingTaskInc != 0">
                                    <span>заработано:&nbsp;</span><span>{{
                                        rating.ratingTaskInc
                                    }}</span>
                                </template>

                                <template
                                    v-if="rating.ratingTaskInc != 0 && rating.ratingTaskDec != 0">
                                    <span>{{ ", " }}</span>
                                </template>

                                <template v-if="rating.ratingTaskDec != 0">
                                    <span>потеряно:&nbsp;</span><span>{{
                                        rating.ratingTaskDec
                                    }}</span>
                                </template>

                                <span>{{ ")" }}</span>
                            </template>
                        </div>
            -->


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
import StatisticPlanItem from "./comp/StatisticPlanItem"
import StatisticGameItem from "./comp/StatisticGameItem"
import TaskItem from "./comp/TaskItem"

export default {

    name: "StatisticPage",

    components: {
        MenuContainer, LogoGame,
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
                frame: '/gameInfo',
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
                frame: '/plan',
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
    height: calc(100vh - 14rem);
    overflow-y: scroll;
    overflow-x: auto;
}

.message-statistic {
    text-align: center;
    color: #404040;
    font-size: 2em;
}

.message-statistic-hint {
    text-align: center;
    color: gray;
    font-size: 1.5em;
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

.result-words-header {
    font-size: 1em;
}

.result-words {
    max-width: 5em;
    padding: 0.2em;
    margin: 0 0.2em;
    border-radius: 5em;
    border-width: 3px;
    border-style: solid;
}

.result-words-done {
    font-weight: bold;
    color: #43a047;
}

.result-words2 {
    position: relative;
    top: -0.1em;
    color: #535353;
}

.result-words2-title {
    font-size: 0.5em;
}

.result-words2-value {
    font-size: 0.8em;
}

.result-words-all {
    font-weight: bold;
    color: #00307e;
    background-color: #004ecf14;
    border-radius: 10em;
}

/* --- */

.result-rating-header {
    font-size: 1em;
}

.result-rating {
    max-width: 5em;
    border-radius: 5em;
    padding: 0;
    margin: 0 0;
}

.result-rating-inc {
    color: #43a047;
}

.result-rating-dec {
    color: #b30000;
}

.result-no-bold {
    font-weight: normal;
}

</style>