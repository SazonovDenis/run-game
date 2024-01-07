<template>

    <MenuContainer title="Уровень">

        <div class="game-info">

            <div class="game-info__text">{{ plan.planText }}</div>

            <div class="game-info__count" style="padding-top: 0.5em">
                Всего баллов: {{ statistic.rating }} из {{ statistic.ratingMax }}
            </div>
            <div class="game-info__count">
                Баллы за скорость: {{ statistic.ratingQuickness }}
            </div>

        </div>

        <q-separator/>

        <jc-btn kind="primary"
                label="Играть уровень"
                style="min-width: 10em;"
                @click="gameStart()">
        </jc-btn>

        <q-separator/>

        <div v-for="planTask in planTasks" class="plan-info">

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
import GameInfo from "./comp/GameInfo"

export default {
    name: "PlanPage",

    props: {
        idPlan: null,
    },

    components: {
        MenuContainer, GameTask, GameInfo
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

.plan-info {
    font-size: 1.5em;
}


hr {
    margin: 1em 0;
}

.game-info {
    font-size: 1.5em;
    text-align: center;

    &__text {
        font-size: 1.5em;
        color: #34558b;
    }

    &__duration {
        color: #6c6c6c;
    }

    &__count {
        color: #4c4c4c;
    }

    &__ratingDec {
        color: #850000;
    }

    &__ratingInc {
        color: #5b9e3a;
    }
}

</style>