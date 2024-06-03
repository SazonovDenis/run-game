<template>
    <MenuContainer
        :title="title"
        tabMenuName="PlanEditPage"
        :frameReturn="getFrameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true">


        <!-- frameMode: editPlan -->

        <div v-if="frameMode==='editPlan'">

            <q-input class="q-mb-sm"
                     dense outlined
                     :disable="!canEditPlan()"
                     label="Название уровня"
                     v-model="planText"
            />


            <TaskListFilterBar
                v-model:filterText="filterText"
                v-model:sortField="sortField"
                v-model:showHidden="showHidden"
                v-model:hiddenCount="hiddenCount"
            />


            <div>

                <TaskList
                    v-if="(!showHidden && visibleCount > 0) || (showHidden && hiddenCount > 0)"
                    :showEdit="true"
                    :tasks="itemsExternal"
                    :itemsMenu="itemsMenu_modeEdit"
                    :filter="filterExt"
                    :actionRightSlide="actionDelete"
                    :actionLeftSlide="actionHide"
                >

                </TaskList>

                <div v-else-if="hiddenCount > 0"
                     class="rgm-state-text">
                    В уровне есть только скрытые слова
                </div>

                <div v-else
                     class="rgm-state-text">
                    В уровне нет ни одного слова
                </div>

            </div>

        </div>


        <!-- frameMode: addByText -->

        <div v-show="canEditItemList() && frameMode==='addByText'">

            <TextInputText
                ref="textInputText"
                :planId="planId"
                :items="itemsLoaded"
                :isToolbarUsed="this.hiddenCountLoaded > 0"
                @itemsChange="itemsOnChange"
                @itemsLoading="itemsOnLoading"
                @paste="onTextInputPaste"
            >

                <template v-slot:toolbar>
                    <q-toggle
                        v-if="this.hideHiddenMode && this.hiddenCountLoaded > 0"
                        v-model="showHiddenLoaded"
                        :label="'Известные (' + this.hiddenCountLoaded + ')'"/>
                </template>

                <template v-slot:append>

                    <q-icon name="image"
                            @click="clickImageBtn"/>

                    <!--
                    <q-icon v-if="!hasClipboardImage" name="folder-open"
                            @click="clickFileChoose"/>
                    -->

                </template>

            </TextInputText>

            <input style="display: none;"
                   type="file"
                   _multiple
                   accept="image/*"
                   ref="fileInput"
                   @change="onFileChoose"
            >

            <div ref="output">Paste an image here</div>

            <TaskList
                v-if="itemsShouldLoad"
                :showEdit="true"
                :tasks="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filterLoaded"
                :actionLeftSlide="actionHide"
                :messageNoItems="itemsIsLoading ? '' : 'Слова не найдены'"
            >

            </TaskList>

        </div>


        <!-- frameMode: addByPhoto -->

        <div v-show="canEditItemList() && frameMode==='addByPhoto'">

            <TextInputPhoto
                ref="textInputPhoto"
                :planId="planId"
                :items="itemsLoaded"
                :image="imageLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                @itemsChange="itemsOnChange"
                @itemClick="itemOnClick"
                @fileChoose="clickFileChoose"
            >

                <template v-slot:toolbar>
                    <!--
                                        <q-btn
                                            v-if="itemsLoaded.length > 0"
                                            no-caps
                                            class="q-ma-sm"
                                            icon="quasar.stepper.done"
                                            label="Выбрать все"
                                            @click="selectAll()"
                                        />
                    -->

                    <q-toggle
                        v-if="this.hideHiddenMode && this.hiddenCountLoaded > 0"
                        v-model="showHiddenLoaded"
                        :label="'Скрытые (' + this.hiddenCountLoaded + ')'"/>

                </template>

            </TextInputPhoto>

        </div>


        <div v-if="frameMode==='viewItemsLoaded'">

            <TaskList
                :showEdit="true"
                :tasks="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
            />

        </div>


        <div v-if="frameMode==='viewItemsAdd'">

            <q-input v-if="!this.plan" class="q-mb-sm"
                     dense outlined
                     label="Название уровня"
                     v-model="planText"
            />

            <TaskList
                :showEdit="true"
                :tasks="itemsAdd"
                :itemsMenu="itemsMenu_modeViewItemsAdd"
            />

        </div>


        <div v-if="frameMode==='viewItemsDel'">

            <TaskList
                :showEdit="true"
                :tasks="itemsDel"
                :itemsMenu="itemsMenu_modeViewItemsDel"
            />

        </div>


        <div v-if="frameMode==='viewItemsHideAdd'">

            <TaskList
                :showEdit="true"
                :tasks="itemsHideAdd"
                :itemsMenu="itemsMenu_modeViewHide"
            />

        </div>


        <div v-if="frameMode==='viewItemsHideDel'">

            <TaskList
                :showEdit="true"
                :tasks="itemsHideDel"
                :itemsMenu="itemsMenu_modeViewHide"
            />

        </div>


        <template v-slot:menuBarRight v-if="!isModeView()">

            <div style="display: flex;">

                <q-btn
                    v-if="canEditPlan() && this.frameMode !== 'editPlan'"
                    rounded
                    color="purple-4"
                    class="q-my-xnone q-mx-xs"
                    size="1.3em"
                    icon="edit"
                    @click="this.setFrameMode('editPlan')"
                />

                <q-btn
                    v-if="canEditItemList() && this.frameMode !== 'addByText'"
                    rounded
                    color="yellow-10"
                    class="q-my-xnone q-mx-xs"
                    align="left"
                    size="1.3em"
                    icon="word-add-keyboard"
                    @click="this.setFrameMode('addByText')"
                />

                <q-btn
                    v-if="canEditItemList() && this.frameMode !== 'addByPhoto'"
                    rounded
                    color="yellow-8"
                    class="q-my-xnone q-mx-xs"
                    align="left"
                    size="1.3em"
                    icon="word-add-photo"
                    @click="this.setFrameMode('addByPhoto')"
                />
            </div>

        </template>


        <template v-slot:footer>

            <q-footer v-if="wasChanges()">

                <q-toolbar class="bg-grey-1 text-black" style="min-height: 4em">


                    <div class="row" style="width: 100%">

                        <div class="row" style="flex-grow: 10; align-content: end">

                            <q-space/>

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


                            <template v-if="!plan && frameMode !== 'viewItemsAdd'">

                                <q-btn
                                    v-if="!immediateSaveMode"
                                    no-caps
                                    class="q-ma-xs"
                                    label="Далее"
                                    @click="btnNextClick()"
                                />

                            </template>

                            <template v-else>

                                <q-btn
                                    v-if="!immediateSaveMode && wasChanges()"
                                    no-caps
                                    class="q-ma-xs"
                                    :label="btnSaveTitle"
                                    @click="btnSaveClick()"
                                />

                            </template>


                        </div>
                    </div>
                </q-toolbar>

            </q-footer>

            <div v-else class="bg-white"/>

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
import {apx} from "./vendor"
import utils from "./utils"
import ctx from "./gameplayCtx"


