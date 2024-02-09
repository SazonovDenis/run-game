<template>

    <MenuContainer title="Уровень"
                   :showFooter="showMode==='edit'"
                   :frameReturn="frameReturn"
                   :frameReturnProps="frameReturnProps"
    >

        <div v-if="dataLoaded">

            <div v-if="showMode==='edit'">


                <q-input
                    dense outlined
                    v-model="plan.planText"
                />

            </div>

            <div v-if="showMode==='view' && planId">

                <PlanInfo :planText="plan.planText"
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
            </div>


            <TaskList
                :showEdit="true"
                :tasks="tasks"
                :itemsMenu="itemsMenu"
                :filter="filter"/>


            <q-page-sticky v-if="showMode === 'view'"
                           position="bottom-right"
                           :offset="[70, 10]">
                <q-btn round
                       color="purple-4"
                       icon="edit"
                       size="1.2em"
                       @click="onEditPlan"
                />
            </q-page-sticky>

            <q-page-sticky v-if="showMode === 'view'"
                           position="bottom-right"
                           :offset="[10, 10]">
                <q-btn round
                       color="green-7"
                       icon="add"
                       size="1.2em"
                       @click="onAddFact"
                />
            </q-page-sticky>

        </div>


        <template v-slot:footer>

            <q-footer>

                <q-toolbar class="bg-grey-1 text-black" style="min-height: 5em">


                    <div class="row " style="width: 100%">

                        <div class="row" style="flex-grow: 10; align-content: end">
                            <div style="flex-grow: 10">
                                &nbsp;
                            </div>


                            <div
                                class="q-ma-sm"
                                style="margin: auto;"
                                @click="clickItemStateText">
                                    <span class="rgm-link-soft"> {{
                                            itemStateText
                                        }}</span>
                            </div>

                            <q-btn
                                no-caps
                                class="q-ma-sm"
                                label="Сохранить"
                                @click="savePlan()"
                            />


                        </div>
                    </div>
                </q-toolbar>

            </q-footer>

        </template>


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
import utils from "./utils"

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
                {
                    icon: this.itemDeleteMenuIcon,
                    itemMenuColor: this.itemDeleteMenuColor,
                    itemMenuClick: this.itemDeleteMenuClick,
                }
            ],

            //globalState: ctx.getGlobalState(),
            dataLoaded: false,
            showMode: "view",
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

            showHidden: false,
            filterText: null,
        }
    },

    watch: {
        sortField: function(value, old) {
            this.tasks.sort(this.compareFunction)
        }
    },

    methods: {

        itemDeleteMenuIcon(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            if (p !== -1) {
                return "visibility-off"
            } else {
                return "visibility"
            }
        },

        itemDeleteMenuColor(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            if (p !== -1) {
                return "red-6"
            } else {
                return "grey-5"
            }
        },

        itemDeleteMenuClick(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            if (p !== -1) {
                this.itemsDel.splice(p, 1)
            } else {
                this.itemsDel.push(taskItem)
            }
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


        async taskRemove(task) {
            console.info("planTask: ", task)
        },

        onEditPlan() {
            this.showMode = "edit"
            this.frameReturn = "/plan"
            this.frameReturnProps = {planId: this.planId}
        },

        savePlan() {
            this.showMode = "view"
            this.frameReturn = null
            this.frameReturnProps = null
        },

        onAddFact() {
            apx.showFrame({
                frame: '/addFact',
                props: {
                    planId: this.planId,
                    planText: this.plan.planText,
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

        if (!this.planId) {
            let planText = apx.date.toDisplayStr(apx.date.today())
            this.plan = {planText: "Уровень " + planText}
            this.tasks = []
            this.statistic = {}

            //this.showMode = "edit"
            this.dataLoaded = true

            // --- Сортировка по умолчанию
            this.sortField = "question"

            //
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

