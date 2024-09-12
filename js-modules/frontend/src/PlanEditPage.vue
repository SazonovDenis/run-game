<template>
    <MenuContainer
        :title="title"
        tabMenuName="PlanEditPage"
        :frameReturn="getFrameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true"
        :helpKey="getHelpKey()"
    >

        <!-- -->

        <input style="display: none;"
               type="file"
               accept="image/*"
               ref="fileInput"
               @change="onFileChoose"
        >

        <!-- -->

        <HelpPanel class="q-mt-none q-mb-xs" :helpKey="getHelpKey()"/>

        <!-- -->


        <!-- frameMode: editPlan -->

        <div v-if="frameMode==='editPlan'">

            <q-input class="q-ma-sm"
                     dense outlined
                     :disable="!canEditPlan()"
                     label="Название уровня"
                     v-model="planText"
            />


            <TaskListFilterBar
                class="q-my-sm"
                ed-hidden _ed-sort ed-filter
                v-model:filterText="filterText"
                v-model:sortField="viewSettings.sortField"
                v-model:showHidden="viewSettings.showHidden"
                :hiddenCount="hiddenCount"
                :visibleCount="visibleCount"
            />

            <TaskList
                v-if="(visibleCount > 0) || (viewSettings.showHidden && hiddenCount > 0)"
                :showLastItemPadding="true"
                :showEdit="true"
                :tasks="itemsExternal"
                :itemsMenu="itemsMenu_modeEdit"
                :filter="filterExt"
                :actionRightSlide="actionDelete"
                :actionLeftSlide="actionHide"
            />

            <div v-else-if="hiddenCount > 0"
                 class="q-pa-md rgm-state-text">
                Все слова в уровне вы знаете, поэтому они скрыты
            </div>

            <div v-else
                 class="q-pa-md rgm-state-text">
                В уровне нет ни одного слова
            </div>


        </div>


        <!-- frameMode: addByText -->

        <div v-show="canEditItemList() && frameMode==='addByText'">

            <div class="row" style="justify-content: end;">

                <TextInputText
                    ref="textInputText"
                    v-model:text="searchText"
                    :loading="searchTextLoading"
                    @paste="onTextInputPaste"
                />

                <!-- -->

                <template v-if="searchTextShouldLoad">

                    <TaskListFilterBar
                        class="q-my-sm"
                        :ed-hidden="isScreenWide()"
                        ed-tags
                        v-model:tags="viewSettings.filterTags"
                        v-model:showHidden="viewSettings.showHidden"
                        :hiddenCount="hiddenCountLoaded"
                        :onTagsChange="toggleKazXorEng"
                    />

                    <div
                        class="q-my-sm"
                        v-if="isScreenWide()">

                        <BtnAddAll
                            style="height: 3.1em;"
                            v-if="shouldShowAddAll()"
                            :disabled="canAddInPlanCount() === 0"
                            :count="canAddInPlanCount()"
                            @click="clickAddAll"
                        />

                    </div>

                </template>

                <!-- -->

                <template v-if="!isModeView()">

                    <div class="q-ml-sm q-mr-xs">

                        <q-icon
                            class="q-my-sm q-mr-sm q-pa-sm bg-grey-4 btn-keyboard btn-set-frame-mode"
                            name="picture"
                            size="2rem"
                            @click="clickImageBtn"
                        />

                        <q-icon
                            class="q-my-sm q-pa-sm bg-amber-5 btn-keyboard btn-set-frame-mode"
                            v-if="canEditItemList() && this.frameMode !== 'addByPhoto'"
                            name="camera"
                            size="2rem"
                            @click="this.setFrameMode('addByPhoto')"
                        />

                    </div>

                </template>

            </div>


            <!-- -->


            <div class="row q-mb-sm" style="justify-content: end; height: 3.1em;"
                 v-if="!isScreenWide() && (hiddenCountLoaded !== 0 || shouldShowAddAll())">

                <BtnHidden
                    v-if="hiddenCountLoaded !== 0"
                    class="q-mx-sm"
                    v-model:showHidden="viewSettings.showHidden"
                    :hiddenCount="hiddenCountLoaded"
                />

                <q-space/>

                <BtnAddAll
                    class="q-mr-sm"
                    v-if="shouldShowAddAll()"
                    :disabled="canAddInPlanCount() === 0"
                    :count="canAddInPlanCount()"
                    @click="clickAddAll"
                />

            </div>


            <!-- -->


            <template
                v-if="foundButHidden()">

                <div class="q-pa-md rgm-state-text">
                    Все найденные слова вы знаете, поэтому они скрыты
                </div>

            </template>


            <!-- -->


            <TaskList
                v-if="searchTextShouldLoad"
                :showEdit="true"
                :tasks="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filter"
                :actionLeftSlide="actionHide"
                :messageNoItems="searchTextLoading ? '' : taskListEmptyText"
            >

            </TaskList>

        </div>


        <!-- frameMode: addByPhoto -->

        <div v-show="canEditItemList() && frameMode==='addByPhoto'"
             class="btn-container">

            <div class="btn-container-top-right btn-container-on-top row">

                <q-icon
                    class="q-my-sm q-mx-xs q-pa-sm bg-grey-4 btn-keyboard btn-set-frame-mode"
                    name="picture"
                    size="2rem"
                    @click="clickImageBtn"
                />

                <q-icon
                    class="q-my-sm q-mx-xs q-pa-sm bg-amber-5 btn-keyboard btn-set-frame-mode"
                    v-if="canEditItemList() && this.frameMode !== 'addByText'"
                    name="keyboard"
                    size="2rem"
                    @click="this.setFrameMode('addByText')"
                />

            </div>


            <!-- -->


            <TextInputPhoto
                ref="textInputPhoto"
                :planId="planId"
                :items="itemsLoaded"
                :itemsMenu="itemsMenu_modeAddFact"
                @itemsChange="itemsOnChange"
                @itemClick="itemOnClick"
                @fileChoose="clickFileChoose"
            />

        </div>


        <!-- frameMode: другие -->


        <div v-if="frameMode==='viewItem'">

            <ItemInfo
                :itemMenu="itemsMenu_modeViewItemInfo"
                :item="itemLoadedItem"
                :tasks="itemLoadedTask"
            />

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

        <q-page-sticky
            position="bottom-right"
            :offset="[10, 10]">

            <BtnsEditAdd v-if="!isModeView() && isPlanExisting()"
                         :showEdit="canEditPlan() && this.frameMode !== 'editPlan'"
                         :showAdd="canEditPlan() && this.frameMode !== 'addByText' && this.frameMode !== 'addByPhoto'"
                         @onPlanEdit="this.setFrameMode('editPlan')"
                         @onPlanAddFact="this.setFrameMode('addByText')"
            />

        </q-page-sticky>


        <template v-slot:footer>

            <q-footer v-if="wasChanges()">

                <q-toolbar class="bg-grey-1 text-black" style="min-height: 4em">


                    <div class="row" style="width: 100%">

                        <div class="row" style="flex-grow: 10; align-content: end">

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

                            <q-space/>

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
import {apx} from "./vendor"
import utils from "./utils"
import ctx from "./gameplayCtx"
import dbConst from "./dao/dbConst"
import MenuContainer from "./comp/MenuContainer"
import HelpPanel from "./comp/HelpPanel"
import TextInputPhoto from "./comp/TextInputPhoto"
import TextInputText from "./comp/TextInputText"
import TaskListFilterBar from "./comp/TaskListFilterBar"
import TaskList from "./comp/TaskList"
import BtnsEditAdd from "./comp/BtnsEditAdd"
import BtnHidden from "./comp/filter/BtnHidden"
import BtnAddAll from "./comp/BtnAddAll"
import ItemInfo from "./comp/ItemInfo"


