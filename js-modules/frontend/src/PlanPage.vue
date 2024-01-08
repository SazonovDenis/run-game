<template>

    <MenuContainer title="Уровень">

        <PlanInfo :planText="plan.planText"
                  :rating="statistic.rating"
                  :ratingQuickness="statistic.ratingQuickness"
                  :ratingMax="statistic.ratingMax"
        />

        <q-separator/>

        <jc-btn kind="primary"
                label="Играть уровень"
                style="min-width: 10em;"
                @click="gameStart()">
        </jc-btn>

        <q-separator/>

        <div v-for="planTask in planTasks" class="plan-tasks">

            <div class="row">

                <GameTask :task="planTask"/>

                <div class="col" style="max-width: 5em">
                    {{ planTask.rating }}
                </div>

            </div>

        </div>

    </MenuContainer>

</template>


<script>

import {apx} from "./vendor"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"
import GameTask from "./comp/GameTask"
import PlanInfo from "./comp/PlanInfo"
/*
import GameInfo from "./comp/GameInfo"
*/

export default {
    name: "PlanPage",

    props: {
        idPlan: null,
    },

    components: {
        MenuContainer, GameTask, PlanInfo, /*GameInfo*/
    },

    computed: {},

    // Состояние игрового мира
    data() {
        return {
            plan: {},
            planTasks: {},
            statistic: {},
            globalState: ctx.getGlobalState(),
        }
    },

    methods: {

        isAuth() {
            return auth.isAuth()
        },

        async gameStart() {
            await gameplay.gameStart(this.idPlan)

            apx.showFrame({
                frame: '/game', props: {}
            })
        },

        async taskRemove(task) {
            console.info("planTask: ", task)
        },

    },

    created() {
    },

    async mounted() {
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login',
            })
            return
        }

        //
        let res = await ctx.gameplay.api_getPlanTasks(this.idPlan)

        //
        this.plan = res.plan
        this.planTasks = res.planTasks
        this.statistic = res.statistic
    },

    unmounted() {
    },

}

</script>


<style lang="less" scoped>

hr {
    margin: 1em 0;
}

.plan-tasks {
    font-size: 1.5em;
}

.game-info {
    font-size: 1.5em;
    text-align: center;
}

</style>