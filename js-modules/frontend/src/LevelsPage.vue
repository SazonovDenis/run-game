<template>

    <MenuContainer title="Уровни">

        <div v-for="plan in plans"
             class="game-plan-item"
             @click="click(plan.id)">
            {{ plan.text }} ({{ plan.count }})
            <div class="game-plan-item-progress">
                <div v-for="(item, index) in plan.taskInfo"
                     :class="'game-plan-item-progress-el game-plan-item-progress-bar ' + getClass(item, index, plan.taskInfo.length)"
                     :style="{height: getHeight(plan, item)}">
                </div>
            </div>
            <div class="game-plan-item-progress">
                <div v-for="(item, index) in plan.taskInfo"
                     class="game-plan-item-progress-el game-plan-item-progress-num">
                    {{ getItemCount(item) }}
                </div>
            </div>
        </div>

    </MenuContainer>

</template>

<script>

import {apx} from './vendor'
import {daoApi} from "./dao"
import gameplay from "./gameplay"
import ctx from "./gameplayCtx"
import MenuContainer from "./comp/MenuContainer"
import auth from "./auth"

export default {

    extends: apx.JcFrame,

    components: {
        MenuContainer
    },

    data() {
        return {
            plans: [],
            globalState: ctx.getGlobalState(),
        }
    },

    methods: {
        getClass(item, index, count) {
            if (index === 0) {
                return "game-plan-todo"
            } else if (index === (count - 1)) {
                return "game-plan-done"
            } else {
                return "game-plan-inprogress"
            }
        },

        getHeight(plan, item) {
            if (plan.count === 0) {
                return "0em"
            }
            let perc = 100 * item.count / plan.count
            let em = perc / 50
            return em + 'em'
        },

        getItemCount(item) {
            if (item.count > 0) {
                return item.count
            } else {
                return ""
            }
        },

        async click(idPlan) {
            await gameplay.gameStart(idPlan)

            apx.showFrame({
                frame: '/game', props: {}
            })
        },

        async loadLevels() {
            let res = await daoApi.loadStore(
                "m/Game/getPlans", {}
            )

            this.plans = res.records
        },

    },

    mounted() {
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login',
            })
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
    align-items: flex-end;
}

.game-plan-item-progress-el {
    margin: 0.1rem;
    padding: 0.1rem 0.2rem;
    min-width: 3rem;
}

.game-plan-item-progress-bar {
    border: 1px solid silver;
    border-radius: 5px;
}

.game-plan-item-progress-num {
    text-align: center;
    font-size: 90%;
    _border: 1px solid silver;
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