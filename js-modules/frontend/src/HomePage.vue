<template>

    <div>

        <PlanEditPage
            :plan="plan"
            :doEditPlan="false"
        />


        <MenuContainer
            v-if="!dataLoaded && error"
            :showFooter="true"
            footerMode="error"
        >

            <template v-slot:footerContent >
                <div>{{ error }}</div>
            </template>

        </MenuContainer>

    </div>


</template>

<script>

import auth from "./auth"
import {daoApi} from "./dao"
import {apx} from "./vendor"
import MenuContainer from "./comp/MenuContainer"
import PlanEditPage from "./PlanEditPage"

export default {

    name: "HomePage",

    components: {
        MenuContainer,
        PlanEditPage,
    },

    data() {
        return {
            dataLoaded: false,
            error: null,
            plan: {},
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

        try {
            //
            let userInfo = auth.getUserInfo()
            let planDefaultId = userInfo.planDefault

            //
            let resApi = await daoApi.loadStore(
                'm/Plan/getPlan', [planDefaultId]
            )
            let planDefault = resApi.records[0]

            //
            this.plan = planDefault

            //
            this.dataLoaded = true
        } catch(e) {
            this.error = e.message
        }
    }

}
</script>
