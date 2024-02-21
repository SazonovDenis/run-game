<template>

    <MenuContainer :title="plan.planText"
                   :frameReturn="frameReturn"
                   :frameReturnProps="frameReturnProps"
    >

        <div v-if="dataLoaded">

            <PlanInfo :_planText="plan.planText"
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


            <TaskList
                :showEdit="true"
                :tasks="tasks"
                :itemsMenu="itemsMenu"
            />


            <q-page-sticky
                position="bottom-right"
                :offset="[70, 10]">
                <q-btn round
                       color="purple-4"
                       icon="edit"
                       size="1.2em"
                       @click="onPlanEdit"
                />
            </q-page-sticky>

            <q-page-sticky
                position="bottom-right"
                :offset="[10, 10]">
                <q-btn round
                       color="green-7"
                       icon="add"
                       size="1.2em"
                       @click="onPlanAddFact"
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

export default {
    name: "PlanPage",

    props: {
        planId: null,
    },

    components: {
        MenuContainer, PlanInfo, TaskList
    },

    computed: {
        itemStateText() {
            if (this.itemsDel.length > 0) {
                return "Выбрано на удаление: " + this.itemsDel.length
            } else {
                return ""
            }
        }
    },

    data() {
        return {
            plan: {},
            tasks: {},
            statistic: {},

            itemsDel: [],
            itemsMenu: [
                /*
                                {
                                    icon: this.itemHideMenuIcon,
                                    color: this.itemHideMenuColor,
                                    itemMenuClick: this.itemHideMenuClick,
                                },
                */
            ],

            dataLoaded: false,
            frameReturn: null,
            frameReturnProps: null,

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

        itemHideMenuIcon(taskItem) {
            if (taskItem.isHidden) {
                return "visibility-off"
            } else {
                return "visibility"
            }
        },

        itemHideMenuColor(taskItem) {
            if (taskItem.isHidden) {
                return "red-3"
            } else {
                return "grey-5"
            }
        },

        itemHideMenuClick(taskItem) {
            taskItem.isHidden = !taskItem.isHidden
            //
            if (taskItem.isHidden) {
                taskItem.isKnownGood = false
                taskItem.isKnownBad = false
            }
            //
            ctx.gameplay.api_saveUsrFact(taskItem.factQuestion, taskItem.factAnswer, taskItem)
        },


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

        /*
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

                    if (value.includes(filter)) {
                        return true
                    } else {
                        return false
                    }
                },
        */

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
                    planId: this.plan.id,
                    planText: this.plan.planText,
                    frameReturn: "/plan",
                    frameReturnProps: {planId: this.planId}
                }
            })
        },

        onPlanAddFact() {
            apx.showFrame({
                frame: '/planAddFact',
                props: {
                    planId: this.plan.id,
                    planText: this.plan.planText,
                    plan: this.plan,
                    tasks: this.tasks,
                    frameReturn: "/plan",
                    frameReturnProps: {planId: this.planId}
                }
            })
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
        this.sortField = "question"

        //
        this.tasks.push({})

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
    _font-size: 1.2em;
}

.game-info {
    font-size: 1.5em;
    text-align: center;
}

</style>

