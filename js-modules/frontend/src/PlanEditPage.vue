<template>
    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true">


        <div v-if="viewMode==='editPlan'">

            <q-input class="q-mb-sm"
                     dense outlined
                     :disable="!canEditPlan()"
                     label="Название плана"
                     v-model="planText"
            />


            <TaskListFilterBar
                v-model:filterText="filterText"
                v-model:sortField="sortField"
                v-model:showHidden="showHidden"
                v-model:hiddenCount="hiddenCount"
            />


            <TaskList
                v-if="itemsExternal.length > 0"
                :showEdit="true"
                :tasks="itemsExternal"
                :itemsMenu="itemsMenu_modeEdit"
                :filter="filterExt"
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
                        v-if="this.hiddenCountLoaded > 0"
                        v-model="showHiddenLoaded"
                        :label="'Скрытые (' + this.hiddenCountLoaded + ')'"/>
                </template>


            </TextInputText>

            <TaskList
                v-if="itemsLoaded.length > 0"
                :showEdit="true"
                :tasks="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filterLoaded"
            >

            </TaskList>

        </div>


        <div v-if="viewMode==='addByPhoto'">

            <TextInputPhoto
                :items="itemsLoaded"
                :itemsOnChange="itemsOnChange"
            >

                <template v-slot:toolbar>
                    <q-btn
                        v-if="itemsLoaded.length > 0"
                        no-caps
                        class="q-ma-sm"
                        icon="quasar.stepper.done"
                        label="Выбрать все"
                        @click="selectAll()"
                    />

                    <q-toggle
                        v-if="this.hiddenCountLoaded > 0"
                        v-model="showHiddenLoaded"
                        :label="'Скрытые (' + this.hiddenCountLoaded + ')'"/>

                </template>

            </TextInputPhoto>

            <TaskList
                v-if="itemsLoaded.length > 0"
                :showEdit="true"
                :tasks="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filterLoaded"
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
                :itemsMenu="itemsMenu_modeViewItemsAdd"
            />

        </div>

        <div v-if="viewMode==='viewItemsDel'">

            <TaskList
                :showEdit="true"
                :tasks="itemsDel"
                :itemsMenu="itemsMenu_modeViewItemsDel"
            />

        </div>

        <div v-if="viewMode==='viewItemsHideAdd'">

            <TaskList
                :showEdit="true"
                :tasks="itemsHideAdd"
                :itemsMenu="itemsMenu_modeViewHide"
            />

        </div>


        <div v-if="viewMode==='viewItemsHideDel'">

            <TaskList
                :showEdit="true"
                :tasks="itemsHideDel"
                :itemsMenu="itemsMenu_modeViewHide"
            />

        </div>


        <template v-slot:footer>

            <q-footer>

                <q-toolbar class="bg-grey-1 text-black" style="min-height: 4em">


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

                            <q-btn
                                v-if="this.canEditPlan() && this.viewMode !== 'addByText'"
                                round
                                color="yellow-10"
                                class="q-ma-xs"
                                size="1.2em"
                                icon="word-add-keyboard"
                                @click="this.setViewMode('addByText')"
                            />

                            <q-btn
                                v-if="this.canEditPlan() && this.viewMode !== 'addByPhoto'"
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
    name: "PlanEditPage",

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

            itemsExternal: [],
            itemsLoaded: [],

            itemsAdd: [],
            itemsDel: [],
            itemsHideAdd: [],
            itemsHideDel: [],

            hiddenCount: 0,
            hiddenCountLoaded: 0,

            filterText: "",
            sortField: "ratingAsc",
            showHidden: false,
            showHiddenLoaded: false,

            itemsMenu_modeAddFact: [
                {
                    outline: this.itemHideMenuOutline,
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    onClick: this.itemHideMenuClick,
                },
                {
                    outline: this.itemAddMenuOutline,
                    icon: this.itemAddMenuIcon,
                    color: this.itemAddMenuColor,
                    onClick: this.itemAddMenuClick,
                },
            ],

            itemsMenu_modeEdit: [
                {
                    outline: this.itemHideMenuOutline,
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    onClick: this.itemHideMenuClick,
                },
                {
                    icon: this.takeRemoveMenuIcon,
                    color: this.takeRemoveMenuColor,
                    onClick: this.takeRemoveMenuClick,
                    hidden: !this.canEditPlan(),
                },
            ],

            itemsMenu_modeViewItemsDel: [
                {
                    outline: this.itemHideMenuOutline,
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    onClick: this.itemHideMenuClick,
                },
                {
                    outline: this.itemAddMenuOutline,
                    icon: this.takeRemoveMenuIcon,
                    color: this.takeRemoveMenuColor,
                    onClick: this.takeRemoveMenuClick,
                    hidden: !this.canEditPlan(),
                },
            ],

            itemsMenu_modeViewItemsAdd: [
                {
                    outline: true,
                    icon: this.itemDeleteMenuIcon,
                    color: this.itemDeleteMenuColor,
                    onClick: this.itemDeleteMenuClick,
                },
            ],

            itemsMenu_modeViewHide: [
                {
                    outline: true,
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    onClick: this.itemHideMenuClick,
                },
            ],

        }
    },

    watch: {
        sortField: function(value, old) {
            this.itemsExternal.sort(this.compareFunction)
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

        selectAll() {
            let itemsAdd = []
            for (let taskItem of this.itemsLoaded) {
                if ((taskItem.isHidden && this.showHiddenLoaded) || (!taskItem.isHidden && !this.showHiddenLoaded)) {
                    itemsAdd.push(taskItem)
                }
            }
            this.itemsAddItems(itemsAdd)
        },


        calcHiddenCountExternal() {
            this.hiddenCount = 0
            for (let item of this.itemsExternal) {
                if (item.isHidden) {
                    this.hiddenCount = this.hiddenCount + 1
                }
            }
            //
            if (this.hiddenCount === 0) {
                this.showHidden = false
            }
        },

        calcHiddenCountLoaded() {
            this.hiddenCountLoaded = 0
            for (let item of this.itemsLoaded) {
                if (item.isHidden) {
                    this.hiddenCountLoaded = this.hiddenCountLoaded + 1
                }
            }
            //
            if (this.hiddenCountLoaded === 0) {
                this.showHiddenLoaded = false
            }
        },

        itemsOnChange() {

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
            }

            // Удобнее держать отдельную переменную this.hiddenCount
            this.calcHiddenCountLoaded()
        },

        filterLoaded(taskItem) {
            if (taskItem.isHidden && !this.showHiddenLoaded) {
                return false
            }

            if (!taskItem.isHidden && this.showHiddenLoaded) {
                return false
            }

            //
            if (taskItem.isDeleted && !this.showHiddenLoaded) {
                return false
            }

            return true
        },

        filterExt(taskItem) {
            if (taskItem.isHidden && !this.showHidden) {
                return false
            }

            if (!taskItem.isHidden && this.showHidden) {
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

        contains(filterLoaded, value) {
            if (!filterLoaded) {
                return true
            }

            if (!value) {
                return false
            }

            if (value.toLowerCase().includes(filterLoaded.toLowerCase())) {
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


        itemHideMenuOutline(taskItem) {
            let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
            let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)
            if (posItemsHideAdd !== -1 || posItemsHideDel !== -1) {
                return true
            } else {
                return false
            }
        },

        itemHideMenuIcon(taskItem) {
            if (taskItem.isHidden) {
                return "visibility-off"
            } else {
                return "visibility"
            }
        },

        itemHideMenuColor(taskItem) {
            return "grey-7"
/*
            if (taskItem.isHidden) {
                let p = utils.itemPosInItems(taskItem, this.itemsHideAdd)
                if (p !== -1) {
                    return "grey-7"
                } else {
                    return "grey-7"
                }
            } else {
                let p = utils.itemPosInItems(taskItem, this.itemsHideDel)
                if (p !== -1) {
                    return "grey-7"
                } else {
                    return "grey-7"
                }
            }
*/
        },

        itemHideMenuClick(taskItem) {
            if (this.itemIsHidden(taskItem)) {
                this.itemHideDel(taskItem)
            } else {
                this.itemHideAdd(taskItem)
            }


            //
            if (this.itemsHideAdd.length === 0 && this.viewMode === "viewItemsHideAdd") {
                this.setViewMode(this.defaultViewMode)
                /*
                                if (this.canEditPlan()) {
                                    this.setViewMode("addByText")
                                } else {
                                    this.setViewMode("editPlan")
                                }
                */
            }
            //
            if (this.itemsHideDel.length === 0 && this.viewMode === "viewItemsHideDel") {
                this.setViewMode(this.defaultViewMode)
                /*
                                if (this.canEditPlan()) {
                                    this.setViewMode("addByText")
                                } else {
                                    this.setViewMode("editPlan")
                                }
                */
            }
        },

        /* -------------------------------- */

        itemAddMenuOutline(taskItem) {
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsDel = utils.itemPosInItems(taskItem, this.itemsDel)
            if (posItemsAdd !== -1 || posItemsDel !== -1) {
                return true
            } else {
                return false
            }
        },

        itemAddMenuIcon(taskItem) {
            if (this.itemIsAdded(taskItem)) {
                let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                if (p !== -1) {
                    return "del"
                    //return "quasar.stepper.done"
                } else {
                    return "del"
                    //return "quasar.stepper.done"
                }
            } else {
                return "add"
            }
        },

        itemAddMenuColor(taskItem) {
            return "grey-8"

/*
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            if (p !== -1 || posItemsExt !== -1) {
                if (p !== -1) {
                    return "green-6"
                } else {
                    return "grey-7"
                }
            } else {
                return "grey-7"
            }
*/
        },

        itemAddMenuClick(taskItem) {
            if (this.itemIsAdded(taskItem)) {
                this.itemDel(taskItem)
            } else {
                this.itemAdd(taskItem)
            }
        },

        /* -------------------------------- */

        takeRemoveMenuIcon(taskItem) {
            if (this.itemIsAdded(taskItem)) {
                let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                if (p !== -1) {
                    return "del"
                    //return "quasar.stepper.done"
                } else {
                    return "del"
                    //return "quasar.stepper.done"
                }
            } else {
                return "add"
            }
        },

        takeRemoveMenuColor(taskItem) {
            return "grey-8"
/*
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            if (p !== -1) {
                return "red-6"
            } else {
                return "grey-8"
            }
*/
        },

        takeRemoveMenuClick(taskItem) {
            if (this.itemIsAdded(taskItem)) {
                this.itemDel(taskItem)
            } else {
                this.itemAdd(taskItem)
            }

            //
            if (this.itemsDel.length === 0 && this.viewMode === "viewItemsDel") {
                this.setViewMode("addByText")
            }
        },

        /* -------------------------------- */

        itemDeleteMenuIcon(taskItem) {
            if (this.itemIsAdded(taskItem)) {
                return "del"
            } else {
                return "add"
            }
        },

        itemDeleteMenuColor(taskItem) {
            return "grey-8"
/*
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                return "red-6"
            } else {
                return "grey-7"
            }
*/
        },

        itemDeleteMenuClick(taskItem) {
            if (this.itemIsAdded(taskItem)) {
                this.itemDel(taskItem)
            } else {
                this.itemAdd(taskItem)
            }

            //
            if (this.itemsAdd.length === 0) {
                this.setViewMode(this.defaultViewMode)
                /*
                                if (this.canEditPlan()) {
                                    this.setViewMode("addByText")
                                } else {
                                    this.setViewMode("editPlan")
                                }
                */
            }
        },

        /* -------------------------------- */

        /**
         * После сохранения taskItem будет в плане и не скрыт
         */
        itemAdd(taskItem) {
            console.info("itemAdd", taskItem)

            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsDel = utils.itemPosInItems(taskItem, this.itemsDel)

            // Согласуем нахождение Item в других списках
            if (posItemsExt === -1 && posItemsAdd === -1) {
                this.itemsAdd.push(taskItem)
            }
            //
            if (posItemsDel !== -1) {
                this.itemsDel.splice(posItemsDel, 1)
            }
            //
            if (taskItem.isHidden) {
                let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
                if (posItemsHideAdd !== -1) {
                    this.itemsHideAdd.splice(posItemsHideAdd, 1)
                }
                //
                let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)
                if (posItemsHideDel === -1 && posItemsHideAdd === -1) {
                    this.itemsHideDel.push(taskItem)
                }
            }

            // Собственное состояние
            taskItem.isHidden = false
            //taskItem.isDeleted = false

            // Подправим состояние "isHidden" в основном списке
            if (posItemsExt !== -1) {
                this.itemsExternal[posItemsExt].isHidden = false
                this.itemsExternal[posItemsExt].isDeleted = false
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
        },

        /**
         * После сохранения taskItem не будет в плане
         */
        itemDel(taskItem) {
            console.info("itemDel", taskItem)

            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsDel = utils.itemPosInItems(taskItem, this.itemsDel)

            // Согласуем нахождение Item в других списках
            if (posItemsAdd !== -1) {
                this.itemsAdd.splice(posItemsAdd, 1)
            }
            //
            if (posItemsExt !== -1 && posItemsDel === -1) {
                this.itemsDel.push(taskItem)
            }
            //
            if (taskItem.isHidden) {
                let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
                if (posItemsHideAdd !== -1) {
                    this.itemsHideAdd.splice(posItemsHideAdd, 1)
                }
                //
                let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)
                if (posItemsHideDel === -1) {
                    this.itemsHideDel.splice(posItemsHideDel, 1)
                }
            }

            // Собственное состояние
            taskItem.isHidden = false

            // Подправим состояние "isHidden" в основном списке
            if (posItemsExt !== -1) {
                this.itemsExternal[posItemsExt].isHidden = false
                this.itemsExternal[posItemsExt].isDeleted = true
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
        },

        itemHideAdd(taskItem) {
            // Собственное состояние
            taskItem.isHidden = true
            taskItem.isKnownGood = false
            taskItem.isKnownBad = false

            // Согласуем нахождение Item в других списках
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsDel = utils.itemPosInItems(taskItem, this.itemsDel)
            let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
            let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)

            if (posItemsHideAdd === -1 && posItemsHideDel === -1) {
                this.itemsHideAdd.push(taskItem)
            }
            if (posItemsDel !== -1) {
                this.itemsDel.splice(posItemsDel, 1)
            }
            if (posItemsHideDel !== -1) {
                this.itemsHideDel.splice(posItemsHideDel, 1)
            }
            if (posItemsAdd !== -1) {
                this.itemsAdd.splice(posItemsAdd, 1)
            }

            // Подправим состояние "isHidden" в основном списке
            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            if (posItemsExt !== -1) {
                this.itemsExternal[posItemsExt].isHidden = true
                this.itemsExternal[posItemsExt].isDeleted = false
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
        },

        itemHideDel(taskItem) {
            // Собственное состояние
            taskItem.isHidden = false

            //
            let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
            let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)

            // Согласуем нахождение Item в других списках
            if (posItemsHideAdd !== -1) {
                this.itemsHideAdd.splice(posItemsHideAdd, 1)
            }
            if (posItemsHideDel === -1 && posItemsHideAdd === -1) {
                this.itemsHideDel.push(taskItem)
            }

            // Подправим состояние "isHidden" в основном списке
            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            if (posItemsExt !== -1) {
                this.itemsExternal[posItemsExt].isHidden = false
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
        },

        /**
         * @returns {boolean} true, если taskItem находится в плане или будет добавлен при сохранении
         */
        itemIsAdded(taskItem) {
            let posItemsDel = utils.itemPosInItems(taskItem, this.itemsDel)
            if (posItemsDel !== -1) {
                return false;
            }

            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            if (posItemsExt !== -1) {
                return true;
            }

            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (posItemsAdd !== -1) {
                return true;
            }

            return false;
        },

        /**
         * @returns {boolean} true, если taskItem скрыт или будет скрыт при сохранении
         */
        itemIsHidden(taskItem) {
            return taskItem.isHidden;
        },

        itemsAddItems(taskItems) {
            for (let taskItem of taskItems) {
                if (!this.itemIsAdded(taskItem)) {
                    this.itemAdd(taskItem)
                }
            }

            //
            this.calcHiddenCountExternal()
            this.calcHiddenCountLoaded()
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


        canEditPlan() {
            return !this.plan || this.plan.isOwner === true
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
            // ---
            // Параметры для вызова api


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


            // ---
            // Вызов api


            // План
            let planId
            if (!this.plan) {
                // Создаем новый план и добавляем слова
                let recPlan = {text: this.planText}
                planId = await daoApi.invoke('m/Plan/ins', [recPlan, stPlanFactAdd, []])
            } else {
                planId = this.plan.id

                if (this.canEditPlan()) {
                    // Редактируем план
                    let recPlan = {id: planId, text: this.planText}
                    await daoApi.invoke('m/Plan/upd', [recPlan])

                    // Добавляем слова в существующий план
                    await daoApi.invoke('m/Plan/addFact', [planId, stPlanFactAdd])

                    // Удаляем слова в существующем плане
                    await daoApi.invoke('m/Plan/delFact', [planId, stPlanFactDel])
                }
            }

            // Скрытые и показанные факты
            await ctx.gameplay.api_saveUsrFacts(stPlanFactHideAddHideDel, planId)


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

        //
        if (this.planItems) {
            this.itemsExternal = this.planItems
        }

        //
        if (this.plan) {
            this.planText = this.plan.planText
        } else {
            // Еще не созданный план
            let planTextDt = apx.date.toDisplayStr(apx.date.today())
            this.planText = "Уровень " + planTextDt
        }


        // Удобнее держать отдельную переменную this.hiddenCount
        this.calcHiddenCountExternal()
    },


}

</script>


<style scoped>

.items-count-info {
    margin-top: auto;
    margin-bottom: auto;
}

</style>