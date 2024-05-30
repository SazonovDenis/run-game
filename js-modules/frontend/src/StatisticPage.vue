<template>

    <MenuContainer
        tabMenuName="GameInfoPage"
        _title="Статистика"
    >

        <div style="font-size: 1.5em">
            <span>Повторено слов:</span>&nbsp;<span>{{ rating.wordCount }}</span>
        </div>

        <div style="font-size: 1.5em">
            <span>Рейтинг:&nbsp;</span><span>{{ rating.ratingTask }}</span>&nbsp;
            <span>+{{ rating.ratingTaskInc }}</span>&nbsp;<span>-{{
                rating.ratingTaskDec
            }}</span>
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
import StatisticPlanItem from "./comp/StatisticPlanItem"
import StatisticGameItem from "./comp/StatisticGameItem"
import TaskItem from "./comp/TaskItem"

export default {

    name: "StatisticPage",

    components: {
        MenuContainer,
        StatisticPlanItem, StatisticGameItem, TaskItem,
    },

    props: {
        period: String,
        group: String,
    },

    data() {
        return {
            items: [],

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
            await this.load()
        },

        async onParams_group() {
            // Обязательно очистить this.items, иначе до момента окончания this.load()
            // рендеринг будет для нового  режима группировки использовать старый список
            // (для предыдущего режима группировки), где поля совсем другие.
            this.items = []

            //
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
            //
            let today = apx.date.today()
            let dend = today
            let dbeg
            //
            if (this.params.period === "day") {
                dbeg = apx.date.add(dend, {days: -1})
                /*
                            } else if (this.params.period === "day-1") {
                                dbeg = apx.date.add(today, {days: -1})
                                dend = apx.date.add(today, {days: -2})
                */
            } else if (this.params.period === "week") {
                dbeg = apx.date.add(dend, {days: -7})
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

            this.items = resApi.items.records
            this.rating = resApi.rating.records[0]

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

.bottom-buttons {
    position: fixed;
    bottom: 0.2rem;
}

</style>