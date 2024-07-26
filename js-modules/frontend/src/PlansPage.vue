<template>

    <MenuContainer
        tabMenuName="PlansPage"
        :title="getTitle()"
        :frameReturn="getFrameReturn()"
    >

        <PlansFilterBar
            class="q-my-sm"
            :showFullFilter="this.viewPlanType === 'common'"
            v-model:filterText="filterText"
            v-model:sortField="sortField"
            v-model:tags="filterTags"
            @update:tags="toggleKazEng()"
        />


        <q-scroll-area class="q-scroll-area" style="height: calc(100% - 8rem);">


            <q-list>

                <template v-for="plan in plans">

                    <PlanItem
                        v-if="isItemShown(plan)"
                        :plan="plan"
                        :viewPlanType="viewPlanType"
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


        <q-page-sticky v-if="showEdit && viewPlanType !== 'common'"
                       position="bottom-right"
                       :offset="[10, 10]">
            <q-fab
                color="purple"
                vertical-actions-align="right"
                direction="up"
                class="btn-add"
                label="Добавить уровень"
            >
                <q-fab-action class="btn-add-submenu"
                              color="secondary"
                              icon="gallery-add"
                              label="Создать свой уровень"
                              rounded
                              @click="onCreatePlan"
                />
                <q-fab-action class="btn-add-submenu"
                              color="amber-10"
                              text-color="black"
                              icon="gallery-favourite"
                              label="Подключить из библиотеки"
                              rounded
                              @click="onAddPlan"
                />
            </q-fab>
        </q-page-sticky>

    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import gameplay from "./gameplay"
import auth from "./auth"
import PlansFilterBar from "./comp/PlansFilterBar"
import MenuContainer from "./comp/MenuContainer"
import TasksStatistic from "./comp/TasksStatistic"
import {daoApi} from "./dao"
import PlanItem from "./comp/PlanItem"
import dbConst from "./dao/dbConst"

export default {

    components: {
        MenuContainer, PlanItem, PlansFilterBar, TasksStatistic
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

            viewPlanType: "personal",

            filterText: "",
            sortField: "ratingAsc",
            filterTags: {},
        }
    },

    watch: {

        viewPlanType: function(viewPlanType, oldVal) {
            this.loadPlans(viewPlanType)
        },

        sortField: function(value, old) {
            this.plans.sort(this.compareFunction)
        }

    },

    computed: {
        isDesktop() {
            return Jc.cfg.is.desktop
        },

    },

    methods: {

        toggleKazEng() {
            if (this.filterTags.clickedTag === "kaz") {
                delete this.filterTags["eng"]
            } else if (this.filterTags.clickedTag === "eng") {
                delete this.filterTags["kaz"]
            }
        },

        getTitle() {
            if (this.viewPlanType === "personal") {
                return null
            } else {
                return "Добавление уровней"
            }
        },

        getFrameReturn() {
            if (this.viewPlanType === "personal") {
                return null
            } else {
                return this.setViewPlanTypepersonal
            }
        },

        setViewPlanTypepersonal() {
            this.viewPlanType = "personal"
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

        onAddPlan() {
            this.viewPlanType = "common"
        },

        compareFunction(v1, v2) {
            if (this.sortField === "ratingAsc") {
                if (v1.ratingTask > v2.ratingTask) {
                    return 1
                } else if (v1.ratingTask < v2.ratingTask) {
                    return -1
                } else {
                    // Если рейтинг одинаковый - то сложнее тот, у кого больше слов
                    if (v1.count < v2.count) {
                        return 1
                    } else if (v1.count > v2.count) {
                        return -1
                    } else {
                        return 0
                    }
                }
            } else if (this.sortField === "ratingDesc") {
                if (v1.ratingTask < v2.ratingTask) {
                    return 1
                } else if (v1.ratingTask > v2.ratingTask) {
                    return -1
                } else {
                    // Если рейтинг одинаковый - то сложнее тот, у кого больше слов
                    if (v1.count < v2.count) {
                        return 1
                    } else if (v1.count > v2.count) {
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
            let filterTagsCount = Object.keys(this.filterTags).length
            if (!this.filterTags || filterTagsCount === 0) {
                return true
            }

            //
            let filterTag_sound = this.filterTags["word-sound"]
            if (filterTag_sound === true) {
                let planTag_question_datatype = plan.tags[dbConst.TagType_plan_question_datatype]
                let planTag_answer_datatype = plan.tags[dbConst.TagType_plan_answer_datatype]
                if (planTag_question_datatype !== "word-sound" && planTag_answer_datatype !== "word-sound") {
                    return false
                }
            }
            if (filterTag_sound === false) {
                let planTag_question_datatype = plan.tags[dbConst.TagType_plan_question_datatype]
                let planTag_answer_datatype = plan.tags[dbConst.TagType_plan_answer_datatype]
                if (planTag_question_datatype === "word-sound" || planTag_answer_datatype === "word-sound") {
                    return false
                }
            }

            //
            let planTag_translate_direction = plan.tags[dbConst.TagType_word_translate_direction]
            if (planTag_translate_direction) {
                //
                let filterTag_eng = this.filterTags["eng"]
                if (filterTag_eng) {
                    if (planTag_translate_direction.indexOf("eng") === -1) {
                        return false
                    }
                }
                //
                let filterTag_kaz = this.filterTags["kaz"]
                if (filterTag_kaz) {
                    if (planTag_translate_direction.indexOf("kaz") === -1) {
                        return false
                    }
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

        async loadPlans(viewPlanType) {
            if (viewPlanType === "personal") {
                this.plans = await gameplay.api_getPlansVisible()
            } else if (viewPlanType === "common") {
                this.plans = await gameplay.api_getPlansPublic()
            } else if (viewPlanType === "deleted") {
                this.plans = []
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
        await this.loadPlans(this.viewPlanType)
    },

}

</script>
