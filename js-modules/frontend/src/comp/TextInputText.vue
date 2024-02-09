<template>

    <div>
        <q-input
            dense outlined clearable
            ref="filterText"
            _style="max-width: 9em"
            v-model="filterText"
            placeholder="Поиск"
        >

            <template v-slot:append v-if="!filterText">
                <q-icon name="search"/>
            </template>

        </q-input>


        <TaskList
            :showEdit="true"
            :tasks="items"
            :itemsMenu="itemsMenu"
        />

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
        // Куда складывать выбранные Item
        itemsAdd: {type: Array, default: []},
    },

    data() {
        return {
            filterText: "",
            items: [],
            itemsMenu: [
                {
                    icon: "quasar.stepper.done",
                    itemMenuColor: this.itemDeleteMenuColor,
                    itemMenuClick: this.itemDeleteMenuClick,
                }
            ],
        }
    },

    methods: {

        itemDeleteMenuColor(taskItem) {
            let p = this.itemPosInItemsAdd(taskItem)
            if (p !== -1) {
                return "green-6"
            } else {
                return "grey-7"
            }
        },

        itemDeleteMenuClick(taskItem) {
            console.info("itemDeletedItemMenuClick", taskItem)
            console.info("this.itemsAdd", this.itemsAdd)

            let p = this.itemPosInItemsAdd(taskItem)
            if (p !== -1) {
                this.itemsAdd.splice(p, 1)
            } else {
                this.itemsAdd.push(taskItem)
            }
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
        },
    },

}

</script>

<style scoped>

</style>