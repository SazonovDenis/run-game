<template>
    <MenuContainer
        :title="title"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true">


        <div v-if="addMode==='text'">

            <TextInputText :itemsAdd="itemsAdd">

            </TextInputText>

        </div>


        <div v-if="addMode==='photo'">

            <TextInputPhoto :itemsAdd="itemsAdd">

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
                :itemsMenu="itemsMenu"
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

                            <template v-if="itemsAdd.length > 0">

                                <div _v-if="addMode !== 'viewItemsAdd'"
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
            itemsMenu: [
                {
                    icon: "clear",
                    itemMenuClick: this.itemDeleteMenuClick,
                }
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
            return "Добавлено слов: " + this.itemsAdd.length
        }

    },

    methods: {

        itemDeleteMenuClick(taskItem) {
            let p = utils.itemPosInItems(taskItem, this.itemsAdd)
            if (p !== -1) {
                this.itemsAdd.splice(p, 1)
            }
        },

        clickItemStateText() {
            this.addMode = "viewItemsAdd"
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