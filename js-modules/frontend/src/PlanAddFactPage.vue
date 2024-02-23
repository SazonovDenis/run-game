<template>
    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true">


        <div v-if="viewMode==='editPlan'">

            <q-input class="q-mb-sm"
                     dense outlined
                     label="Название плана"
                     v-model="planText"
            />


            <TaskListFilterBar
                v-model:filterText="filterText"
                v-model:sortField="sortField"
                v-model:showHidden="showHidden"
                v-model:hiddenCount="planItemsHiddenCount"
            />


            <TaskList
                v-if="planItems.length > 0"
                :showEdit="true"
                :tasks="planItems"
                :itemsMenu="itemsMenu_modeEdit"
                :filter="filter"
            >

            </TaskList>

        </div>


        <div v-if="viewMode==='addByText'">

            <TextInputText
                :items="itemsLoaded"
                :itemsOnChange="itemsOnChange"
            >

                <template v-slot:toolbar>
                    <q-toggle
                        v-if="this.hiddenCount > 0"
                        v-model="showHidden"
                        :label="'Скрытые (' + this.hiddenCount + ')'"/>
                </template>


            </TextInputText>

            <TaskList
                v-if="itemsLoaded.length > 0"
                :showEdit="true"
                :tasks="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filter"
            >

            </TaskList>

        </div>


        <div v-if="viewMode==='addByPhoto'">

            <TextInputPhoto
                :items="itemsLoaded"
                :itemsOnChange="itemsOnChange"
            >

                <template v-slot:toolbar>
                    <q-toggle
                        v-if="this.hiddenCount > 0"
                        v-model="showHidden"
                        :label="'Скрытые (' + this.hiddenCount + ')'"/>

                </template>

            </TextInputPhoto>

            <TaskList
                v-if="itemsLoaded.length > 0"
                :showEdit="true"
                :tasks="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filter"
            />

        </div>


        <div v-if="viewMode==='viewItemsAdd'">

            <q-input v-if="!this.plan" class="q-mb-sm"
                     dense outlined
                     label="Название плана"
                     v-model="planText"
            />

            <TaskList
                :showEdit="true"
                :tasks="itemsAdd"
                :itemsMenu="itemsMenuAdd"
            />

        </div>

        <div v-if="viewMode==='viewItemsDel'">

            <TaskList
                :showEdit="true"
                :tasks="itemsDel"
                :itemsMenu="itemsMenu_modeEdit"
            />

        </div>

        <div v-if="viewMode==='viewItemsHideAdd'">

            <TaskList
                :showEdit="true"
                :tasks="itemsHideAdd"
                :itemsMenu="itemsMenuHide"
            />

        </div>


        <div v-if="viewMode==='viewItemsHideDel'">

            <TaskList
                :showEdit="true"
                :tasks="itemsHideDel"
                :itemsMenu="itemsMenuHide"
            />

        </div>


        <template v-slot:footer>

            <q-footer>

                <q-toolbar class="bg-grey-1 text-black" style="min-height: 5em">


                    <div class="row " style="width: 100%">
                        <div
                            style="position: absolute; top: -3em;">

                            <q-btn v-if="plan && this.viewMode !== 'editPlan'"
                                   round
                                   color="purple-4"
                                   class="q-ma-xs"
                                   icon="edit"
                                   size="1.2em"
                                   @click="this.setViewMode('editPlan')"
                            />

                            <q-btn v-if="this.viewMode !== 'addByText'"
                                   round
                                   color="yellow-10"
                                   class="q-ma-xs"
                                   size="1.2em"
                                   icon="word-add-keyboard"
                                   @click="this.setViewMode('addByText')"
                            />
                            <q-btn v-if="this.viewMode !== 'addByPhoto'"
                                   round
                                   color="yellow-9"
                                   class="q-ma-xs"
                                   size="1.2em"
                                   icon="word-add-photo"
                                   @click="this.setViewMode('addByPhoto')"
                            />
                        </div>

                        <div class="row" style="flex-grow: 10; align-content: end">
                            <div style="flex-grow: 10">
                                &nbsp;
                            </div>

                            <template v-if="itemsAdd.length > 0">

                                <div
                                    class="q-ma-xs items-count-info"
                                    @click="clickItemsAddText()">
                                    <span class="rgm-link-soft">
                                        {{ itemsAddText }}
                                    </span>
                                </div>

                            </template>

                            <template v-if="itemsHideAdd.length > 0">

                                <div
                                    class="q-ma-xs items-count-info"
                                    @click="clickItemsHideAddText()">
                                    <span class="rgm-link-soft">
                                        {{ itemsHideAddText }}
                                    </span>
                                </div>

                            </template>

                            <template v-if="itemsHideDel.length > 0">

                                <div
                                    class="q-ma-xs items-count-info"
                                    @click="clickItemsHideDelText()">
                                    <span class="rgm-link-soft">
                                        {{ itemsHideDelText }}
                                    </span>
                                </div>

                            </template>

                            <template v-if="itemsDel.length > 0">

                                <div
                                    class="q-ma-xs items-count-info"
                                    @click="clickItemsDelText()">
                                    <span class="rgm-link-soft">
                                        {{ itemsDelText }}
                                    </span>
                                </div>

                            </template>

                            <template
                                v-if="(plan && plan.planText !== planText) || itemsAdd.length > 0 || itemsDel.length > 0 || itemsHideAdd.length > 0 || itemsHideDel.length > 0">

                                <template v-if="!plan && viewMode !== 'viewItemsAdd'">

                                    <q-btn
                                        no-caps
                                        class="q-ma-sm"
                                        label="Далее"
                                        @click="btnNextClick()"
                                    />

                                </template>

                                <template v-else>

                                    <q-btn
                                        no-caps
                                        class="q-ma-sm"
                                        :label="btnSaveTitle"
                                        @click="btnSaveClick()"
                                    />

                                </template>


                            </template>


                        </div>
                    </div>
                </q-toolbar>

            </q-footer>

        </template>


    </MenuContainer>
