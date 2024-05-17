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
                        <TaskValue
                            :task="item"
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
                @update:model-value="load()"
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
import TaskValue from "./comp/TaskValue"

export default {

    name: "StatisticPage",

    components: {
        MenuContainer,
        StatisticPlanItem, StatisticGameItem, TaskValue,
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
        /*
                let userInfo = auth.getContextUserInfo()
                let planDefaultId = userInfo.planDefault
        */

        await this.load()

        this.items_ = [
            {
                text: "vahre eljrhj",
                valueSpelling: "dfdklfj kdf",
                valueTranslate: "Павло влао",
                valueSound: "1000-puzzle-english/mp3/freedictionary_UK/take.mp3",
                rating: {words: 12, rating: 2.7, ratingInc: 1.1, ratingDec: 4.4,},
            },
            {
                text: "jdkfj dkfj",
                valueSpelling: "dfdklfj kdf",
                valueTranslate: "ллав",
                valueSound: "1000-puzzle-english/mp3/freedictionary_UK/read.mp3",
                rating: {words: 15, rating: 4.8, ratingInc: 0, ratingDec: 12.9,},
            },
            {
                text: "jdkfj dkfj",
                valueSpelling: "me",
                valueTranslate: "Я",
                valueSound: "1000-puzzle-english/mp3/freedictionary_UK/me.mp3",
                rating: {words: 15, rating: 4.8, ratingInc: 0, ratingDec: 12.9,},
            },
        ]

    },

    methods: {

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