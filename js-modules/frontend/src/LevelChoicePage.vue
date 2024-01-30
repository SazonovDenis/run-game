<template>

    <MenuContainer title="Выбор уровня">

        <q-list bordered separator>

            <template v-for="plan in plans">

                <q-item clickable @click="gameStart(plan.id)">

                    <q-item-section top avatar>
                        <q-avatar icon="folder" color="grey-4" text-color="white"/>
                    </q-item-section>

                    <q-item-section>
                        {{ plan.text }}
                    </q-item-section>

                    <q-item-section top side>

                        <div clas="row">

                            <q-badge
                                color="green-5"
                                :label="'0 / ' + plan.count"/>

                        </div>

                    </q-item-section>

                </q-item>

            </template>

        </q-list>
        
<!--
        <div v-for="plan in plans"
             class="game-plan-item"
             @click="gameStart(plan.plan)">
            {{ plan.planText }} ({{ plan.count }})

            <TasksStatistic :tasksStatistic="plan.tasksStatistic"/>

        </div>
-->

    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import MenuContainer from "./comp/MenuContainer"
import auth from "./auth"
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
        async gameStart(idPlan) {
            await gameplay.gameStart(idPlan)

            apx.showFrame({
                frame: '/game', props: {}
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