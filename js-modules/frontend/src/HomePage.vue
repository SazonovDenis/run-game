<template>
    <div>
        <div v-if="dataLoaded">
            <PlanEditPage
                :plan="plan"
                :doEditPlan="false"
            />
        </div>
    </div>
</template>

<script>

import WordPage from "./WordPage"
import PlanEditPage from "./PlanEditPage"
import auth from "./auth"
import {daoApi} from "run-game-frontend/src/dao"
import {apx} from "run-game-frontend/src/vendor"

export default {

    components: {
        PlanEditPage,
        WordPage,
    },

    data() {
        return {
            dataLoaded: false,
            plan: null,
            defaultViewMode: "addByText",
        }
    },

    async mounted() {
        // Есть текущий пользователь?
        if (!auth.isAuth()) {
            apx.showFrame({
                frame: '/login'
            })
            return
        }

        //
        let userInfo = auth.getUserInfo()
        let planId = userInfo.planDefault

        //
        let resApi = await daoApi.loadStore(
            'm/Plan/getPlanUsr', [planId]
        )
        let planDefault = resApi.records[0]

        //
        this.plan = planDefault

        //
        this.dataLoaded = true
    }

}
</script>