</template>

<script>

import {daoApi} from "./dao"
import MenuContainer from "./comp/MenuContainer"
import TaskList from "./comp/TaskList"
import TextInputPhoto from "./comp/TextInputPhoto"
import TextInputText from "./comp/TextInputText"
import TaskListFilterBar from "./comp/TaskListFilterBar"
import auth from "./auth"
import {apx} from "./vendor"
import utils from "./utils"
import ctx from "./gameplayCtx"


export default {
    name: "PlanAddFactPage",

    components: {
        MenuContainer, TaskListFilterBar, TextInputPhoto, TextInputText, TaskList
    },

    props: {
        plan: null,
        planItems: null,

        defaultViewMode: null,
        frameReturn: null,
        frameReturnProps: null,
    },

    data() {
        return {
            planText: null,

            viewMode: "addByText",

            itemsLoaded: [],

            itemsAdd: [],
            itemsDel: [],
            itemsHideAdd: [],
            itemsHideDel: [],

            hiddenCount: 0,
            planItemsHiddenCount: 0,

            filterText: "",
            sortField: "ratingAsc",
            showHidden: false,

            itemsMenu_modeAddFact: [
                {
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    itemMenuClick: this.itemHideMenuClick,
                },
                {
                    icon: this.itemAddMenuIcon,
                    color: this.itemAddMenuColor,
                    itemMenuClick: this.itemAddMenuClick,
                },
            ],

            itemsMenu_modeEdit: [
                {
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    itemMenuClick: this.itemHideMenuClick,
                },
                {
                    icon: this.takeRemoveMenuIcon,
                    color: this.takeRemoveMenuColor,
                    itemMenuClick: this.takeRemoveMenuClick,
                },
            ],

            itemsMenuAdd: [
                {
                    icon: this.itemDeleteMenuIcon,
                    color: this.itemDeleteMenuColor,
                    itemMenuClick: this.itemDeleteMenuClick,
                },
            ],

            itemsMenuHide: [
                {
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    itemMenuClick: this.itemHideMenuClick,
                },
            ],

        }
    },

    watch: {
        sortField: function(value, old) {
            this.planItems.sort(this.compareFunction)
        }
    },


    computed: {

        title() {
            if (this.viewMode === "editPlan") {
                return "Редактирование плана"
            } else if (this.viewMode === "addByPhoto") {
                return "Добавление слов"
            } else if (this.viewMode === "addByText") {
                return "Добавление слов"
            } else if (this.viewMode === "viewItemsHideAdd") {
                return "Скрытые слова"
            } else if (this.viewMode === "viewItemsHideDel") {
                return "Показанные слова"
            } else if (this.viewMode === "viewItemsDel") {
                return "Удаленные слова"
            } else if (this.viewMode === "viewItemsAdd") {
                if (this.plan) {
                    return "Добавленные слова"
                } else {
                    return "Создание плана"
                }
            }
        },

        btnSaveTitle() {
            if (this.plan) {
                return "Сохранить"
            } else {
                return "Создать план"
            }
        },

        itemsAddText() {
            return "Добавлено: " + this.itemsAdd.length
        },

        itemsDelText() {
            return "Удалено: " + this.itemsDel.length
        },

        itemsHideAddText() {
            return "Скрыто: " + this.itemsHideAdd.length
        },

        itemsHideDelText() {
            return "Показано: " + this.itemsHideDel.length
        },

    },

    methods: {

        itemsOnChange() {

            // Удобнее держать отдельную переменную this.hiddenCount
            this.hiddenCount = 0
            for (let item of this.itemsLoaded) {
                if (item.isHidden) {
                    this.hiddenCount = this.hiddenCount + 1
                }
            }

            // Для новой порции слов учтем, какие мы только что скрыли и добавили
            for (let item of this.itemsLoaded) {
                let posItemsHideAdd = utils.itemPosInItems(item, this.itemsHideAdd)
                if (posItemsHideAdd !== -1) {
                    item.isHidden = true
                }

                let posItemsHideDel = utils.itemPosInItems(item, this.itemsHideDel)
                if (posItemsHideDel !== -1) {
                    item.isHidden = false
                }

                let posItemsAdd = utils.itemPosInItems(item, this.itemsAdd)
                if (posItemsAdd !== -1) {
                    item.isInItemsAdd = true
                }
            }

        },

        filter(taskItem) {
            if (taskItem.isHidden && !this.showHidden) {
                return false
            }

            //
            if (taskItem.isDeleted && !this.showHidden) {
                return false
            }

            //
            if (!this.filterText) {
                return true
            } else {
                if (this.contains(this.filterText, taskItem.question.valueTranslate)) {
                    return true
                }

                if (this.contains(this.filterText, taskItem.question.valueSpelling)) {
                    return true
                }

                if (this.contains(this.filterText, taskItem.answer.valueTranslate)) {
                    return true
                }

                if (this.contains(this.filterText, taskItem.answer.valueSpelling)) {
                    return true
                }
            }

            //
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

        /* -------------------------------- */

        itemHideMenuIcon(taskItem) {
            if (taskItem.isHidden) {
                return "visibility-off"
            } else {
                return "visibility"
            }
        },

        itemHideMenuColor(taskItem) {
            if (taskItem.isHidden) {
                let p = utils.itemPosInItems(taskItem, this.itemsHideAdd)
                if (p !== -1) {
                    return "red-6"
                } else {
                    return "grey-7"
                }
            } else {
                let p = utils.itemPosInItems(taskItem, this.itemsHideDel)
                if (p !== -1) {
                    return "red-5"
                } else {
                    return "grey-6"
                }
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
            if (taskItem.isHidden) {
                this.hiddenCount = this.hiddenCount + 1
            } else {
                this.hiddenCount = this.hiddenCount - 1
            }


            //
            //ctx.gameplay.api_saveUsrFact(taskItem.factQuestion, taskItem.factAnswer, taskItem)


            //
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
            let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)

            //
            if (taskItem.isHidden) {
                if (posItemsHideAdd === -1 && posItemsHideDel === -1) {
                    this.itemsHideAdd.push(taskItem)
                }
                if (posItemsHideDel !== -1) {
                    this.itemsHideDel.splice(posItemsHideDel, 1)
                }
                if (posItemsAdd !== -1) {
                    this.itemsAdd.splice(posItemsAdd, 1)
                    taskItem.isInItemsAdd = false
                }
            } else {
                if (posItemsHideAdd !== -1) {
                    this.itemsHideAdd.splice(posItemsHideAdd, 1)
                }
                if (posItemsHideDel === -1 && posItemsHideAdd === -1) {
                    this.itemsHideDel.push(taskItem)
                }
            }


            //
            if (this.itemsHideAdd.length === 0 && this.viewMode === "viewItemsHideAdd") {
                this.setViewMode("addByText")
            }
            //
            if (this.itemsHideDel.length === 0 && this.viewMode === "viewItemsHideDel") {
                this.setViewMode("addByText")
            }
        },

        /* -------------------------------- */

        itemAddMenuIcon(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                return "quasar.stepper.done"
            } else {
                return "add"
            }
        },

        itemAddMenuColor(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                return "green-6"
            } else {
                return "grey-7"
            }
        },

        itemAddMenuClick(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                this.itemsAdd.splice(p, 1)
            } else {
                this.itemsAdd.push(taskItem)

                //
                if (taskItem.isHidden) {
                    let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
                    let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)
                    if (posItemsHideAdd !== -1) {
                        this.itemsHideAdd.splice(posItemsHideAdd, 1)
                    }
                    if (posItemsHideDel === -1 && posItemsHideAdd === -1) {
                        this.itemsHideDel.push(taskItem)
                    }
                }
                //
                taskItem.isHidden = false
            }
        },

        /* -------------------------------- */

        takeRemoveMenuIcon(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            if (p !== -1) {
                return "del"
            } else {
                return "del"
            }
        },

        takeRemoveMenuColor(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            if (p !== -1) {
                return "red-6"
            } else {
                return "grey-8"
            }
        },

        takeRemoveMenuClick(taskItem) {
            // Собственное состояние
            taskItem.isDeleted = !taskItem.isDeleted
            if (taskItem.isDeleted) {
                taskItem.isKnownGood = false
                taskItem.isKnownBad = false
            }


            // Общий счетчик
            if (taskItem.isDeleted) {
                this.hiddenCount = this.hiddenCount + 1
            } else {
                this.hiddenCount = this.hiddenCount - 1
            }


            // Состояние в списках
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            //
            if (taskItem.isDeleted && p === -1) {
                this.itemsDel.push(taskItem)
            }
            //
            if (!taskItem.isDeleted && p !== -1) {
                this.itemsDel.splice(p, 1)
            }

            //
            if (this.itemsDel.length === 0 && this.viewMode === "viewItemsDel") {
                this.setViewMode("addByText")
            }
        },

        /* -------------------------------- */

        itemDeleteMenuIcon(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                return "clear"
            } else {
                return "add"
            }
        },

        itemDeleteMenuColor(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                return "grey-7"
            } else {
                return "grey-7"
            }
        },

        itemDeleteMenuClick(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                this.itemsAdd.splice(p, 1)
            }

            //
            if (this.itemsAdd.length === 0) {
                this.setViewMode("addByText")
            }
        },

        /* -------------------------------- */

        clickItemsAddText() {
            this.setViewMode("viewItemsAdd")
        },

        clickItemsDelText() {
            this.setViewMode("viewItemsDel")
        },

        clickItemsHideAddText() {
            this.setViewMode("viewItemsHideAdd")
        },

        clickItemsHideDelText() {
            this.setViewMode("viewItemsHideDel")
        },

        setViewMode(viewMode) {
            if (viewMode !== this.viewMode) {
                // Фильтр очищаем, ведь ищем заново
                this.filterText = ""

                // Ранее загруженные слова очищаем, ведь ищем заново
                this.itemsLoaded.length = 0
                this.itemsOnChange()
            }
            this.viewMode = viewMode
        },

        btnNextClick() {
            this.viewMode = "viewItemsAdd"
        },

        async btnSaveClick() {
            // Добавленные факты
            let stPlanFactAdd = []
            for (let item of this.itemsAdd) {
                stPlanFactAdd.push({
                    factAnswer: item.factAnswer,
                    factQuestion: item.factQuestion,
                })
            }

            // Удаленные факты
            let stPlanFactDel = []
            for (let item of this.itemsDel) {
                stPlanFactDel.push({
                    factAnswer: item.factAnswer,
                    factQuestion: item.factQuestion,
                })
            }

            // Скрытые и показанные факты
            let stPlanFactHideAddHideDel = []
            for (let item of this.itemsHideAdd) {
                stPlanFactHideAddHideDel.push({
                    isHidden: item.isHidden,
                    factAnswer: item.factAnswer,
                    factQuestion: item.factQuestion,
                })
            }
            for (let item of this.itemsHideDel) {
                stPlanFactHideAddHideDel.push({
                    isHidden: item.isHidden,
                    factAnswer: item.factAnswer,
                    factQuestion: item.factQuestion,
                })
            }

            //
            let planId
            if (!this.plan) {
                // Создаем новый план и добавляем слова
                let recPlan = {text: this.planText}
                planId = await daoApi.invoke('m/Plan/ins', [recPlan, stPlanFactAdd, []])
            } else {
                planId = this.plan.id

                // Редактируем план
                let recPlan = {id: planId, text: this.planText}
                await daoApi.invoke('m/Plan/upd', [recPlan])

                // Добавляем слова в существующий план
                await daoApi.invoke('m/Plan/addFact', [planId, stPlanFactAdd])

                // Удаляем слова в существующем плане
                await daoApi.invoke('m/Plan/delFact', [planId, stPlanFactDel])
            }

            // Скрытые и показанные факты
            for (let item of stPlanFactHideAddHideDel) {
                await ctx.gameplay.api_saveUsrFact(item.factQuestion, item.factAnswer, item)
            }

            //
            apx.showFrame({
                frame: '/plan', props: {planId: planId}
            })

        },

    },

    async mounted() {
        // Есть текущий пользователь?
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login'
            })
            return
        }

        //
        if (this.defaultViewMode) {
            this.viewMode = this.defaultViewMode
        }

        // Для обеспечения возможности добавлять слова в еще не созданный план
        if (!this.plan) {
            let planTextDt = apx.date.toDisplayStr(apx.date.today())
            this.planText = "Уровень " + planTextDt
        } else {
            this.planText = this.plan.planText
        }


        // Удобнее держать отдельную переменную this.planItemsHiddenCount
        this.planItemsHiddenCount = 0
        for (let item of this.planItems) {
            if (item.isHidden) {
                this.planItemsHiddenCount = this.planItemsHiddenCount + 1
            }
        }
    },


}

</script>


<style scoped>

.items-count-info {
    margin-top: auto;
    margin-bottom: auto;
}

</style>