<template>

    <MenuContainer
        tabMenuName="GameInfoPage"
        _title="Статистика"
    >

        <div style="font-size: 1.5em">
            <span>Повторено слов:</span>&nbsp;<span>{{ rating.words }}</span>
        </div>

        <div style="font-size: 1.5em">
            <span>Рейтинг:&nbsp;</span><span>{{ rating.rating }}</span>&nbsp;
            <span>+{{ rating.ratingInc }}</span>&nbsp;<span>-{{ rating.ratingDec }}</span>
        </div>


        <div>

            <q-btn-toggle
                v-model="params.period"
                no-caps
                _rounded
                _unelevated

                class="rgm-toggle"
                toggle-color="blue-7"
                color="white"
                text-color="primary"

                :options="periodOptions"
                @update:model-value="load()"
            />

        </div>


        <div

            style="height: calc(100vh - 15rem); overflow: scroll;">

            <q-list>

                <template v-for="item in items">

                    <template v-if="params.group==='plan'">
                        <StatisticPlanItem
                            :item="item"
                        />
                    </template>

                    <template v-if="params.group==='game'">
                        <StatisticGameItem
                            :item="item"
                        />
                    </template>

                    <template v-if="params.group==='word'">

                        <TaskItem
                            :taskItem="item"
                        />

                    </template>


                </template>

            </q-list>


        </div>


        <div style="position: absolute">

            <q-btn-toggle
                v-model="params.group"
                no-caps
                _rounded
                _unelevated

                class="rgm-toggle"
                toggle-color="blue-7"
                color="white"
                text-color="primary"

                :options="groupOptions"
                @update:model-value="onParams_group"
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

    data() {
        return {
            items: {},

            rating: {
                words: 37,
                rating: 24.8,
                ratingInc: 2.1,
                ratingDec: 5.3,
            },

            params: {
                period: "day",
                group: "plan",
                sort: null,
            },

            periodOptions: [
                {label: 'Сегодня', value: 'day'},
                {label: 'Неделя', value: 'week'},
                {label: 'Месяц', value: 'month'},
            ],
            groupOptions: [
                {label: 'Планы', value: 'plan'},
                {label: 'Игры', value: 'game'},
                {label: 'Слова', value: 'word'},
            ],
        }
    },

    async mounted() {
        await this.load()
    },

    methods: {

        async onParams_group() {
            // Обязательно очистить, иначе рендеринг будет до момента окончания this.load()
            // рендерить старый список (для предыдкщего режима группировки),
            // где поля совсем другие.
            this.items = []

            //
            await this.load()
        },

        async load(x) {
            //
            let dend = apx.date.today()
            let dbeg
            //
            if (this.params.period === "day") {
                dbeg = apx.date.add(dend, {days: -1})
            } else if (this.params.period === "week") {
                dbeg = apx.date.add(dend, {days: -7})
            } else if (this.params.period === "month") {
                dbeg = apx.date.add(dend, {days: -30})
            }

            //
            if (this.params.group === "plan") {
                let resApi = await daoApi.loadStore(
                    "m/Statistic/byPlan", [dbeg, dend]
                )

                this.items = resApi.records

            } else if (this.params.group === "game") {
                let resApi = await daoApi.loadStore(
                    "m/Statistic/byGame", [dbeg, dend]
                )

                this.items = resApi.records

            } else if (this.params.group === "word") {
                let resApi = await daoApi.loadStore(
                    "m/Statistic/byWord", [dbeg, dend]
                )

                this.items = resApi.records
            }

        },

    }

}
</script>


<style lang="less" scoped>

.rgm-toggle {
    _border: 1px solid #4072a8;
}

</style>