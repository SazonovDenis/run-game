<template>

    <MenuContainer title="Уровень">

        <PlanInfo :planText="plan.planText"
                  :rating="statistic.rating"
                  :ratingQuickness="statistic.ratingQuickness"
                  :ratingMax="statistic.ratingMax"
        />

        <q-separator/>


        <jc-btn kind="primary"
                label="Играть уровень"
                style="min-width: 10em;"
                @click="gameStart()">
        </jc-btn>


        <q-separator/>

        <div class="row q-ma-sm">

            <q-btn-dropdown
                @click="sortFieldMenu=true"
                v-model="sortFieldMenu"
                style="min-width: 10em;"
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


            <div class="q-ml-md" _style="flex-grow: 100">

                <q-input
                    style="max-width: 8em"
                    dense
                    v-model="filterText"
                    placeholder="Поиск">
                    <template v-slot:append>
                        <q-icon name="search"/>
                    </template>
                </q-input>

            </div>

        </div>


        <div>
            <q-list bordered separator>
                <template v-for="planTask in planTasks">

                    <q-item clickable v-ripple v-if="filter(planTask)">
                        <q-item-section>
                            <q-item-label overline>
                                <TaskValue :task="planTask.question" :doShowText="true"/>
                            </q-item-label>

                            <q-item-label>
                                <TaskValue :task="planTask.answer" :doShowText="true"/>
                            </q-item-label>
                        </q-item-section>

                        <q-item-section top side>
                            <div class="text-grey-8 q-gutter-xs">
                                <q-btn flat dense round
                                       icon="del"
                                       :color="hiddenColor(planTask.hidden)"
                                       @click="hiddenToggle(planTask)"/>
                                <q-btn flat dense round
                                       icon="star"
                                       :color="getStarredColor(planTask.starred)"
                                       @click="itemStarredToggle(planTask)"/>
                            </div>
                        </q-item-section>

                        <q-item-section top side>

                            <div style="min-width: 2em">

                                <q-badge
                                    :text-color="getRatingTextColor(planTask.rating)"
                                    :color="getRatingColor(planTask.rating)"
                                    :label="planTask.rating"/>

                                <!--
                                                            <div class="row">
                                                                <q-item-label caption>5 min ago</q-item-label>
                                                            </div>
                                -->
                            </div>


                        </q-item-section>


                    </q-item>

                </template>
            </q-list>
        </div>


    </MenuContainer>

</template>


<script>

import {apx} from "./vendor"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"
import GameTask from "./comp/GameTask"
import PlanInfo from "./comp/PlanInfo"
import TaskValue from "./comp/TaskValue"

export default {
    name: "PlanPage",

    props: {
        idPlan: null,
    },

    components: {
        MenuContainer, GameTask, PlanInfo, TaskValue
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

            columns: [
                {
                    name: 'valueSpelling',
                    required: true,
                    label: 'Задание',
                    align: 'left',
                    field: row => row.question.valueSpelling,
                    format: val => `${val}`,
                    sortable: true
                },
                {
                    name: 'valueTranslate', align: 'left', label: 'Перевод',
                    field: row => row.answer.valueTranslate,
                    sortable: true
                },
                {
                    name: 'rating', align: 'center', label: 'Баллов',
                    field: 'rating', sortable: true
                },
            ],
            rows: [],
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

        getRatingColor(rating) {
            if (rating > 0.8) {
                return "green-10"
            } else if (rating > 0.4) {
                return "green-3"
            } else if (rating > 0.1) {
                return "yellow-5"
            } else {
                return "blue-grey-1"
            }
        },

        getRatingTextColor(rating) {
            if (rating > 0.8) {
                return "white"
            } else if (rating > 0.4) {
                return "black"
            } else if (rating > 0.1) {
                return "black"
            } else {
                return "black"
            }
        },

        getStarredColor(starred) {
            if (starred) {
                return "yellow-8"
            } else {
                return "silver"
            }
        },

        hiddenColor(hidden) {
            if (hidden) {
                return "red-6"
            } else {
                return "silver"
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

        async taskRemove(task) {
            console.info("planTask: ", task)
        },

        hiddenToggle(task) {
            task.hidden = !task.hidden
            if (task.hidden && task.starred) {
                task.starred = false
            }
        },

        itemStarredToggle(task) {
            task.starred = !task.starred
            if (task.starred && task.hidden) {
                task.hidden = false
            }
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
    font-size: 1.5em;
}

.game-info {
    font-size: 1.5em;
    text-align: center;
}

</style>

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

    &.q-table--loading thead tr:last-child th {
        /* height of all previous header rows */
        top: 48px;
    }

    /* prevent scrolling behind sticky top row on focus */

    tbody {
        /* height of all previous header rows */
        scroll-margin-top: 48px;
    }
}

</style>