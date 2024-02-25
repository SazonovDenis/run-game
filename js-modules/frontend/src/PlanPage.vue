<template>

    <MenuContainer :title="plan.planText"
                   :frameReturn="frameReturn"
                   :frameReturnProps="frameReturnProps"
    >

        <div v-if="dataLoaded">

            <PlanInfo :planText="null"
                      :ratingTask="statistic.ratingTask"
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
                    style="width: 12em;"
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
                                @click="sortField='ratingAsc'">
                            Сложные
                        </q-item>

                        <q-item class="q-py-md" clickable v-close-popup
                                @click="sortField='ratingDesc'">
                            Легкие
                        </q-item>

                        <q-item class="q-py-md" clickable v-close-popup
                                @click="sortField='question'">
                            По алфавиту
                        </q-item>

                    </q-list>

                </q-btn-dropdown>


                <!--
                <q-toggle v-model="showHidden" label="Скрытые"/>
                -->

            </div>


            <TaskList
                :showEdit="true"
                :tasks="tasks"
                :itemsMenu="itemsMenu"
                :filter="filter"
            />


            <q-page-sticky
                v-if="plan.isOwner === true && tasks.length === 0"
                position="bottom-left"
                :offset="[10, 10]">
                <q-btn no-caps
                       color="red-7"
                       icon="del"
                       label="Удалить план"
                       size="1.2em"
                       @click="onPlanDelete"
                />
            </q-page-sticky>


            <q-page-sticky
                v-if="plan.isOwner === true"
                position="bottom-right"
                :offset="[70, 5]">
                <q-btn round
                       color="green-7"
                       icon="add"
                       size="1.2em"
                       @click="onPlanAddFact"
                />
            </q-page-sticky>

            <q-page-sticky
                _v-if="plan.isOwner === true"
                position="bottom-right"
                :offset="[10, 5]">
                <q-btn round
                       color="purple-4"
                       icon="edit"
                       size="1.2em"
                       @click="onPlanEdit"
                />
            </q-page-sticky>

        </div>


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
import {daoApi} from "run-game-frontend/src/dao"

export default {
    name: "PlanPage",

    props: {
        planId: null,
    },

    components: {
        MenuContainer, PlanInfo, TaskList
    },

    data() {
        return {
            plan: {},
            tasks: {},
            statistic: {},

            itemsMenu: [],

            dataLoaded: false,
            frameReturn: null,
            frameReturnProps: null,

            sortFieldMenu: false,
            sortField: "",
            sortFieldText: {
                question: "По алфавиту",
                answer: "Перевод",
                ratingDesc: "Легкие",
                ratingAsc: "Сложные",
            },
            sortFieldIcon: {
                question: "quasar.arrow.down",
                answer: "quasar.arrow.down",
                ratingDesc: "quasar.arrow.up",
                ratingAsc: "quasar.arrow.down",
            },

            /*
                        showHidden: false,
            */
            filterText: null,
        }
    },

    watch: {
        sortField: function(value, old) {
            this.tasks.sort(this.compareFunction)
        }
    },

    methods: {

        compareFunction(v1, v2) {
            if (!v1.factQuestion) {
                return 1
            } else if (!v2.factQuestion) {
                return -1
            } else if (this.sortField === "question") {
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
                if (v1.ratingTaskForSort > v2.ratingTaskForSort) {
                    return 1
                } else if (v1.ratingTaskForSort < v2.ratingTaskForSort) {
                    return -1
                } else {
                    return 0
                }
            } else if (this.sortField === "ratingDesc") {
                if (v1.ratingTaskForSort < v2.ratingTaskForSort) {
                    return 1
                } else if (v1.ratingTaskForSort > v2.ratingTaskForSort) {
                    return -1
                } else {
                    return 0
                }
            } else {
                // По умолчанию сортируем по коду факта-вопроса, а потом по рейтингу
                if (v1.factQuestion > v2.factQuestion) {
                    return 1
                } else if (v1.factQuestion < v2.factQuestion) {
                    return -1
                } else if (v1.factQuestion === v2.factQuestion && v1.ratingTask > v2.ratingTask) {
                    return 1
                } else if (v1.factQuestion === v2.factQuestion && v1.ratingTask < v2.ratingTask) {
                    return -1
                } else {
                    return 0
                }
            }
        },

        filter(planTask) {
            if (planTask.isHidden && !this.showHidden) {
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

            if (value.toLowerCase().includes(filter.toLowerCase())) {
                return true
            } else {
                return false
            }
        },

        isAuth() {
            return auth.isAuth()
        },

        async gameStart() {
            await gameplay.gameStart(this.planId)

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

        onPlanEdit() {
            apx.showFrame({
                frame: '/planEdit',
                props: {
                    plan: this.plan,
                    planItems: this.tasks,
                    defaultViewMode: "editPlan",
                    frameReturn: "/plan",
                    frameReturnProps: {planId: this.planId}
                }
            })
        },

        onPlanAddFact() {
            apx.showFrame({
                frame: '/planEdit',
                props: {
                    plan: this.plan,
                    planItems: this.tasks,
                    defaultViewMode: "addByText",
                    frameReturn: "/plan",
                    frameReturnProps: {planId: this.planId}
                }
            })
        },

        async onPlanDelete() {
            //
            await daoApi.invoke('m/Plan/del', [this.planId])

            //
            apx.showFrame({
                frame: '/levels',
            })
        },

    },


    async mounted() {
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login',
            })
            return
        }

        //
        this.dataLoaded = false

        //
        let res = await ctx.gameplay.api_getPlanTasks(this.planId)

        //
        this.plan = res.plan
        this.tasks = res.tasks
        this.statistic = res.statistic


        // --- Подготовим возможность сортировки по рейтингу, но с группировкой по фактам

        // Сортировка по группе, а потом по худшему рейтингу в группе
        // (группу образуют факт-вопрос и несколько фактов ответа)
        this.sortField = ""
        this.tasks.sort(this.compareFunction)

        // Сделам "сортировочный" рейтинг одинаковым внутри группы "факт-вопрос"
        // Это для того, чтобы при сортировке по рейтингу члены группы двигались вместе
        let getRatingTaskForSort = function(ratingTaskGroup, taskItem) {
            return "" +
                (ratingTaskGroup * 1000 + "_").padStart(6, "0") +
                taskItem.factQuestion + "_" +
                (taskItem.ratingTask * 1000 + "_").padStart(6, "0")
        };
        //
        let ratingTaskGroup
        for (let i = 0; i < this.tasks.length; i++) {
            let task = this.tasks[i]

            if (i === 0 || task.factQuestion !== this.tasks[i - 1].factQuestion) {
                ratingTaskGroup = task.ratingTask
            }

            task.ratingTaskForSort = getRatingTaskForSort(ratingTaskGroup, task)
        }

        // --- Сортировка по умолчанию
        this.sortField = "ratingAsc"

        // Для красивого отступа от последнего элемента todo сделать красивее
        if (this.tasks.length > 5) {
            this.tasks.push({})
        }

        //
        this.dataLoaded = true
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
}

.game-info {
    font-size: 1.5em;
    text-align: center;
}

</style>

