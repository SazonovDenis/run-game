<template>
    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true">


        <div v-if="addMode==='editPlan'">

            <q-input class="q-mb-sm"
                     dense outlined
                     label="Название плана"
                     v-model="plan.planText"
            />

            <TaskListFilterBar
                v-model:filterText="filterText"
                v-model:sortField="sortField"
                v-model:showHidden="showHidden"
            />

            <TaskList
                v-if="itemsExt.length > 0"
                :showEdit="true"
                :tasks="itemsExt"
                :itemsMenu="itemsMenu_modeEdit"
                :filter="filter"
            >

            </TaskList>

        </div>


        <div v-if="addMode==='text'">

            <TextInputText
                :items="items"
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
                v-if="items.length > 0"
                :showEdit="true"
                :tasks="items"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filter"
            >

            </TaskList>

        </div>


        <div v-if="addMode==='photo'">

            <TextInputPhoto
                :items="items"
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
                v-if="items.length > 0"
                :showEdit="true"
                :tasks="items"
                :itemsMenu="itemsMenu_modeAddFact"
                :filter="filter"
            />

        </div>


        <div v-if="addMode==='viewItemsAdd'">

            <q-input v-if="!planId" class="q-mb-sm"
                     dense outlined
                     label="Название плана"
                     v-model="plan.planText"
            />

            <TaskList
                :showEdit="true"
                :tasks="itemsAdd"
                :itemsMenu="itemsMenuAdd"
            />

        </div>

        <div v-if="addMode==='viewItemsDel'">

            <TaskList
                :showEdit="true"
                :tasks="itemsDel"
                :itemsMenu="itemsMenu_modeEdit"
            />

        </div>

        <div v-if="addMode==='viewItemsHideAdd'">

            <TaskList
                :showEdit="true"
                :tasks="itemsHideAdd"
                :itemsMenu="itemsMenuHide"
            />

        </div>


        <div v-if="addMode==='viewItemsHideDel'">

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

                            <q-btn v-if="this.addMode !== 'editPlan'"
                                   round
                                   color="purple-4"
                                   class="q-ma-xs"
                                   icon="edit"
                                   size="1.2em"
                                   @click="this.setAddMode('editPlan')"
                            />

                            <q-btn v-if="this.addMode !== 'text'"
                                   round
                                   color="yellow-10"
                                   class="q-ma-xs"
                                   size="1.2em"
                                   icon="word-add-keyboard"
                                   @click="this.setAddMode('text')"
                            />
                            <q-btn v-if="this.addMode !== 'photo'"
                                   round
                                   color="yellow-9"
                                   class="q-ma-xs"
                                   size="1.2em"
                                   icon="word-add-photo"
                                   @click="this.setAddMode('photo')"
                            />
                        </div>

                        <div class="row" style="flex-grow: 10; align-content: end">
                            <div style="flex-grow: 10">
                                &nbsp;
                            </div>

                            <template v-if="itemsHideAdd.length > 0">

                                <div
                                    class="q-ma-xs items-count-info"
                                    @click="clickItemsHideAddText()">
                                    <span class="rgm-link-soft">
                                        {{ itemsHideAddText }}
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

                            <template v-if="itemsHideDel.length > 0">

                                <div
                                    class="q-ma-xs items-count-info"
                                    @click="clickItemsHideDelText()">
                                    <span class="rgm-link-soft">
                                        {{ itemsHideDelText }}
                                    </span>
                                </div>

                            </template>

                            <template v-if="itemsAdd.length > 0">

                                <div
                                    class="q-ma-xs items-count-info"
                                    @click="clickItemsAddText()">
                                    <span class="rgm-link-soft">
                                        {{ itemsAddText }}
                                    </span>
                                </div>


                                <template v-if="addMode === 'viewItemsAdd'">

                                    <q-btn
                                        no-caps
                                        class="q-ma-sm"
                                        :label="btnSaveTitle"
                                        @click="btnSaveClick()"
                                    />

                                </template>

                                <template v-else>

                                    <q-btn
                                        no-caps
                                        class="q-ma-sm"
                                        label="Готово"
                                        @click="btnNextClick()"
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
        planId: null,
        planText: null,

        tasks: null,
        defaultAddMode: null,

        frameReturn: null,
        frameReturnProps: null,
    },

    data() {
        return {
            addMode: "text",

            plan: {},
            items: [],
            itemsExt: [],

            itemsAdd: [],
            itemsDel: [],
            itemsHideAdd: [],
            itemsHideDel: [],

            hiddenCount: 0,

            filterText: "",
            sortField: "",
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

    computed: {

        title() {
            if (this.addMode === "editPlan") {
                return "Редактирование плана"
            } else if (this.addMode === "photo") {
                return "Добавление слов"
            } else if (this.addMode === "text") {
                return "Добавление слов"
            } else {
                return "Сохранение"
            }
        },

        btnSaveTitle() {
            if (this.planId) {
                return "Сохранить"
            } else {
                return "Создать план"
            }
        },

        itemsAddText() {
            return "Добавлено: " + this.itemsAdd.length
        },

        itemsDelText() {
            if (this.itemsDel.length > 0) {
                return "Удалено: " + this.itemsDel.length
            } else {
                return ""
            }
        },

        itemsHideAddText() {
            let s = ""

            if (this.itemsHideAdd.length > 0) {
                s = "Скрыто: " + this.itemsHideAdd.length
            }

            return s
        },

        itemsHideDelText() {
            let s = ""

            if (this.itemsHideDel.length > 0) {
                s = s + "Показано: " + this.itemsHideDel.length
            }

            return s
        },

    },

    methods: {

        itemsOnChange() {
            //console.info("itemsOnChange")

            // Удобнее держать отдельную переменную this.hiddenCount
            this.hiddenCount = 0
            for (let item of this.items) {
                if (item.isHidden) {
                    this.hiddenCount = this.hiddenCount + 1
                }
            }

            // Для новой порции слов учтем, какие мы только что скрыли и добавили
            for (let item of this.items) {
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
            if (!this.showHidden && taskItem.isHidden) {
                return false
            }

            return true
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
            ctx.gameplay.api_saveUsrFact(taskItem.factQuestion, taskItem.factAnswer, taskItem)


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
            if (this.itemsHideAdd.length === 0 && this.addMode === "viewItemsHideAdd") {
                this.setAddMode("text")
            }
            //
            if (this.itemsHideDel.length === 0 && this.addMode === "viewItemsHideDel") {
                this.setAddMode("text")
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
            let p = utils.itemPosInItems(taskItem, this.itemsDel)
            if (p !== -1) {
                this.itemsDel.splice(p, 1)
            } else {
                this.itemsDel.push(taskItem)
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
                this.setAddMode("text")
            }
        },

        /* -------------------------------- */

        clickItemsAddText() {
            this.setAddMode("viewItemsAdd")
        },

        clickItemsDelText() {
            this.setAddMode("viewItemsDel")
        },

        clickItemsHideAddText() {
            this.setAddMode("viewItemsHideAdd")
        },

        clickItemsHideDelText() {
            this.setAddMode("viewItemsHideDel")
        },

        /*
                btnNextClick() {
                    this.addMode = "viewItemsAdd"
                },
        */

        setAddMode(addMode) {
            if (addMode !== this.addMode) {
                this.items.length = 0
                this.itemsOnChange()
            }
            this.addMode = addMode
        },

        async btnSaveClick() {
            //
            let stPlanFact = []
            for (let item of this.itemsAdd) {
                stPlanFact.push({
                    factAnswer: item.factAnswer,
                    factQuestion: item.factQuestion,
                })
            }

            //
            let planId = this.planId
            if (!planId) {
                // Создаем новый план и добавляем слова
                let recPlan = {text: this.plan.planText}
                planId = await daoApi.invoke('m/Plan/ins', [recPlan, stPlanFact, []])
            } else {
                // Добавляем слова в существующий план
                await daoApi.invoke('m/Plan/addFact', [planId, stPlanFact])
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
        if (this.defaultAddMode){
            this.addMode = this.defaultAddMode
        }

        // Для обеспечения возможности добавлять слова в еще не созданный план
        if (!this.planId) {
            let planText = apx.date.toDisplayStr(apx.date.today())
            this.plan = {planText: "Уровень " + planText}
        }

        //
        if (this.tasks) {
            this.itemsExt = this.tasks
        }
    },


}

</script>


<style scoped>

.items-count-info {
    margin: auto;
}

</style>