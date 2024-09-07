<template>

    <MenuContainer
        tabMenuName="PlansPage"
        :title="getTitle()"
        :frameReturn="getFrameReturn()"
        :helpKey="getHelpKey()"
        :loaded="dataLoaded"
    >

        <!-- -->

        <HelpPanel class="q-mt-xs q-mb-none" :helpKey="getHelpKey()"/>

        <!-- -->


        <PlansFilterBar
            class="q-my-sm"
            v-model:filterText="filterText"
            v-model:sortField="viewSettings.sortField"
            v-model:tags="viewSettings.filterTags"
            v-model:favourite="viewSettings.favourite"
            :onTagsChange="toggleKazXorEng"
        />


        <q-scroll-area class="q-scroll-area" style="height: calc(100% - 8rem);">


            <q-list>

                <template v-for="plan in plans">

                    <PlanItem
                        v-if="isItemShown(plan)"
                        :plan="plan"
                        @planClick="planClick"
                        @delUsrPlan="delUsrPlan"
                        @addUsrPlan="addUsrPlan"
                    />

                </template>

                <!--
                Элемент для последнего "пустого" элемента.
                Чтобы кнопки редактирования списка не загораживали последнюю строку
                -->
                <q-item>
                    <div style="height: 2em">&nbsp;</div>
                </q-item>

            </q-list>


        </q-scroll-area>


        <q-page-sticky v-if="showEdit"
                       position="bottom-right"
                       :offset="[10, 10]">

            <q-btn
                round no-caps class="q-py-none q-px-md"
                color="positive"
                icon="gallery-add"
                _label="Создать свой уровень"
                size="1.2em"
                @click="onCreatePlan"
            />


        </q-page-sticky>

    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import {daoApi} from "./dao"
import auth from "./auth"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import dbConst from "./dao/dbConst"
import PlansFilterBar from "./comp/PlansFilterBar"
import MenuContainer from "./comp/MenuContainer"
import TasksStatistic from "./comp/TasksStatistic"
import PlanItem from "./comp/PlanItem"
import HelpPanel from "./comp/HelpPanel"

