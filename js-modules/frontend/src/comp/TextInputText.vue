<template>

    <div>

        <div class="row q-mb-sm">

            <q-input
                dense outlined clearable
                ref="filterText"
                v-model="filterText"
                placeholder="Поиск"
            >

                <template v-slot:append v-if="!filterText">
                    <q-icon name="search"/>
                </template>

            </q-input>


            <slot name="toolbar"></slot>


        </div>

    </div>

</template>

<script>

import {daoApi} from "../dao"
import TaskList from "./TaskList"
import MenuContainer from "./MenuContainer"

export default {

    components: {
        MenuContainer, TaskList
    },

    props: {
        items: {type: Array, default: []},
        itemsOnChange: {type: Function},
    },

    data() {
        return {
            filterText: "",
        }
    },

    methods: {

        /*
                itemHideMenuIcon(taskItem) {
                    if (taskItem.isHidden) {
                        return "visibility-off"
                    } else {
                        return "visibility"
                    }
                },

                itemHideMenuColor(taskItem) {
                    if (taskItem.isHidden) {
                        return "grey-8"
                    } else {
                        return "grey-5"
                    }
                },

                itemHideMenuClick(taskItem) {
                    taskItem.isHidden = !taskItem.isHidden
                    //
                    if (taskItem.isHidden) {
                        taskItem.isKnownGood = false
                        taskItem.isKnownBad = false
                    }
                    if (taskItem.isHidden) {
                        this.hiddenCount = this.hiddenCount + 1
                    } else {
                        this.hiddenCount = this.hiddenCount - 1
                    }
                    //
                    ctx.gameplay.api_saveUsrFact(taskItem.factQuestion, taskItem.factAnswer, taskItem)

                    //
                    let posItemsAdd = utils.itemPosInItems(taskItem, this.itemsAdd)
                    let posItemsExtra = utils.itemPosInItems(taskItem, this.itemsExtra)
                    //
                    if (taskItem.isHidden) {
                        if (posItemsExtra === -1) {
                            this.itemsExtra.push(taskItem)
                        }
                        if (posItemsAdd !== -1) {
                            this.itemsAdd.splice(posItemsAdd, 1)
                        }
                    } else {
                        if (posItemsExtra !== -1) {
                            this.itemsExtra.splice(posItemsExtra, 1)
                        }
                        if (posItemsAdd === -1) {
                            //this.itemsAdd.push(taskItem)
                        }
                    }

                },

                itemDeleteMenuIcon(taskItem) {
                    let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                    if (p !== -1) {
                        return "quasar.stepper.done"
                    } else {
                        return "add"
                    }
                },

                itemDeleteMenuColor(taskItem) {
                    let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                    if (p !== -1) {
                        return "green-6"
                    } else {
                        return "grey-7"
                    }
                },

                itemDeleteMenuClick(taskItem) {
                    let p = utils.itemPosInItems(taskItem, this.itemsAdd)
                    if (p !== -1) {
                        this.itemsAdd.splice(p, 1)
                    } else {
                        this.itemsAdd.push(taskItem)
                    }
                },
        */


    },

    async mounted() {
        this.$refs.filterText.focus()
    },

    watch: {

        async filterText(filterText, v2) {
            // Установка daoApi.waitShow = false и оборачивание в try/finally - чтобы не дергался фокус
            let resApi
            try {
                daoApi.waitShow = false
                resApi = await daoApi.loadStore("m/Game/findItems", [filterText])
            } finally {
                daoApi.waitShow = true
            }

            // Заполним родительский список
            let items = resApi.records
            this.items.length = 0
            for (let item of items) {
                this.items.push(item)
            }

            // Уведомим родителя
            if (this.itemsOnChange) {
                this.itemsOnChange()
            }
        },

    },

}

</script>

<style scoped>

</style>