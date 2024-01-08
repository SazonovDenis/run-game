<template>

    <MenuContainer title="Уровни">

        <div v-for="plan in plans"
             class="game-plan-item"
             @click="planTaskStatistic(plan.plan)">
            {{ plan.planText }} ({{ plan.count }})

            <TasksStatistic :tasksStatistic="plan.tasksStatistic"/>

        </div>

    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import auth from "./auth"
import MenuContainer from "./comp/MenuContainer"
import TasksStatistic from "./comp/TasksStatistic"

export default {

    components: {
        MenuContainer, TasksStatistic
    },

    data() {
        return {
            plans: [],
            globalState: ctx.getGlobalState(),
        }
    },

    methods: {
        planTaskStatistic(plan) {
            apx.showFrame({
                frame: '/plan', props: {idPlan: plan}
            })
        },
    },

    async mounted() {
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login',
            })
            return
        }

        //
        this.plans = await gameplay.api_getPlans()
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


</style>