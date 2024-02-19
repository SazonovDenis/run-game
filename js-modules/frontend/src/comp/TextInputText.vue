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

            <q-toggle
                v-if="this.hiddenCount > 0"
                v-model="showHidden"
                :label="'Скрытые (' + this.hiddenCount + ')'"/>

        </div>

        <TaskList
            :showEdit="true"
            :tasks="items"
            :itemsMenu="itemsMenu"
            :filter="filter"
        />

    </div>

</template>

<script>

import {daoApi} from "../dao"
import TaskList from "./TaskList"
import MenuContainer from "./MenuContainer"
import utils from "../utils"
import ctx from "../gameplayCtx"

export default {

    components: {
        MenuContainer, TaskList
    },

    props: {
        // Куда складывать выбранные Item
        itemsAdd: {type: Array, default: []},

        // Куда складывать лишние Item
        itemsExtra: {type: Array, default: []},
    },

    data() {
        return {
            filterText: "",
            items: [],
            itemsMenu: [
                {
                    icon: this.itemHideMenuIcon,
                    color: this.itemHideMenuColor,
                    itemMenuClick: this.itemHideMenuClick,
                },
                {
                    icon: this.itemDeleteMenuIcon,
                    color: this.itemDeleteMenuColor,
                    itemMenuClick: this.itemDeleteMenuClick,
                },
            ],

            showHidden: false,
            hiddenCount: 0,
        }
    },

    methods: {

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

        filter(planTask) {
            if (planTask.isHidden && !this.showHidden) {
                return false
            }

            return true
        },

    },

    async mounted() {
        this.$refs.filterText.focus()
    },

    watch: {
        async filterText(filterText, v2) {
            let resApi
            // daoApi.waitShow = false - чтобы не дергался фокус
            try {
                daoApi.waitShow = false

                resApi = await daoApi.loadStore("m/Game/findItems", [filterText])

            } finally {
                daoApi.waitShow = true
            }

            //
            this.items = resApi.records
            /*
                        this.items = []
                        this.itemsExtra.length = 0
                        for (let item of resApi.records) {
                            if (item.isHidden) {
                                this.itemsExtra.push(item)
                            } else {
                                this.items.push(item)
                            }
                        }
            */

            this.hiddenCount = 0
            for (let item of this.items) {
                if (item.isHidden) {
                    this.hiddenCount = this.hiddenCount + 1
                }
            }

        },
    },

}

</script>

<style scoped>

</style>