export default {

    components: {
        MenuContainer, TaskListFilterBar, TextInputPhoto, TextInputText, TaskList
    },

    props: {
        plan: null,
        planItems: null,


        immediateSaveMode: {
            type: Boolean,
            default: true,
        },

        hideHiddenMode: {
            type: Boolean,
            default: false,
        },

        doEditPlan: {
            type: Boolean,
            default: true,
        },
        doEditHidden: {
            type: Boolean,
            default: true,
        },
        doEditItemList: {
            type: Boolean,
            default: true,
        },

        defaultMode: null,
        frameReturn: null,
        frameReturnProps: null,
    },

    data() {
        let actionHide = {
            label: "Знаю",
            //info: "Слово больше не появится в играх",
            outline: this.itemHideMenuOutline,
            icon: this.itemHideMenuIcon,
            color: this.itemHideMenuColor,
            textColor: this.itemHideMenuTextColor,
            onClick: this.itemHideMenuClick,
            hidden: this.itemHideMenuHidden,
        };

        let actionDelete = {
            //label: "Убрать из уровня",
            //info: "слово из этого плана",
            icon: this.itemDeleteMenuIcon,
            color: this.itemDeleteMenuColor,
            onClick: this.itemDeleteMenuClick,
            hidden: this.itemAddDelMenuHidden,
        }

        return {
            planText: null,

            frameMode: "addByText",
            editMode: "addByText",
            frameModePrior: null,

            itemsExternal: [],
            itemsLoaded: [],

            //
            imageLoaded: null,

            // Странный способ получить данные из дочернего компонента
            itemsShouldLoad: false,
            itemsIsLoading: false,

            itemsAdd: [],
            itemsDel: [],
            itemsHideAdd: [],
            itemsHideDel: [],

            visibleCount: 0,
            hiddenCount: 0,
            hiddenCountLoaded: 0,

            filterText: "",
            sortField: "ratingAsc",
            showHidden: false,
            showHiddenLoaded: false,

            actionHide: actionHide,
            actionDelete: actionDelete,

            itemsMenu_modeAddFact: [
                {
                    label: "Знаю",
                    outline: this.itemHideMenuOutline,
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    textColor: this.itemHideMenuTextColor,
                    onClick: this.itemHideMenuClick,
                },
                {
                    outline: this.itemAddMenuOutline,
                    icon: this.itemAddMenuIcon,
                    color: this.itemAddMenuColor,
                    onClick: this.itemAddMenuClick,
                    hidden: this.itemAddDelMenuHidden,
                },
            ],

            itemsMenu_modeEdit: [
                actionHide,
                actionDelete,
            ],

            itemsMenu_modeViewItemsDel: [
                {
                    label: "Знаю",
                    outline: this.itemHideMenuOutline,
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    textColor: this.itemHideMenuTextColor,
                    onClick: this.itemHideMenuClick,
                },
                {
                    outline: true,
                    icon: this.itemDeleteMenuIcon,
                    color: this.itemDeleteMenuColor,
                    onClick: this.itemDeleteMenuClick,
                    hidden: this.itemAddDelMenuHidden,
                },
            ],

            itemsMenu_modeViewItemsAdd: [
                {
                    outline: true,
                    icon: this.itemDeleteMenuIcon,
                    color: this.itemDeleteMenuColor,
                    onClick: this.itemDeleteMenuClick,
                    hidden: this.itemAddDelMenuHidden,
                },
            ],

            itemsMenu_modeViewHide: [
                {
                    label: "Знаю",
                    outline: true,
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    textColor: this.itemHideMenuTextColor,
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

        planId() {
            if (this.plan) {
                return this.plan.id
            } else {
                return 0
            }
        },

        title() {
            if (this.isModeView()) {
                if (this.frameMode === "viewItemsHideAdd") {
                    return "Известные слова"
                } else if (this.frameMode === "viewItemsHideDel") {
                    return "Показанные слова"
                } else if (this.frameMode === "viewItemsDel") {
                    return "Удаленные слова"
                } else if (this.frameMode === "viewItemsAdd") {
                    if (this.plan) {
                        return "Добавленные слова"
                    } else {
                        return "Создание уровня"
                    }
                }
            } else if (!this.doEditPlan) {
                return null
            } else if (this.frameMode === "editPlan") {
                return "Редактирование"
            } else if (this.frameMode === "addByText") {
                return "Добавление слов"
            } else if (this.frameMode === "addByPhoto") {
                return "Добавление слов"
            }
        },

        getFrameReturn() {
            if (this.isModeView()) {
                return this.returnFrameMode
            } else {
                return this.frameReturn
            }
        },


        btnSaveTitle() {
            if (this.plan) {
                return "Сохранить"
            } else {
                return "Создать уровень"
            }
        },

        itemsAddText() {
            return "Добавлено: " + this.itemsAdd.length
        },

        itemsDelText() {
            return "Удалено: " + this.itemsDel.length
        },

        itemsHideAddText() {
            return "Известные: +" + this.itemsHideAdd.length
        },

        itemsHideDelText() {
            return "Не известные: +" + this.itemsHideDel.length
        },

    },

    methods: {

        isModeView() {
            return this.frameMode.startsWith("viewItems")
        },

        wasChanges() {
            return (this.plan && this.plan.planText && this.planText && this.plan.planText !== this.planText) ||
                this.itemsAdd.length > 0 ||
                this.itemsDel.length > 0 ||
                this.itemsHideAdd.length > 0 ||
                this.itemsHideDel.length > 0;
        },

        canEditPlan() {
            return this.doEditPlan && (!this.plan || this.plan.isOwner === true)
        },

        canEditHidden() {
            return this.doEditHidden
        },

        canEditItemList() {
            return this.doEditItemList && (!this.plan || this.plan.isOwner === true)
        },

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
                if (item.isHidden && item.isInPlan) {
                    this.hiddenCount = this.hiddenCount + 1
                }
            }
            //
            this.visibleCount = 0
            for (let item of this.itemsExternal) {
                if (!item.isHidden && item.isInPlan) {
                    this.visibleCount = this.visibleCount + 1
                }
            }
        },

        calcHiddenCountLoaded() {
            this.hiddenCountLoaded = 0
            for (let item of this.itemsLoaded) {
                if (item.isHidden) {
                    this.hiddenCountLoaded = this.hiddenCountLoaded + 1
                }
            }
        },

        checkShowHiddenMode() {
            // Если скрытых нет, то выключаем режим "показать скрытые"
            if (this.hiddenCount === 0) {
                this.showHidden = false
            }
            if (this.hiddenCountLoaded === 0) {
                this.showHiddenLoaded = false
            }
        },

        checkViewMode() {
            if (this.itemsAdd.length === 0 && this.frameMode === "viewItemsAdd") {
                this.setFrameMode(this.editMode)
            }

            //
            if (this.itemsDel.length === 0 && this.frameMode === "viewItemsDel") {
                this.setFrameMode(this.editMode)
            }

            //
            if (this.itemsHideAdd.length === 0 && this.frameMode === "viewItemsHideAdd") {
                this.setFrameMode(this.editMode)
            }

            //
            if (this.itemsHideDel.length === 0 && this.frameMode === "viewItemsHideDel") {
                this.setFrameMode(this.editMode)
            }
        },

        itemOnClick(item) {
            this.itemAddMenuClick(item, false)
        },

        itemsOnLoading(itemsIsLoading) {
            this.itemsIsLoading = itemsIsLoading
        },

        itemsOnChange(itemsShouldLoad) {
            this.itemsShouldLoad = itemsShouldLoad

            // Ранее сформированные списки с разницей очищаем, ведь ищем заново
            this.itemsAdd = []
            this.itemsDel = []
            this.itemsHideAdd = []
            this.itemsHideDel = []

            // Удобнее держать отдельную переменную this.hiddenCount
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
            this.checkShowHiddenMode()
        },

        filterLoaded(taskItem) {
            if (!this.hideHiddenMode) {
                return true
            }

            if (taskItem.isHidden && !this.showHiddenLoaded) {
                return false
            }

            if (!taskItem.isHidden && this.showHiddenLoaded) {
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
            if (!taskItem.isInPlan) {
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

        itemHideMenuHidden() {
            return !this.canEditHidden()
        },

        itemHideMenuIcon(taskItem) {
            if (taskItem.isHidden) {
                return "quasar.chip.selected"
            } else {
                return null
            }
        },

        itemHideMenuColor(taskItem) {
            if (taskItem.isHidden) {
                return "indigo-1"
            } else {
                return "white"
            }
        },

        itemHideMenuTextColor(taskItem) {
            if (taskItem.isHidden) {
                return "indigo-10"
            } else {
                return "grey-9"
            }
        },

        itemHideMenuClick(taskItem) {
            if (this.itemIsHidden(taskItem)) {
                this.itemHideDel(taskItem)
            } else {
                this.itemHideAdd(taskItem)

                // Если скрыли - то и в плане делать нечего
                if (this.itemIsInPlan(taskItem) && this.canEditItemList()) {
                    this.itemDel(taskItem)
                }
            }

            //
            this.checkViewMode()
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
            if (this.itemIsInPlan(taskItem)) {
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
        },

        itemAddMenuClick(taskItem, canItemDel = true) {
            // Можно ли удалить добавленный элемент?
            if (this.itemIsInPlan(taskItem) && !canItemDel) {
                return
            }

            // Можно ли показать скрытый элемент?
            if (this.itemIsHidden(taskItem) && !canItemDel) {
                return
            }

            //
            if (this.itemIsInPlan(taskItem)) {
                this.itemDel(taskItem)
            } else {
                this.itemAdd(taskItem)

                // Если добавили в план - то и скрывать незачем
                if (this.itemIsHidden(taskItem) && this.canEditHidden()) {
                    this.itemHideDel(taskItem)
                }
            }
        },

        /* -------------------------------- */

        itemAddDelMenuHidden() {
            return !this.canEditItemList()
        },

        /* -------------------------------- */

        itemDeleteMenuIcon(taskItem) {
            if (this.itemIsInPlan(taskItem)) {
                return "del"
            } else {
                return "add"
            }
        },

        itemDeleteMenuColor(taskItem) {
            return "grey-8"
        },

        itemDeleteMenuClick(taskItem) {
            if (this.itemIsInPlan(taskItem)) {
                this.itemDel(taskItem)
            } else {
                this.itemAdd(taskItem)
            }

            //
            this.checkViewMode()
        },

        /* -------------------------------- */

        /**
         * После сохранения taskItem будет в плане и не скрыт
         */
        itemAdd(taskItem) {
            //console.info("itemAdd", taskItem)

            //
            if (this.itemIsHidden(taskItem)) {
                this.itemHideDel(taskItem)
            }

            //
            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsDel = utils.itemPosInItems(taskItem, this.itemsDel)

            // Согласуем нахождение Item в других списках
            if (posItemsDel === -1 && /*posItemsExt === -1 &&*/ posItemsAdd === -1) {
                this.itemsAdd.push(taskItem)
            }
            //
            if (posItemsDel !== -1) {
                this.itemsDel.splice(posItemsDel, 1)
            }


            // Собственное состояние
            taskItem.isInPlan = true

            // Подправим состояние в основном списке
            if (posItemsExt !== -1) {
                this.itemsExternal[posItemsExt].isInPlan = true
                this.itemsExternal[posItemsExt].isHidden = false
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
            this.checkShowHiddenMode()


            // Синхронизируем с сревером
            this.api_itemAdd(taskItem)
        },

        /**
         * После сохранения taskItem не будет в плане
         */
        itemDel(taskItem) {
            //console.info("itemDel", taskItem)

            //
            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsDel = utils.itemPosInItems(taskItem, this.itemsDel)

            // Согласуем нахождение Item в других списках
            if (posItemsAdd !== -1) {
                this.itemsAdd.splice(posItemsAdd, 1)
            }
            //
            if (posItemsAdd === -1 &&/*posItemsExt !== -1 &&*/ posItemsDel === -1) {
                this.itemsDel.push(taskItem)
            }

            // Собственное состояние
            taskItem.isInPlan = false

            // Подправим состояние "isInPlan" в основном списке
            if (posItemsExt !== -1) {
                this.itemsExternal[posItemsExt].isInPlan = false
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
            this.checkShowHiddenMode()


            // Синхронизируем с сревером
            this.api_itemDel(taskItem)
        },

        itemHideAdd(taskItem) {
            // Собственное состояние
            taskItem.isHidden = true
            taskItem.isKnownGood = false
            taskItem.isKnownBad = false

            // Согласуем нахождение Item в других списках
            let posItemsExt = utils.itemPosInItems(taskItem, this.itemsExternal)
            let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
            let posItemsHideAdd = utils.itemPosInItems(taskItem, this.itemsHideAdd)
            let posItemsHideDel = utils.itemPosInItems(taskItem, this.itemsHideDel)

            if (posItemsAdd !== -1) {
                this.itemsAdd.splice(posItemsAdd, 1)
            }
            if (posItemsHideAdd === -1 && posItemsHideDel === -1) {
                this.itemsHideAdd.push(taskItem)
            }
            if (posItemsHideDel !== -1) {
                this.itemsHideDel.splice(posItemsHideDel, 1)
            }

            // Подправим состояние "isHidden" в основном списке
            if (posItemsExt !== -1) {
                this.itemsExternal[posItemsExt].isHidden = true
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
            this.checkShowHiddenMode()


            // Синхронизируем с сревером
            this.api_itemHideAddDel(taskItem)
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
            if (posItemsHideAdd === -1 && posItemsHideDel === -1) {
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
            this.checkShowHiddenMode()


            // Синхронизируем с сревером
            this.api_itemHideAddDel(taskItem)
        },


        api_itemAdd(item) {
            if (!this.immediateSaveMode) {
                return
            }

            //
            if (!this.plan) {
                return
            }
            let planId = this.plan.id

            //
            let recPlanFact = {
                factQuestion: item.factQuestion,
                factAnswer: item.factAnswer,
            }

            // Добавляем слова в существующий план
            daoApi.invoke('m/Plan/addFact', [planId, [recPlanFact]], {waitShow: false})
        },

        api_itemDel(item) {
            if (!this.immediateSaveMode) {
                return
            }

            //
            if (!this.plan) {
                return
            }
            let planId = this.plan.id

            //
            let recPlanFact = {
                factQuestion: item.factQuestion,
                factAnswer: item.factAnswer,
            }

            // Удаляем слова в существующем плане
            daoApi.invoke('m/Plan/delFact', [planId, [recPlanFact]], {waitShow: false})
        },

        api_itemHideAddDel(item) {
            if (!this.immediateSaveMode) {
                return
            }

            //
            let recItem = {
                isHidden: item.isHidden,
            }

            // Скрытые и показанные факты
            ctx.gameplay.api_saveUsrFact(item.factQuestion, item.factAnswer, recItem)
        },


        /**
         * @returns {boolean} true, если taskItem находится в плане или будет добавлен при сохранении
         */
        itemIsInPlan(taskItem) {
            return taskItem.isInPlan
        },

        /**
         * @returns {boolean} true, если taskItem скрыт или будет скрыт при сохранении
         */
        itemIsHidden(taskItem) {
            return taskItem.isHidden;
        },

        itemsAddItems(taskItems) {
            for (let taskItem of taskItems) {
                if (!this.itemIsInPlan(taskItem)) {
                    this.itemAdd(taskItem)
                }
            }

            //
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
            this.checkShowHiddenMode()
        },

        /* -------------------------------- */

        clickItemsLoadedText() {
            this.setFrameMode("viewItemsLoaded")
        },

        clickItemsAddText() {
            this.setFrameMode("viewItemsAdd")
        },

        clickItemsDelText() {
            this.setFrameMode("viewItemsDel")
        },

        clickItemsHideAddText() {
            this.setFrameMode("viewItemsHideAdd")
        },

        clickItemsHideDelText() {
            this.setFrameMode("viewItemsHideDel")
        },


        async clickImageBtn() {
            let blob = await this.handleClipboardItems()

            // Есть буфер обмена?
            if (blob) {
                // Вставим из буфера обмена
                this.handleImage(blob)

                // Чистим буфер обмена, чтобы второй раз не вставлять, а могла сработать вставка из файла
                this.clearClipboardItems()

            } else {
                // Начинаем выбирать файл
                this.clickFileChoose()
            }
        },

        async onTextInputPaste(event) {
            let blob = await this.handleDataTransferItems(event.clipboardData)

            // Вставим из буфера обмена
            this.handleImage(blob)

            // Чистим буфер, чтобы второй раз не вставлять, а могла сработать вставка из файла
            this.clearClipboardItems()
        },

        clickFileChoose() {
            // Чистим файл, чтобы второй раз можно было выбрать тот же файл,
            // иначе:
            //  - неудобно при отладке,
            //  - событие @change не иницируется и тогда пользователю покажется,
            //    что нет никакой реакции
            this.clearChoosenFile()

            // Начинаем выбирать файл
            let elFileInput = this.$refs.fileInput
            elFileInput.click()
        },

        async onFileChoose(event) {
            let blob = await this.handleFileItems(event.target)

            if (blob) {
                this.handleImageBase64(blob[0])
            }
        },

        clearChoosenFile() {
            let elFileInput = this.$refs.fileInput
            elFileInput.value = null
        },

        handleImage(blob) {
            if (!blob) {
                return
            }

            //this.frameMode = "addByPhoto"

            // Найдено - покажем
            const img = document.createElement('img');
            img.src = URL.createObjectURL(blob);
            let elOutput = this.$refs.output
            elOutput.innerHTML = ''; // Clear previous content
            elOutput.appendChild(img);
        },

        handleImageBase64(text) {
            this.imageLoaded = text;
            this.frameMode = "addByPhoto"

            this.$refs.textInputPhoto.applyImage(text)

            /*
                        // Найдено - покажем
                        const img = document.createElement('img');
                        img.src = text
                        let elOutput = this.$refs.output
                        elOutput.innerHTML = ''; // Clear previous content
                        elOutput.appendChild(img);
            */
        },

        clearClipboardItems() {
            // Чистим буфер обмена
            navigator.clipboard.writeText("");
        },

        async handleClipboardItems() {
            let blob = null

            // Буфер обмена пуст?
            // В десктопной версии всега не null,
            // на мобилке == null когда буфер пуст (после перезагрузки)
            if (!navigator.clipboard) {
                return
            }

            // Запрос доступа к содержимому буфера обмена
            const clipboardItems = await navigator.clipboard.read();

            // Ищем изображение
            for (const clipboardItem of clipboardItems) {
                for (const type of clipboardItem.types) {
                    if (type.startsWith('image/')) {
                        blob = await clipboardItem.getType(type);
                        break
                    }
                }
            }

            //
            return blob
        },

        async handleDataTransferItems(dataTransfer) {
            let blob = null

            // Содержимое буфера обмена
            const dataTransferItems = dataTransfer.items;

            // Ищем изображение
            for (let i = 0; i < dataTransferItems.length; i++) {
                const dataItem = dataTransferItems[i]
                if (dataItem.type.startsWith('image/')) {
                    blob = await dataItem.getAsFile();
                    break;
                }
            }

            return blob
        },

        async handleFileItems(eventDataTarget) {
            let blob = []

            const files = eventDataTarget.files;

            for (const file of files) {
                const img = await this.readFileAsDataURL(file);
                //const imgElement = document.createElement('img');
                //imgElement.src = img;
                blob.push(img);
            }

            //
            if (blob.length === 0) {
                return null
            } else {
                return blob
            }
        },

        readFileAsDataURL(file) {
            return new Promise((resolve, reject) => {
                const reader = new FileReader();
                reader.onload = () => resolve(reader.result);
                reader.onerror = () => reject(reader.error);
                reader.readAsDataURL(file);
            });
        },

        returnFrameMode() {
            if (this.frameModePrior) {
                this.setFrameMode(this.frameModePrior)
                this.frameModePrior = null
            }
        },

        setFrameMode(frameMode) {
            if (frameMode !== this.frameMode) {

                // Смена только режим просмотра или еще и режима ввода
                if (this.isEditModeChanged(this.frameMode, frameMode)) {
                    // Фильтр очищаем, ведь ищем заново
                    this.filterText = ""

                    // Ранее загруженные слова очищаем, ведь ищем заново
                    this.itemsLoaded.length = 0
                    this.itemsOnChange()

                    // Сменился не только режим просмотра, но и режим ввода
                    this.editMode = frameMode

                    // Уведомим кому надо.
                    // Дочерние элементы почистят свои дополнительные данные.
                    // Например, ввод по фото - очистит расположение найденных слов на фото.
                    // Ввод по текту - очистит поисковое слово.
                    ctx.eventBus.emit('itemsCleared')

                    // Уведомим кому надо.
                    // Например, ввод по фото инициализирует камеру.
                    ctx.eventBus.emit('editModeChanged', frameMode)
                }


                // Если сейчас не просмотр - то вернемся потом в просмотр
                if (!this.isModeView()) {
                    this.frameModePrior = this.frameMode
                }

                //
                this.frameMode = frameMode


                // Поможем компоненту TextInputText поставить фокус
                let elTextInputText = this.$refs.textInputText
                elTextInputText.doFocus()
            }
        },

        isEditModeChanged(viewModeNow, viewModeNew) {
            if (viewModeNew.startsWith("add") && viewModeNow !== viewModeNew) {
                return true
            } else {
                return false
            }
        },

        btnNextClick() {
            this.frameMode = "viewItemsAdd"
        },

        async btnSaveClick() {
            if (this.immediateSaveMode) {
                return
            }

            // ---
            // Параметры для вызова api


            // Добавленные факты
            let stPlanFactAdd = []
            for (let item of this.itemsAdd) {
                let recPlanFact = {
                    factQuestion: item.factQuestion,
                    factAnswer: item.factAnswer,
                }
                stPlanFactAdd.push(recPlanFact)
            }

            // Удаленные факты
            let stPlanFactDel = []
            for (let item of this.itemsDel) {
                let recPlanFact = {
                    factQuestion: item.factQuestion,
                    factAnswer: item.factAnswer,
                }
                stPlanFactDel.push(recPlanFact)
            }

            // Скрытые и показанные факты
            let stPlanFactHideAddHideDel = []
            for (let item of this.itemsHideAdd) {
                let recPlanFact = {
                    isHidden: item.isHidden,
                    factQuestion: item.factQuestion,
                    factAnswer: item.factAnswer,
                }
                stPlanFactHideAddHideDel.push(recPlanFact)
            }
            for (let item of this.itemsHideDel) {
                let recPlanFact = {
                    isHidden: item.isHidden,
                    factQuestion: item.factQuestion,
                    factAnswer: item.factAnswer,
                }
                stPlanFactHideAddHideDel.push(recPlanFact)
            }


            // ---
            // Вызов api

            let planId
            if (!this.plan) {
                // Создаем новый план и добавляем все слова
                let recPlan = {text: this.planText}
                planId = await daoApi.invoke('m/Plan/ins', [recPlan, stPlanFactAdd, []])
            } else {
                planId = this.plan.id

                // Редактируем сам план
                if (this.canEditPlan()) {
                    let recPlan = {id: planId, text: this.planText}
                    await daoApi.invoke('m/Plan/upd', [recPlan])
                }

                // Редактируем состав плана
                if (this.canEditItemList()) {
                    // Добавляем слова в существующий план
                    await daoApi.invoke('m/Plan/addFact', [planId, stPlanFactAdd])

                    // Удаляем слова в существующем плане
                    await daoApi.invoke('m/Plan/delFact', [planId, stPlanFactDel])
                }
            }

            // Скрытые и показанные факты
            await ctx.gameplay.api_saveUsrFacts(stPlanFactHideAddHideDel, planId)


            // ---
            // Возврат, если есть куда

            if (this.frameReturn) {
                apx.showFrame({
                    frame: this.frameReturn,
                    props: this.frameReturnProps,
                })
                return
            }

            //
            apx.showFrame({
                frame: '/',
            })
        },

    },

    async mounted() {
        if (this.defaultMode) {
            this.frameMode = this.defaultMode
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


        // Проставим поле "isInPlan = true" - это нужно при редактировании
        for (let i = 0; i < this.itemsExternal.length; i++) {
            let task = this.itemsExternal[i]
            task.isInPlan = true
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