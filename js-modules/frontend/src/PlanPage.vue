<template>

    <MenuContainer
        :title="plan.planText"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :helpKey="getHelpKey()"
        :loaded="dataLoaded"
    >

        <!-- -->

        <HelpPanel class="q-mt-xs q-mb-none" :helpKey="getHelpKey()"/>

        <!-- -->


        <div class="justify-center row q-pt-sm">

            <StatisticWordsLearned :statistic="statistic"/>

            <StatisticRating :statistic="statistic"/>

        </div>

        <div class="row">

            <jc-btn class="q-ma-sm"
                    kind="secondary"
                    label="Статистика"
                    style="min-width: 8em;"
                    @click="onPlanStatistic()">
            </jc-btn>

            <jc-btn class="q-ma-sm"
                    kind="secondary"
                    label="Поделиться"
                    style="min-width: 8em;"
                    @click="onPlanUsrPlan()">
            </jc-btn>

            <q-space/>

            <jc-btn class="q-ma-sm"
                    kind="primary"
                    label="Играть уровень"
                    style="min-width: 12em;"
                    @click="gameStart()">
            </jc-btn>

        </div>


        <TaskListFilterBar
            class="q-my-sm"
            edHidden _edSort edFilter edMaskAnswer
            v-model:filterText="filterText"
            v-model:sortField="sortField"
            v-model:showHidden="showHidden"
            v-model:maskAnswer="maskAnswer"
            :hiddenCount="hiddenCount"
            :visibleCount="visibleCount"
        />

        <TaskList
            v-if="(visibleCount > 0) || (showHidden && hiddenCount > 0)"
            :showLastItemPadding="true"
            :showEdit="true"
            :showRating="true"
            :maskAnswer="maskAnswer"
            :tasks="tasks"
            :itemsMenu="itemsMenu"
            :filter="filter"
        />

        <div v-else-if="hiddenCount > 0"
             class="q-pa-md rgm-state-text">
            Все слова в уровне вы знаете, поэтому они скрыты
        </div>

        <div v-else
             class="q-pa-md rgm-state-text">
            В уровне нет ни одного слова
        </div>


        <q-page-sticky
            position="bottom-left"
            :offset="[10, 10]">

            <div class="row q-gutter-x-sm"
                 style="_border: solid 1px red; align-items: end;">
                <q-btn
                    v-if="canDeletePlan()"
                    rounded no-caps class="q-py-xs q-px-md"
                    color="red-7"
                    icon="del"
                    label="Удалить уровень"
                    size="1.2em"
                    style="width: 15rem"
                    @click="onPlanDelete"
                />
            </div>

        </q-page-sticky>


        <q-page-sticky
            position="bottom-right"
            :offset="[10, 10]">

            <BtnsEditAdd
                :showAdd="plan.isOwner === true"
                @onPlanEdit="onPlanEdit"
                @onPlanAddFact="onPlanAddFact"
            />

        </q-page-sticky>


    </MenuContainer>

</template>


<script>

import MenuContainer from "./comp/MenuContainer"
import TaskListFilterBar from "./comp/TaskListFilterBar"
import StatisticWordsLearned from "./comp/StatisticWordsLearned"
import StatisticRating from "./comp/StatisticRating"
import TaskList from "./comp/TaskList"
import HelpPanel from "./comp/HelpPanel"
import BtnsEditAdd from "./comp/BtnsEditAdd"
import gameplay from "./gameplay"
import {apx} from "./vendor"
import ctx from "./gameplayCtx"
import auth from "./auth"
import {daoApi} from "./dao"

