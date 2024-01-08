<template>

    <MenuContainer title="Уровень">

        <PlanInfo :planText="plan.planText"
                  :rating="statistic.rating"
                  :ratingQuickness="statistic.ratingQuickness"
                  :ratingMax="statistic.ratingMax"
        />

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
                    label="Выбрать другой уровень"
                    style="min-width: 12em;"
                    @click="onSelectLevel()">
            </jc-btn>
        </div>

        <q-separator/>

        <div class="row q-mb-sm">

            <div class="q-mr-sm">

                <q-input
                    style="max-width: 9em"
                    dense outlined clearable
                    v-model="filterText"
                    placeholder="Поиск"
                >

                    <template v-slot:append v-if="!filterText">
                        <q-icon name="search"/>
                    </template>

                </q-input>

            </div>

            <q-btn-dropdown
                @click="sortFieldMenu=true"
                v-model="sortFieldMenu"
                style="width: 10em;"
                color="grey-2"
                text-color="black"
                no-caps
                split
                align="left"
                :icon="sortFieldIcon[sortField]"
                :label="sortFieldText[sortField]"
            >
                <q-list class="q-pa-sm">

                    <q-item class="q-py-md" clickable v-close-popup
                            @click="sortField='question'">
                        Слово
                    </q-item>

                    <q-item class="q-py-md" clickable v-close-popup
                            @click="sortField='answer'">
                        Перевод
                    </q-item>

                    <q-item class="q-py-md" clickable v-close-popup
                            @click="sortField='ratingDesc'">
                        Лучшие
                    </q-item>

                    <q-item class="q-py-md" clickable v-close-popup
                            @click="sortField='ratingAsc'">
                        Худшие
                    </q-item>

                </q-list>
            </q-btn-dropdown>


            <q-toggle v-model="showHidden" label="Скрытые"/>


        </div>


        <TaskList
            :showEdit="true"
            :planTasks="planTasks"
            :filter="filter"/>


    </MenuContainer>

</template>


<script>

import {apx} from "./vendor"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"
import PlanInfo from "./comp/PlanInfo"
import TaskList from "./comp/TaskList"

export default {
    name: "PlanPage",

    props: {
        idPlan: null,
    },

    components: {
        MenuContainer, PlanInfo, TaskList
    },

    computed: {},

    data() {
        return {
            plan: {},
            planTasks: {},
            statistic: {},
            globalState: ctx.getGlobalState(),

            sortFieldMenu: false,
            sortField: "",
            sortFieldText: {
                question: "Слово",
                answer: "Перевод",
                ratingDesc: "Лучшие",
                ratingAsc: "Худшие",
            },
            sortFieldIcon: {
                question: "quasar.arrow.down",
                answer: "quasar.arrow.down",
                ratingDesc: "quasar.arrow.up",
                ratingAsc: "quasar.arrow.down",
            },

            showHidden: false,
            filterText: null,
        }
    },

    watch: {
        sortField: function(value, old) {
            this.planTasks.sort(this.compare)
        }
    },

    methods: {

        compare(v1, v2) {
            if (this.sortField === "question") {
                if (v1.question.valueSpelling > v2.question.valueSpelling) {
                    return 1
                } else if (v1.question.valueSpelling < v2.question.valueSpelling) {
                    return -1
                } else {
                    return 0
                }
            } else if (this.sortField === "answer") {
                if (v1.answer.valueTranslate > v2.answer.valueTranslate) {
                    return 1
                } else if (v1.answer.valueTranslate < v2.answer.valueTranslate) {
                    return -1
                } else {
                    return 0
                }
            } else if (this.sortField === "ratingAsc") {
                if (v1.rating > v2.rating) {
                    return 1
                } else if (v1.rating < v2.rating) {
                    return -1
                } else {
                    return 0
                }
            } else if (this.sortField === "ratingDesc") {
                if (v1.rating < v2.rating) {
                    return 1
                } else if (v1.rating > v2.rating) {
                    return -1
                } else {
                    return 0
                }
            } else {
                return v1 > v2
            }
        },

        filter(planTask) {
            if (planTask.hidden && !this.showHidden) {
                return false
            }

            if (!this.filterText) {
                return true
            }

            if (this.contains(this.filterText, planTask.question.valueTranslate)) {
                return true
            }

            if (this.contains(this.filterText, planTask.question.valueSpelling)) {
                return true
            }

            if (this.contains(this.filterText, planTask.answer.valueTranslate)) {
                return true
            }

            if (this.contains(this.filterText, planTask.answer.valueSpelling)) {
                return true
            }

            return false
        },

        contains(filter, value) {
            if (!filter) {
                return true
            }

            if (!value) {
                return false
            }

            if (value.includes(filter)) {
                return true
            } else {
                return false
            }
        },

        isAuth() {
            return auth.isAuth()
        },

        async gameStart() {
            await gameplay.gameStart(this.idPlan)

            apx.showFrame({
                frame: '/game', props: {}
            })
        },

        async onSelectLevel() {
            await gameplay.closeActiveGame()

            apx.showFrame({
                frame: '/levels',
            })
        },


        async taskRemove(task) {
            console.info("planTask: ", task)
        },


    },

    created() {
    },

    async mounted() {
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login',
            })
            return
        }

        //
        let res = await ctx.gameplay.api_getPlanTasks(this.idPlan)

        //
        this.plan = res.plan
        this.planTasks = res.planTasks
        this.statistic = res.statistic

        this.sortField = "question"
        //this.rows = res.planTasks
    },

    unmounted() {
    },

}

</script>


<style lang="less" scoped>

hr {
    margin: 1em 0;
}

.plan-tasks {
    _font-size: 1.2em;
}

.game-info {
    font-size: 1.5em;
    text-align: center;
}

</style>

<!--
<style lang="less">

.my-sticky-header-table {

    /* height or max-height is important */
    height: 20rem;

    .q-table__top,
    .q-table__bottom,
    thead tr:first-child th {
        /* bg color is important for th; just specify one */
        background-color: #ebebeb;
        _background-color: white;
    }

    thead tr th {
        position: sticky;
        z-index: 1;
    }

    thead tr:first-child th {
        top: 0;
    }

    /* this is when the loading indicator appears */

    &.q-table&#45;&#45;loading thead tr:last-child th {
        /* height of all previous header rows */
        top: 48px;
    }

    /* prevent scrolling behind sticky top row on focus */

    tbody {
        /* height of all previous header rows */
        scroll-margin-top: 48px;
    }
}

</style>-->
