<template>
    <MenuContainer
        :title="planText"
        :frameReturn="frameReturn"
        :frameReturnProps="frameReturnProps"
        :showFooter="true">

        <q-layout view="hHh lpR fFf">

            <q-page-container>

                <div v-if="addMode==='text'">

                    <TextInputText :itemsAdd="itemsAdd">

                    </TextInputText>

                </div>


                <div v-if="addMode==='photo'">

                    <TextInputPhoto :itemsAdd="itemsAdd">

                    </TextInputPhoto>

                </div>

                <div v-if="addMode==='viewItemsAdd'">

                    <TaskList
                        :showEdit="true"
                        :tasks="itemsAdd"
                        :itemsMenu="itemsMenu"
                    />

                </div>

            </q-page-container>

        </q-layout>


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

                                <div
                                    class="q-ma-sm"
                                    style="margin: auto; _padding-left: .2em"
                                    @click="clickItemStateText">
                                    <span class="rgm-link-soft"> {{
                                            itemStateText
                                        }}</span>
                                </div>

                                <q-btn
                                    no-caps
                                    class="q-ma-sm"
                                    label="Сохранить"
                                    @click="saveItems()"
                                />

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

export default {
    name: "AddFactPage",

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
            itemsAdd: [],
            itemsMenu: [
                {
                    icon: "clear",
                    itemMenuClick: this.itemDeleteMenuClick,
                }
            ]
        }
    },

    computed: {
        itemStateText() {
            return "Добавлено: " + this.itemsAdd.length
        }
    },

    methods: {

        itemDeleteMenuClick(taskItem) {
            let p = this.itemPosInItemsAdd(taskItem)
            if (p !== -1) {
                this.itemsAdd.splice(p, 1)
            }
        },

        clickItemStateText() {
            this.addMode = "viewItemsAdd"
        },

        saveItems() {
            let res = daoApi.loadStore('m/Game/addItems', [this.itemsAdd])

        },

        itemPosInItemsAdd(taskItem) {
            for (let p = 0; p < this.itemsAdd.length; p++) {
                let itemAdd = this.itemsAdd[p]
                if (itemAdd.factAnswer === taskItem.factAnswer &&
                    itemAdd.factQuestion === taskItem.factQuestion
                ) {
                    return p
                }
            }

            return -1
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
    },


}

</script>


<style scoped>

</style>