export default {

    components: {
        MenuContainer, HelpPanel, TaskListFilterBar, BtnHidden, BtnAddAll,
        TextInputPhoto, TextInputText, BtnsEditAdd, TaskList, ItemInfo,
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
        let actionItemLoadFull = {
            outline: false,
            icon: "external-link",
            color: "red-10",
            hidden: this.itemLoadFullMenuHidden,
            onClick: this.itemLoadFull,
        };

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

            itemLoadedItem: {},
            itemLoadedTask: [],

            //
            imageLoaded: null,

            //
            itemsAdd: [],
            itemsDel: [],
            itemsHideAdd: [],
            itemsHideDel: [],

            visibleCount: 0,
            hiddenCount: 0,
            hiddenCountLoaded: 0,
            notInPlanCountLoaded: 0,

            searchText: "",
            searchTextLoading: false,

            filterText: "",
            viewSettings: {
                sortField: "ratingAsc",
                filterTags: {},
                showHidden: false,
            },

            actionItemLoadFull: actionItemLoadFull,
            actionHide: actionHide,
            actionDelete: actionDelete,

            itemsMenu_modeAddFact: [
                actionItemLoadFull,
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
                actionItemLoadFull,
                actionHide,
                actionDelete,
            ],

            itemsMenu_modeViewItemInfo: [
                actionHide,
                actionDelete,
            ],

            itemsMenu_modeViewItemsDel: [
                actionItemLoadFull,
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
                actionItemLoadFull,
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
        },

        viewSettings: {
            handler() {
                // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
                if (this.settingsPreventWatch) {
                    this.settingsPreventWatch = false
                    return
                }

                //
                let globalViewSettings = ctx.getGlobalState().viewSettings
                globalViewSettings.findItems = this.viewSettings
                ctx.eventBus.emit("change:settings")
            },
            deep: true
        },

        async searchText(searchText, searchTextPrior) {
            this.load(searchText)
        },

    },


    computed: {

        searchTextShouldLoad() {
            return this.searchText && this.searchText.length >= 2
        },

        planId() {
            if (this.isPlanExisting()) {
                return this.plan.id
            } else {
                return 0
            }
        },

        taskListEmptyText() {
            let selectedLang
            for (let langKey of utils.Langs_keys) {
                if (this.viewSettings.filterTags[langKey] === true) {
                    selectedLang = utils.Langs_text_tvor[langKey]
                }
            }
            if (selectedLang) {
                return "В словаре <span class='rgm-state-text-lang'>" + selectedLang + "</span> языка слова не найдены"
            } else {
                return "Слова не найдены"
            }
        },

        title() {
            if (this.isModeView()) {
                if (this.frameMode === "viewItem") {
                    return "Слово"
                } else if (this.frameMode === "viewItemsHideAdd") {
                    return "Известные слова"
                } else if (this.frameMode === "viewItemsHideDel") {
                    return "Показанные слова"
                } else if (this.frameMode === "viewItemsDel") {
                    return "Удаленные слова"
                } else if (this.frameMode === "viewItemsAdd") {
                    if (this.isPlanExisting()) {
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
                return this.execFrameReturn
            } else {
                return this.frameReturn
            }
        },


        btnSaveTitle() {
            if (this.isPlanExisting()) {
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

        async loadItem(itemId, filterTags) {
            let resApi = await daoApi.loadStore("m/Item/loadItem", [itemId, this.planId, filterTags], {})

            //
            this.itemLoadedItem = resApi.item.records[0]
            this.itemLoadedTask = resApi.tasks.records

            //
            this.setFrameMode("viewItem")
        },

        async load(searchText, filterTags) {
            // Новый поиск
            let items = []

            //
            if (this.searchTextShouldLoad) {
                //
                if (!filterTags) {
                    filterTags = this.viewSettings.filterTags
                } else {
                    filterTags = Object.assign({}, this.viewSettings.filterTags)
                    filterTags["dictionary"] = "full"
                }

                //
                let resApi = await daoApi.loadStore("m/Item/findItems", [searchText, this.planId, filterTags], {
                    waitShow: false,
                    onRequestState: (requestState) => {
                        if (requestState === "start") {
                            this.searchTextLoading = true
                        } else {
                            this.searchTextLoading = false
                        }
                    }
                })

                // Нашли
                items = resApi.records

                // Внешние условия поменялись - отбросим результаты.
                // Это важно, если пользователь печатает чаще, чем приходит ответ от сервера,
                // (параметр debounce меньше, чем время ответа сервера).
                // Тогда сюда приходят УСТАРЕВШИЕ результаты, которые надо отбросить в надежде на то,
                // что продолжающийся ввод пользователя иницирует запрос к сервру с АКТУАЛЬНЫМИ параметрами.
                if (searchText !== this.searchText) {
                    return
                }
            }

            // Заполним список
            this.itemsLoaded = items
        },

        isScreenWide() {
            return window.innerWidth > 700
        },

        shouldShowAddAll() {
            if ((this.searchText?.split(" ").length > 5) && (this.itemsLoaded.length > 5)) {
                return true
            }

            return false
        },

        canAddInPlanCount() {
            return this.notInPlanCountLoaded - this.hiddenCountLoaded
        },

        foundButHidden() {
            return !this.viewSettings.showHidden &&
                this.itemsLoaded.length > 0 &&
                this.itemsLoaded.length === this.hiddenCountLoaded
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

            //this.$refs.textInputText.load(this.$refs.textInputText.filterText)
            this.load(this.searchText)
        },

        filter(item) {
            return this.machTags(item) && this.filterLoaded(item)
        },

        machTags(item) {
            // Фильтр по тэгам не задан
            let filterTagsCount = Object.keys(this.viewSettings.filterTags).length
            if (!this.viewSettings.filterTags || filterTagsCount === 0) {
                return true
            }


            // Чтобы не было ошибок, если у плана нет тэгов
            let planTags = item.tags
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
            return true
        },

        getHelpKey() {
            if (!this.plan) {
                if (this.itemsAdd.length === 0) {
                    return "help.mainPage.createPlan"
                }

                return "help.mainPage.createPlanNext"

            } else if (this.frameMode === "addByText") {
                if (this.itemsLoaded.length === 0) {
                    return "help.mainPage"
                }

                if (this.foundButHidden()) {
                    return "help.mainPage.foundHidden"
                }

                return "help.mainPage.found"

            } else if (this.frameMode === "editPlan") {
                return "help.mainPage.editPlan"

            } else if (this.frameMode === "addByPhoto") {
                if (this.isCameraCapturing()) {
                    return ["help.mainPage.addByPhoto.shot", "help.mainPage.addByPhoto.return"]
                } else {
                    return ["help.mainPage.addByPhoto.return"]
                }

            } else {
                return null
            }

        },

        isModeView() {
            return this.frameMode.startsWith("viewItem")
        },

        isMobile() {
            return Jc.cfg.is.mobile
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

        isPlanExisting() {
            if (this.plan) {
                return true
            } else {
                return false
            }
        },

        canEditHidden() {
            return this.doEditHidden
        },

        canEditItemList() {
            return this.doEditItemList && (!this.plan || this.plan.isOwner === true)
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
            //
            this.notInPlanCountLoaded = 0
            for (let item of this.itemsLoaded) {
                if (!item.isInPlan) {
                    this.notInPlanCountLoaded = this.notInPlanCountLoaded + 1
                }
            }
        },

        checkShowHiddenMode() {
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

        itemsOnChange() {
            // Ранее сформированные списки с разницей очищаем, ведь ищем заново
            if (this.immediateSaveMode) {
                this.itemsAdd = []
                this.itemsDel = []
                this.itemsHideAdd = []
                this.itemsHideDel = []
            }

            // Удобнее держать отдельную переменную this.hiddenCount
            this.calcHiddenCountLoaded()
            this.calcHiddenCountExternal()
            this.checkShowHiddenMode()
        },

        filterLoaded(item) {
            if (!this.viewSettings.showHidden && item.isHidden) {
                return false
            }

            return true
        },

        filterExt(taskItem) {
            if (taskItem.isHidden && !this.viewSettings.showHidden) {
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
                return undefined
            }
        },

        itemHideMenuColor(taskItem) {
            if (taskItem.isHidden) {
                return "indigo-2"
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

        itemLoadFull(item) {
            this.loadItem(item.item, this.viewSettings.filterTags)
        },

        itemLoadFullMenuHidden(item) {
            return !item.tag.hasDictionaryFull
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
            if (this.itemIsInPlan(taskItem)) {
                return "grey-8"
            } else {
                return "green-7"
            }
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
            if (this.itemIsInPlan(taskItem)) {
                return "grey-8"
            } else {
                return "green-7"
            }
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

        clickAddAll() {
            let itemsTmp = []
            for (let taskItem of this.itemsLoaded) {
                if (!taskItem.isHidden) {
                    itemsTmp.push(taskItem)
                }
            }
            this.itemsAddItems(itemsTmp)
        },


        async clickImageBtn() {
            // Начинаем выбирать файл
            this.clickFileChoose()
        },

        async onTextInputPaste(event) {
            // Отдадим приоритет простому тексту.
            // Это важно, т.к. некоторые программы копируют в буфер сразу в нескольких форматах.
            // Например, при вставке ячееек, скопированных из LibreOffice, в буфер попадает
            // не только чистый текст, но также и картинка и форматированный текст.
            if (this.isDataTransferContainsText(event.clipboardData)) {
                return
            }

            // Читаем буфер обмена и ищем избражение
            let blob = await this.handleDataTransferItems(event.clipboardData)

            // Найдено избражение?
            if (blob) {
                // Вставим изображение из буфера обмена
                this.handleImage(blob)
            }
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
            // Читаем данные события и читаем файл
            let blob = await this.handleFileItems(event.target)

            // Файл прочитан?
            if (blob) {
                // Вставим изображение из файла
                this.handleImageBase64(blob[0])
            }
        },

        clearChoosenFile() {
            let elFileInput = this.$refs.fileInput
            elFileInput.value = null
        },

        async handleImage(blob) {
            // Преобразуем в формат dataUrl
            const blobDataUrl = await this.readFileAsDataURL(blob);

            // Берем содержимое буфера обмена
            this.handleImageBase64(blobDataUrl)
        },

        handleImageBase64(imageData) {
            this.imageLoaded = imageData;
            this.frameMode = "addByPhoto"

            //
            this.$refs.textInputPhoto.applyImage(imageData)
        },

        isCameraCapturing() {
            return this.$refs.textInputPhoto.isCameraCapturing
        },

        /**
         * Очищает буфер обмена
         */
        clearClipboardItems() {
            navigator.clipboard.writeText("");
        },

        /**
         * Просматривает содержимое буфера обмена
         * @returns Изображение, если оно есть
         */
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

        async handleDataTransferItems(clipboardData) {
            let blob = null

            // Содержимое буфера обмена
            const dataTransferItems = clipboardData.items;

            // Ищем изображение
            for (let i = 0; i < dataTransferItems.length; i++) {
                const dataItem = dataTransferItems[i]
                if (dataItem.type.startsWith("image/")) {
                    blob = await dataItem.getAsFile();
                    break;
                }
            }

            return blob
        },

        isDataTransferContainsText(clipboardData) {
            // Содержимое буфера обмена
            const dataTransferItems = clipboardData.items;

            // Ищем простой текст
            for (let i = 0; i < dataTransferItems.length; i++) {
                const dataItem = dataTransferItems[i]
                if (dataItem.type.startsWith("text/plain")) {
                    return true;
                }
            }

            return false
        },

        async handleFileItems(eventDataTarget) {
            let blob = []

            const files = eventDataTarget.files;

            for (const file of files) {
                const img = await this.readFileAsDataURL(file);
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

        execFrameReturn() {
            if (this.frameMode === "viewItem") {
                this.setFrameMode(this.frameModePrior)
                this.frameModePrior = null
            } else if (this.frameModePrior) {
                this.setFrameMode(this.frameModePrior)
                this.frameModePrior = null
            }
        },

        setFrameMode(frameMode) {
            if (frameMode !== this.frameMode) {

                // Смена только режим просмотра или еще и режима ввода
                if (this.isEditModeChanged(this.frameMode, frameMode)) {
                    // Фильтр очищаем, ведь ищем заново
                    this.searchText = ""

                    // Ранее загруженные слова очищаем, ведь ищем заново
                    this.itemsLoaded.length = 0
                    this.itemsOnChange()

                    // Сменился не только режим просмотра, но и режим ввода
                    this.editMode = frameMode

                    // Уведомим кому надо.
                    // Дочерние элементы почистят свои дополнительные данные.
                    // Например, ввод по фото - очистит расположение найденных слов на фото.
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
            if (viewModeNow === "viewItem") {
                return false
            } else if (viewModeNew.startsWith("add") && viewModeNow !== viewModeNew) {
                return true
            } else {
                return false
            }
        },

        btnNextClick() {
            this.setFrameMode("viewItemsAdd")
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

    mounted() {
        if (this.defaultMode) {
            this.frameMode = this.defaultMode
        }

        //
        if (this.planItems) {
            this.itemsExternal = this.planItems
        }

        //
        if (this.isPlanExisting()) {
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


        // Настройки фильрации
        let globalViewSettings = ctx.getGlobalState().viewSettings
        if (globalViewSettings.findItems) {
            this.viewSettings = globalViewSettings.findItems
            // Это помогает не срабатывать сразу после первичной загрузки данных в mounted
            this.settingsPreventWatch = true
        }
    },


}

</script>


<style scoped>

.items-count-info {
    margin-top: auto;
    margin-bottom: auto;
}

.btn-container {
    position: relative;
}

.btn-container-top-right {
    right: 0;
    top: 0;
    position: absolute;
}

.btn-container-top-left {
    letf: 0;
    top: 0;
    position: absolute;
}

.btn-container-bottom-right {
    right: 0.5rem;
    bottom: 0.5rem;
    position: fixed;
}

.btn-container-on-top {
    opacity: 0.8;
    z-index: 5000;
}

.btn-keyboard {
    border-radius: 20.5rem;
}

.btn-set-frame-mode {
    _z-index: 5000;
}

</style>