export default {

    name: "PlanPage",

    props: {
        planId: null,

        frameReturn: null,
        frameReturnProps: null,

    },

    components: {
        MenuContainer, TaskListFilterBar, StatisticWordsLearned, StatisticRating,
        TaskList, BtnsEditAdd, HelpPanel,
    },

    data() {
        return {
            plan: {},
            tasks: {},
            statistic: {},

            chartData: {},

            itemsMenu: [],

            dataLoaded: false,
            visibleCount: 0,
            hiddenCount: 0,

            /**
             * Режим показа скрытых (известных) слов
             */
            showHidden: null,

            /**
             * Режим сокрытия ответов (для тренировки)
             */
            maskAnswer: null,

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

            filterText: null,
        }
    },

    watch: {
        sortField(value, old) {
            this.tasks.sort(this.compareFunction)
        },

        showHidden() {
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            if (this.settingsPreventWatch) {
                this.settingsPreventWatch = false
                return
            }

            //
            let globalViewSettings = ctx.getGlobalState().viewSettings
            globalViewSettings.findItems.showHidden = this.showHidden
            ctx.eventBus.emit("change:settings")
        },

        maskAnswer() {
            this.doTasksMaskAnswer(this.maskAnswer)
        },

    },

    methods: {

        getHelpKey() {
            return ["help.plan.game", "help.plan.edit"]
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

            if (value.toLowerCase().includes(filter.toLowerCase())) {
                return true
            } else {
                return false
            }
        },

        canDeletePlan() {
            let userInfo = auth.getUserInfo()
            let planDefaultId = userInfo.planDefault

            return this.plan.id !== planDefaultId && this.plan.isOwner === true && this.tasks.length === 0
        },

        async gameStart() {
            await gameplay.gameStart(this.planId)

            apx.showFrame({
                frame: '/game'
            })
        },

        async onPlanStatistic() {
            apx.showFrame({
                frame: '/planStatistic',
                props: {
                    planId: this.planId,
                    frameReturn: "/plan",
                    frameReturnProps: {
                        planId: this.planId,
                        frameReturn: this.frameReturn,
                        frameReturnProps: this.frameReturnProps,
                    }
                }
            })
        },

        async onPlanUsrPlan() {
            apx.showFrame({
                frame: '/planUsrPlan',
                props: {
                    plan: this.plan,
                    frameReturn: "/plan",
                    frameReturnProps: {
                        planId: this.planId,
                        frameReturn: this.frameReturn,
                        frameReturnProps: this.frameReturnProps,
                    }
                }
            })
        },

        onPlanEdit() {
            apx.showFrame({
                frame: '/planEdit',
                props: {
                    plan: this.plan,
                    planItems: this.tasks,
                    defaultMode: "editPlan",
                    frameReturn: "/plan",
                    frameReturnProps: {
                        planId: this.planId,
                        frameReturn: this.frameReturn,
                        frameReturnProps: this.frameReturnProps,
                    }
                }
            })
        },

        onPlanAddFact() {
            apx.showFrame({
                frame: '/planEdit',
                props: {
                    plan: this.plan,
                    planItems: this.tasks,
                    defaultMode: "addByText",
                    frameReturn: "/plan",
                    frameReturnProps: {
                        planId: this.planId,
                        frameReturn: this.frameReturn,
                        frameReturnProps: this.frameReturnProps,
                    }
                }
            })
        },

        async onPlanDelete() {
            //
            await daoApi.invoke('m/Plan/del', [this.planId])

            //
            apx.showFrame({
                frame: '/plans',
            })
        },

        doTasksMaskAnswer(maskAnswer) {
            for (let item of this.tasks) {
                item.maskAnswer = maskAnswer
            }
        },

        calcHiddenCountLoaded() {
            this.hiddenCount = 0
            for (let item of this.tasks) {
                if (item.isHidden) {
                    this.hiddenCount = this.hiddenCount + 1
                }
            }

            this.visibleCount = 0
            for (let item of this.tasks) {
                if (!item.isHidden) {
                    this.visibleCount = this.visibleCount + 1
                }
            }
        },


    },

    async mounted() {
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

        //
        this.calcHiddenCountLoaded()

        //
        this.dataLoaded = true


        // Настройки фильрации
        let globalViewSettings = ctx.getGlobalState().viewSettings
        if (globalViewSettings.findItems) {
            this.showHidden = globalViewSettings.findItems.showHidden
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            this.settingsPreventWatch = true
        }
    },

    unmounted() {
    },

}

</script>


<style lang="less" scoped>

</style>