export default {

    components: {
        MenuContainer, PlanItem, PlansFilterBar, TasksStatistic, HelpPanel,
    },

    props: {
        title: {
            type: String,
            default: null
        },
        onPlanClick: {
            type: Function,
            default: null
        },
        showEdit: {
            type: Boolean,
            default: true
        }
    },

    data() {
        return {
            dataLoaded: false,

            plans: [],

            filterText: "",

            viewSettings: {
                sortField: "ratingAsc",
                filterTags: {},
                favourite: false,
            }
        }
    },

    watch: {

        viewSettings: {
            handler() {
                // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
                if (this.settingsPreventWatch) {
                    this.settingsPreventWatch = false
                    return
                }

                //
                let globalViewSettings = ctx.getGlobalState().viewSettings
                globalViewSettings.plans = this.viewSettings
                ctx.eventBus.emit("change:settings")
            },
            deep: true
        },

        'viewSettings.favourite': function() {
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            if (this.settingsPreventWatch) {
                return
            }

            //
            this.loadPlans()
        },

        "viewSettings.sortField": function(value, old) {
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            if (this.settingsPreventWatch) {
                return
            }

            //
            this.plans.sort(this.compareFunction)
        }

    },

    computed: {},

    methods: {

        getHelpKey() {
            return ["help.plans", "help.plans.default"]
        },

        // Не допускает одновременного наличия двух языков:
        // при ВКЛЮЧЕНИИ одного языка, остальные сбрасываются;
        // при ВЫКЛЮЧЕНИИ одного остальные не трогаются.
        toggleKazXorEng(filterTags, tagLastClicked) {
            if (tagLastClicked === "kaz") {
                delete filterTags["eng"]
            } else if (tagLastClicked === "eng") {
                delete filterTags["kaz"]
            }
        },

        getTitle() {
            return null
        },

        getFrameReturn() {
            return null
        },

        isItemShown(item) {
            if (!this.filter) {
                return true
            } else {
                return this.filter(item)
            }
        },

        planClick(planId) {
            if (this.onPlanClick) {
                this.onPlanClick(planId)
            } else {
                this.planClickDefault(planId)
            }
        },

        planClickDefault(planId) {
            apx.showFrame({
                frame: '/plan',
                props: {
                    planId: planId,
                    frameReturn: "/plans",
                    frameReturnProps: {},
                },

            })
        },

        onCreatePlan() {
            apx.showFrame({
                frame: '/planEdit',
                props: {
                    immediateSaveMode: false,
                    frameReturn: "/plans",
                    frameReturnProps: {},
                }
            })
        },

        compareFunction(v1, v2) {
            if (v1.isDefault === true) {
                return -1
            } else if (v2.isDefault === true) {
                return 1

            } else if (v1.isOwner !== true && v2.isOwner === true) {
                return 1
            } else if (v1.isOwner === true && v2.isOwner !== true) {
                return -1


            } else if (this.viewSettings.sortField === "ratingAsc") {
                if (v1.ratingTask > v2.ratingTask) {
                    return 1
                } else if (v1.ratingTask < v2.ratingTask) {
                    return -1
                } else {
                    // Если рейтинг одинаковый - то сложнее тот, у кого больше слов
                    if (v1.wordCount < v2.wordCount) {
                        return 1
                    } else if (v1.wordCount > v2.wordCount) {
                        return -1
                    } else {
                        return 0
                    }
                }

            } else if (this.viewSettings.sortField === "ratingDesc") {
                if (v1.ratingTask < v2.ratingTask) {
                    return 1
                } else if (v1.ratingTask > v2.ratingTask) {
                    return -1
                } else {
                    // Если рейтинг одинаковый - то сложнее тот, у кого больше слов
                    if (v1.wordCount < v2.wordCount) {
                        return 1
                    } else if (v1.wordCount > v2.wordCount) {
                        return -1
                    } else {
                        return 0
                    }
                }

            } else {
                // По умолчанию сортируем по lastDt, а потом по рейтингу
                if (v1.lastDt > v2.lastDt) {
                    return 1
                } else if (v1.lastDt < v2.lastDt) {
                    return -1
                } else if (v1.lastDt === v2.lastDt && v1.ratingTask > v2.ratingTask) {
                    return 1
                } else if (v1.lastDt === v2.lastDt && v1.ratingTask < v2.ratingTask) {
                    return -1
                } else {
                    return 0
                }
            }
        },

        filter(plan) {
            return this.machText(plan) && this.machTags(plan)
        },

        machTags(plan) {
            // Фильтр по тэгам не задан
            let filterTagsCount = Object.keys(this.viewSettings.filterTags).length
            if (!this.viewSettings.filterTags || filterTagsCount === 0) {
                return true
            }


            // Чтобы не было ошибок, если у плана нет тэгов
            let planTags = plan.tags
            if (!planTags) {
                planTags = []
            }


            // Если в фильтре значение тэга === true или строка, то значение тэга тэга
            // у плана должно совпадать со значением, выставленым в фильтре.
            //
            // Если в фильтре значение тэга === false, то значение тэга тэга у плана
            // должно быть false или отсутствовать.

            //
            let planTag_question_factType = planTags[dbConst.TagType_plan_question_factType]
            let planTag_answer_factType = planTags[dbConst.TagType_plan_answer_factType]
            let filterTag_sound = this.viewSettings.filterTags["word-sound"]
            //
            if (filterTag_sound === true) {
                if (planTag_question_factType !== "word-sound" && planTag_answer_factType !== "word-sound") {
                    return false
                }
            }
            //
            if (filterTag_sound === false) {
                if (planTag_question_factType && (planTag_question_factType === "word-sound" || planTag_answer_factType === "word-sound")) {
                    return false
                }
            }


            //
            let planTag_translate_direction = planTags[dbConst.TagType_translate_direction]
            //
            let filterTag_eng = this.viewSettings.filterTags["eng"]
            if (filterTag_eng && (!planTag_translate_direction || planTag_translate_direction.indexOf("eng") === -1)) {
                return false
            }
            //
            let filterTag_kaz = this.viewSettings.filterTags["kaz"]
            if (filterTag_kaz && (!planTag_translate_direction || planTag_translate_direction.indexOf("kaz") === -1)) {
                return false
            }


            //
            return true
        },

        machText(plan) {
            if (!this.filterText) {
                return true
            }

            if (this.contains(this.filterText, plan.planText)) {
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

        async delUsrPlan(plan) {
            await daoApi.invoke('m/Plan/delUsrPlan', [plan.id])
            plan.isAllowed = false
        },

        async addUsrPlan(plan) {
            await daoApi.invoke('m/Plan/addUsrPlan', [plan.id])
            plan.isAllowed = true
        },

        async loadPlans() {
            if (this.viewSettings.favourite) {
                this.plans = await gameplay.api_getPlansAttached()
            } else {
                this.plans = await gameplay.api_getPlansAvailable()
            }

            // Заполним plan.isDefault
            for (let plan of this.plans) {
                let userInfo = auth.getUserInfo()
                let planDefaultId = userInfo.planDefault
                if (plan.id === planDefaultId) {
                    plan.isDefault = true
                }
            }

            //
            this.plans.sort(this.compareFunction)
        },

    },

    async mounted() {
        // Настройки фильрации
        let globalViewSettings = ctx.getGlobalState().viewSettings
        if (globalViewSettings.plans) {
            this.viewSettings = globalViewSettings.plans
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            this.settingsPreventWatch = true
        }

        // Список
        await this.loadPlans()

        //
        this.dataLoaded = true
    },

}

</script>
