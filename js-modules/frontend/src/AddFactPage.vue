<template>
    <MenuContainer title="Добавление слова">
        {{ this.idPlan }}

        <q-input
            style="max-width: 9em"
            dense outlined clearable
            v-model="filterText"
            placeholder="Поиск"
        >

            <template v-slot:append v-if="!filterText">
                <q-icon name="search"/>
            </template>

        </q-input>


        <TaskList
            :showEdit="true"
            :tasks="items"/>


    </MenuContainer>
</template>

<script>

import {daoApi} from "./dao"
import MenuContainer from "./comp/MenuContainer"
import TaskList from "./comp/TaskList"

export default {
    name: "AddFactPage",

    components: {
        MenuContainer, TaskList
    },

    props: {
        idPlan: null,
    },

    data() {
        return {
            filterText: "",
            items: [],
        }
    },

    watch: {
        async filterText(v1, v2) {
            console.info("filterText: ", v1, v2)
            let resApi = await daoApi.loadStore(
                "m/Game/findItems", [v1]
            )

            //
            this.items = resApi.records

            //
            console.info(this.items)

        },
    },

    methods: {},

}

</script>


<style scoped>

</style>