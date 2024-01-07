<template>

    <MenuContainer title="Уровень">

        <div v-for="planTask in planTasks" class="plan-info">

            <div class="row">

                <GameTask :task="planTask"/>

                <div class="col" style="max-width: 5em">
                    {{ planTask.rating }}
                </div>

            </div>

        </div>

        <jc-btn kind="primary"
                label="Играть уровень"
                style="min-width: 10em;"
                @click="gameStart()">
        </jc-btn>

    </MenuContainer>

</template>


<script>

import {apx} from "./vendor"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import auth from "./auth"
import {daoApi} from "./dao"
import MenuContainer from "./comp/MenuContainer"
import GameTask from "./comp/GameTask"

export default {
    name: "PlanPage",

    props: {
        plan: null,
    },

    components: {
        MenuContainer, GameTask
    },

    computed: {},

    // Состояние игрового мира
    data() {
        return {
            planTasks: {},
            globalState: ctx.getGlobalState(),
        }
    },

    methods: {

        isAuth() {
            return auth.isAuth()
        },

        async gameStart() {
            await gameplay.gameStart(this.plan)

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
        let resApi = await daoApi.loadStore('m/Game/getPlanTaskStatistic', [this.plan])

        //
        this.planTasks = resApi.records
    },

    unmounted() {
    },

}

</script>


<style scoped>

.plan-info {
    font-size: 1.5em;
}

</style>