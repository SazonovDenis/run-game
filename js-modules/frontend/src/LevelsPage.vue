<template>
    <div>
        <q-btn v-for="plan in plans"
               color="white" text-color="black"
               v-bind:label="plan.text" @click="click(plan.id)"/>
    </div>
</template>

<script>

import {apx} from './vendor'
import {daoApi} from "./dao"
import gameplay from "./gameplay"

export default {

    extends: apx.JcFrame,

    components: {},

    data() {
        return {
            plans: []
        }
    },

    created() {
    },

    methods: {

        click(idPlan) {
            apx.showFrame({
                frame: '/', props: {}
            })

            gameplay.gameStart(idPlan)
        },

        async loadLevels() {
            let res = await daoApi.loadStore(
                "m/Game/getPlans", {}
            )

            console.info("m/Game/getPlans", res)

            this.plans = res.records
            /*
                        this.plans = [
                            {id: 1000, text: "Level 1"},
                            {id: 1001, text: "Level 2"},
                            {id: 1002, text: "Level 45"},
                        ]
            */
        },

    },

    mounted() {
        this.loadLevels()
    },


}
</script>
