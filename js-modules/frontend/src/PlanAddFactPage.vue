<template>
    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true">


        <div v-if="addMode==='text'">

            <TextInputText
                :itemsAdd="itemsAdd"
                :itemsHideAdd="itemsHideAdd"
                :itemsHideDel="itemsHideDel"
            >

            </TextInputText>

        </div>


        <div v-if="addMode==='photo'">

            <TextInputPhoto
                :itemsAdd="itemsAdd"
                :itemsHideAdd="itemsHideAdd"
                :itemsHideDel="itemsHideDel"
            >

            </TextInputPhoto>

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

        <div v-if="addMode==='viewItemsHide'">

            <q-input v-if="!planId" class="q-mb-sm"
                     dense outlined
                     label="Название плана"
                     v-model="plan.planText"
            />

            <TaskList
                :showEdit="true"
                :tasks="itemsHideAdd"
                :itemsMenu="itemsMenuHide"
            />

        </div>


        <template v-slot:footer>

            <q-footer>

                <q-toolbar class="bg-grey-1 text-black" style="min-height: 5em">


                    <div class="row " style="width: 100%">
                        <q-btn v-if="this.addMode !== 'text'"
                               class="q-ma-xs"
                               icon="quasar.editor.size"
                               @click="this.addMode='text'"
                        />
                        <q-btn v-if="this.addMode !== 'photo'"
                               class="q-ma-xs"
                               icon="image"
                               @click="this.addMode='photo'"
                        />

                        <div class="row" style="flex-grow: 10; align-content: end">
                            <div style="flex-grow: 10">
                                &nbsp;
                            </div>

                            <template v-if="itemsHideAdd.length > 0 || itemsHideDel.length > 0">

                                <div
                                    class="q-ma-sm"
                                    style="margin: auto;"
                                    @click="clickItemsHideText()">
                                    <span class="rgm-link-soft"> {{
                                            itemsHideText
                                        }}</span>
                                </div>

                            </template>

                            <template v-if="itemsAdd.length > 0">

                                <div
                                    class="q-ma-sm"
                                    style="margin: auto;"
                                    @click="clickItemStateText()">
                                    <span class="rgm-link-soft"> {{
                                            itemStateText
                                        }}</span>
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
                                        label="Далее"
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
import auth from "./auth"
import {apx} from "./vendor"
import utils from "./utils"

export default {
    name: "PlanAddFactPage",

    components: {
        MenuContainer, TextInputPhoto, TextInputText, TaskList
    },

    props: {
        planId: null,
        planText: null,
        frameReturn: null,
        frameReturnProps: null,
    },

    data() {
        return {
            addMode: "text",

            plan: {},
            itemsAdd: [],
            itemsHideAdd: [],
            itemsHideDel: [],

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
            if (this.addMode === "photo") {
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

        itemStateText() {
            return "Добавлено: " + this.itemsAdd.length
        },

        itemsHideText() {
            let s = ""

            if (this.itemsHideAdd.length > 0) {
                s = s + "скрыто: " + this.itemsHideAdd.length
            }

            if (this.itemsHideDel.length > 0) {
                if (s.length > 0) {
                    s = s + ", "
                }
                s = s + "показано: " + this.itemsHideDel.length
            }

            //
            s = s.charAt(0).toUpperCase() + s.slice(1);

            return s
        },

    },

    methods: {

        itemHideMenuIcon(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsHideAdd)
            if (p !== -1) {
                return "visibility-off"
            } else {
                return "visibility"
            }
        },

        itemHideMenuColor(isHidden) {
            if (isHidden) {
                return "red-6"
            } else {
                return "grey-7"
            }
        },

        itemHideMenuClick(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsHideAdd)
            if (p !== -1) {
                this.itemsHideAdd.splice(p, 1)
            } else {
                this.itemsHideAdd.push(taskItem)
            }
        },

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
                this.addMode = "text"
            }
        },

        clickItemStateText() {
            this.addMode = "viewItemsAdd"
        },

        clickItemsHideText() {
            this.addMode = "viewItemsHide"
        },

        btnNextClick() {
            this.addMode = "viewItemsAdd"
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

        // Для обеспечения возможности добавлять слова в еще не созданный план
        if (!this.planId) {
            let planText = apx.date.toDisplayStr(apx.date.today())
            this.plan = {planText: "Уровень " + planText}
        }
    },


}

</script>


<style scoped>

</style>