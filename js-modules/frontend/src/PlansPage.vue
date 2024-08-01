<template>

    <MenuContainer
        tabMenuName="PlansPage"
        :title="getTitle()"
        :frameReturn="getFrameReturn()"
        :helpKey="getHelpKey()"
    >

        <!-- -->

        <HelpPanel :helpKey="getHelpKey()"/>

        <!-- -->


        <PlansFilterBar
            class="q-my-sm"
            v-model:filterText="filterText"
            v-model:sortField="settings.sortField"
            v-model:tags="settings.filterTags"
            v-model:favourite="settings.favourite"
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
                Чтобы кнопки списка не загораживали последнюю строку
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
                color="purple-4"
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
            plans: [],

            //viewPlanType: "common",

            filterText: "",

            settings: {
                sortField: "ratingAsc",
                filterTags: {},
                favourite: false,
            }
        }
    },

    watch: {

        settings: {
            handler() {
                // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
                if (this.settingsPreventWatch) {
                    this.settingsPreventWatch = false
                    return
                }

                //
                ctx.globalState.filterSettings.plans = this.settings
                ctx.eventBus.emit("change:settings")
            },
            deep: true
        },

        'settings.favourite': function() {
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            if (this.settingsPreventWatch) {
                return
            }

            //
            this.loadPlans()
        },

        "settings.sortField": function(value, old) {
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

        // Не допускает одновременного наличия двух языков: при ВКЛЮЧЕНИИ одного языка,
        // остальные сбрасываются; при ВЫКЛЮЧЕНИИ одного остальные не трогаются.
        toggleKazXorEng(filterTags) {
            if (filterTags.tagLastClicked === "kaz") {
                delete filterTags["eng"]
            } else if (filterTags.tagLastClicked === "eng") {
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


            } else if (this.settings.sortField === "ratingAsc") {
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

            } else if (this.settings.sortField === "ratingDesc") {
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
            // В filterTags всегда есть флаг tagLastClicked, поэтому для определения
            // пустоты количество сравниваем с 1, а не с 0
            let filterTagsCount = Object.keys(this.settings.filterTags).length
            if (!this.settings.filterTags || filterTagsCount <= 1) {
                return true
            }


            // Чтобы не было ошибок, если у плана нет тэгов
            let planTags = plan.tags
            if (!planTags) {
                planTags = []
            }


            // Значение тэга, выставленное в фильтре, должно совпадать со значением тэга у плана
            let filterTag_sound = this.settings.filterTags["word-sound"]
            if (filterTag_sound === true) {
                let planTag_question_datatype = planTags[dbConst.TagType_plan_question_datatype]
                let planTag_answer_datatype = planTags[dbConst.TagType_plan_answer_datatype]
                if (planTag_question_datatype !== "word-sound" && planTag_answer_datatype !== "word-sound") {
                    return false
                }
            }
            if (filterTag_sound === false) {
                let planTag_question_datatype = planTags[dbConst.TagType_plan_question_datatype]
                let planTag_answer_datatype = planTags[dbConst.TagType_plan_answer_datatype]
                if (planTag_question_datatype === "word-sound" || planTag_answer_datatype === "word-sound") {
                    return false
                }
            }


            //
            let planTag_translate_direction = planTags[dbConst.TagType_word_translate_direction]
            if (!planTag_translate_direction) {
                return false
            }
            //
            let filterTag_eng = this.settings.filterTags["eng"]
            if (filterTag_eng) {
                if (planTag_translate_direction.indexOf("eng") === -1) {
                    return false
                }
            }
            //
            let filterTag_kaz = this.settings.filterTags["kaz"]
            if (filterTag_kaz) {
                if (planTag_translate_direction.indexOf("kaz") === -1) {
                    return false
                }
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
            if (this.settings.favourite) {
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
        let settings = ctx.globalState.filterSettings.plans
        if (settings) {
            this.settings = settings
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            this.settingsPreventWatch = true
        }

        //
        await this.loadPlans()
    },

}

</script>
