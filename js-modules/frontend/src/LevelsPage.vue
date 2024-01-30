<template>

    <MenuContainer title="Мои уровни">

        <q-list bordered separator>

            <template v-for="plan in plans">

                <q-item clickable @click="planTaskStatistic(plan.plan)">

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


        <q-page-sticky
            position="bottom-right"
            :offset="[18, 18]">
            <q-fab style="height: 4em; width: 4em;"
                   color="purple"
                   _text-color="black"
                   icon="add"
                   vertical-actions-align="right"
                   direction="up">
                <q-fab-action style="height: 4em; _width: 4em;"
                              color="secondary"
                              _text-color="black"
                              label="Создать свой уровень" square icon="mail"
                              @click="onCreateLevel"
                />
                <q-fab-action style="height: 4em; _width: 4em;"
                              color="amber-10"
                              text-color="black"
                              label="Подключить из библиотеки" square icon="alarm"
                              @click="onAddLevel"
                />
            </q-fab>
        </q-page-sticky>


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

        onCreateLevel() {
            console.log("onCreateLevel")
        },

        onAddLevel() {
            console.log("onAddLevel")
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