<template>

    <MenuContainer
        tabMenuName="GameInfoPage"
        _title="Статистика"
    >

        <div>
            <span>Повторено слов:</span>&nbsp;<span>{{ rating.words }}</span>
        </div>

        <div>
            <span>Рейтинг:&nbsp;</span><span>{{ rating.rating }}</span>&nbsp;
            <span>+{{ rating.ratingInc }}</span>&nbsp;<span>-{{ rating.ratingDec }}</span>
        </div>


        <!--
        <RgmButtonToggleGroup
            :options="periodOptions"
            :value="value_RgmButtonToggleGroup"
        />

        <q-separator class="q-ma-md"/>
        -->


        <div>

            <q-btn-toggle
                v-model="params.period"
                no-caps
                _rounded
                _unelevated

                class="rgm-toggle"
                toggle-color="primary"
                color="white"
                text-color="primary"

                :options="periodOptions"
            />

        </div>

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


        <q-btn-toggle
            v-model="params.group"
            no-caps
            _rounded
            _unelevated

            class="rgm-toggle"
            toggle-color="primary"
            color="white"
            text-color="primary"

            :options="groupOptions"
        />

    </MenuContainer>


</template>

<script>

import auth from "./auth"
import gameplay from "./gameplay"
import MenuContainer from "./comp/MenuContainer"
import RgmButtonToggleGroup from "./comp/RgmButtonToggleGroup"
import StatisticPlanItem from "./comp/StatisticPlanItem"
import StatisticGameItem from "./comp/StatisticGameItem"
import TaskValue from "./comp/TaskValue"

export default {

    name: "StatisticPage",

    components: {
        MenuContainer, RgmButtonToggleGroup,
        StatisticPlanItem, StatisticGameItem, TaskValue,
    },

    data() {
        return {
            value_RgmButtonToggleGroup: {},

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
        let userInfo = auth.getContextUserInfo()
        let planDefaultId = userInfo.planDefault

        this.itemsX = await gameplay.api_getPlansVisible()

        this.items = [
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

    }

}
</script>


<style lang="less" scoped>

.rgm-toggle {
    _border: 1px solid #4072a8;
}

</style>