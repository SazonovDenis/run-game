<template>

    <MenuContainer
        tabMenuName="LevelsPage"
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


        <LevelsFilterBar
            v-model:filterText="filterText"
            v-model:sortField="sortField"
        />


        <q-scroll-area class="q-scroll-area" style="height: calc(100% - 11rem);">


            <q-list>

                <template v-for="plan in plans">

                    <q-item v-if="isItemShown(plan)"
                            class="item-first"
                            clickable v-ripple
                    >

                        <q-item-section top avatar>
                            <q-avatar
                                v-if="plan.isDefault"

                                icon="star"
                                color="grey-2"
                                text-color="yellow-8">

                                <div class="plan-count-text">
                                    {{ plan.count }}
                                </div>

                            </q-avatar>

                            <q-avatar
                                v-else-if="plan.isPublic"

                                icon="folder-open"
                                color="grey-2"
                                text-color="grey-8">

                                <div class="plan-count-text">
                                    {{ plan.count }}
                                </div>

                            </q-avatar>

                            <q-avatar
                                v-else

                                icon="user"
                                color="grey-2"
                                text-color="grey-6">

                                <div class="plan-count-text">
                                    {{ plan.count }}
                                </div>

                            </q-avatar>

                        </q-item-section>

                        <q-item-section @click="onPlanClick(plan.plan)">
                            {{ plan.planText }}
                        </q-item-section>


                        <q-item-section top side>

                            <div class="text-grey-8 q-gutter-xs">

                                <q-btn
                                    v-if="plan.isPublic && plan.isAllowed && viewPlanType==='pesonal'"
                                    dense round flat
                                    size="1.2em"
                                    icon="del"
                                    color="grey-8"
                                    @click="delUsrPlan(plan)"
                                />

                                <q-btn
                                    v-if="plan.isPublic && !plan.isAllowed && viewPlanType==='common'"
                                    dense round flat
                                    size="1.2em"
                                    icon="add"
                                    color="green-9"
                                    @click="addUsrPlan(plan)"
                                />


                                <!--
                                                                <q-btn flat dense round
                                                                       icon="more-h"
                                                                       size="1.0em"
                                                                />
                                -->

                            </div>

                        </q-item-section>


                        <q-item-section
                            top side
                            style="width: 2em; align-content: end;">

                            <div>

                                <q-badge
                                    v-if="plan.ratingTask > 0"
                                    color="green-5"
                                    :label="plan.ratingTask"/>

                            </div>

                        </q-item-section>

                    </q-item>

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
            <q-fab :style="{fontSize: '1.3em', height: '4rem', width: isDesktop ? '14rem':'4rem'}"
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
                              @click="onCreateLevel"
                />
                <q-fab-action style="height: 4em; min-width: 18em;"
                              color="amber-10"
                              text-color="black"
                              label="Подключить из библиотеки"
                              square
                              icon="add"
                              @click="onAddLevel"
                />
            </q-fab>
        </q-page-sticky>

    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import gameplay from "./gameplay"
import auth from "./auth"
import LevelsFilterBar from "./comp/LevelsFilterBar"
import MenuContainer from "./comp/MenuContainer"
import TasksStatistic from "./comp/TasksStatistic"
import {daoApi} from "run-game-frontend/src/dao"

export default {

    components: {
        MenuContainer, LevelsFilterBar, TasksStatistic
    },

    props: {
        title: {
            type: String,
            default: null
        },
        onLevelClick: {
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

        onPlanClick(planId) {
            if (this.onLevelClick) {
                this.onLevelClick(planId)
            } else {
                this.onPlanClickDefault(planId)
            }
        },

        onPlanClickDefault(planId) {
            apx.showFrame({
                frame: '/plan',
                props: {
                    planId: planId,
                    frameReturn: "/levels",
                    frameReturnProps: {},
                },

            })
        },

        onCreateLevel() {
            apx.showFrame({
                frame: '/planEdit', props: {
                    immediateSaveMode: false,
                    frameReturn: "/levels",
                    frameReturnProps: {},
                }
            })
        },

        onAddLevel() {
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

<style>

/*
.game-plan-item {
    margin: 0.5em 1em;
    padding: 0.5em 1em 0.8em 1em;

    border: 1px solid #e0e0e0;
    border-radius: 0.5rem;

    font-size: 120%;
    color: #2d2d2d;
}
*/

.item-first {
    border-top: 1px solid silver;
}

.plan-count-text {
    padding: 5px 4px 15px 4px;
    margin-top: 2px;
    width: 6em;
    text-align: center;
    font-size: .6em;
    color: #202020;
    background-color: #dbecfb;
    border-radius: 10px;
}

</style>