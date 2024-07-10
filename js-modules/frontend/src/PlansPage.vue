<template>

    <MenuContainer
        tabMenuName="PlansPage"
        :title="title">

        <q-tabs
            v-model="viewPlanType"
            no-caps
            inline-label
            class="bg-grey-1 _shadow-5 q-my-sm"
        >
            <q-tab name="pesonal" label="Мои уровни" _icon="alarm"/>
            <q-tab name="common" label="Библиотека" _icon="mail"/>
        </q-tabs>


        <PlansFilterBar
            v-model:filterText="filterText"
            v-model:sortField="sortField"
        />


        <q-scroll-area class="q-scroll-area" style="height: calc(100% - 11rem);">


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
                    <div style="height: 3em">&nbsp;</div>
                </q-item>

            </q-list>


        </q-scroll-area>


        <q-page-sticky v-if="showEdit && viewPlanType !== 'common'"
                       position="bottom-right"
                       :offset="[10, 10]">
            <q-fab
                :style="{fontSize: '1.3em', height: '4rem', width: isDesktop ? '16rem':'4rem'}"
                color="purple"
                icon="add"
                vertical-actions-align="right"
                direction="up"
                :label="isDesktop ? 'Добавить уровень' : ''"
            >
                <q-fab-action style="height: 4em; min-width: 18em;"
                              color="secondary"
                              label="Создать свой уровень"
                              square
                              icon="edit"
                              @click="onCreatePlan"
                />
                <q-fab-action style="height: 4em; min-width: 18em;"
                              color="amber-10"
                              text-color="black"
                              label="Подключить из библиотеки"
                              square
                              icon="add"
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

            viewPlanType: "pesonal",

            filterText: "",
            sortField: "ratingAsc",
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
            if (viewPlanType === "pesonal") {
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
