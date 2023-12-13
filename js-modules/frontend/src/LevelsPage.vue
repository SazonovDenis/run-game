<template>
    <div>

        <div v-for="plan in plans"
             class="game-plan-item"
             @click="click(plan.id)">
            {{ plan.text }} ({{ plan.cnt }})
            <div class="game-plan-item-progress">
                <div class="game-plan-done"
                     :style="{flexGrow: plan.cntDone}">
                    {{ plan.cntDone }}
                </div>
                <div class="game-plan-inprogress"
                     :style="{flexGrow: plan.cntInProgress}">
                    {{ plan.cntInProgress }}
                </div>
                <div class="game-plan-todo"
                     :style="{flexGrow: plan.cntToDo}">
                    {{ plan.cntToDo }}
                </div>
            </div>
        </div>

    </div>
</template>

<script>

import {apx} from './vendor'
import {daoApi} from "./dao"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"

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

            this.plans = res.records
        },

    },

    mounted() {
        if (!ctx.globalState.user || !(ctx.globalState.user.id > 0)) {
            apx.showFrame({frame: import("./HomePage")})
            return
        }

        //
        this.loadLevels()
    },


}
</script>

<style>

.game-plan-item {
    margin: 0.5em 1em;
    padding: 0.5em 1em 0.8em 1em;

    border: 1px solid #e0e0e0;
    border-radius: 0.5rem;

    font-size: 120%;
    color: #2d2d2d;
}

.game-plan-item-progress {
    display: flex;
    flex-direction: row;
}

.game-plan-item-progress div {
    margin: 0.1em;
    padding: 0.5em;
}

.game-plan-done {
    background-color: #e6ffda;
}

.game-plan-inprogress {
    background-color: #fff2c8;
}

.game-plan-todo {
    background-color: #e0e0e0;
}


</style>