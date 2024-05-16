<template>

    <div>

        <PlanEditPage
            :plan="plan"
            :doEditPlan="false"
            defaultMode="addByText"
        />

    </div>


</template>

<script>

import auth from "./auth"
import {daoApi} from "./dao"
import MenuContainer from "./comp/MenuContainer"
import PlanEditPage from "./PlanEditPage"

/**
 * Главная страница.
 * Представляет собой редактор плана, включенный в режиме добавления слов.
 * План - план по умолчанию текущего пользователя.
 */
export default {

    name: "HomePage",

    components: {
        MenuContainer,
        PlanEditPage,
    },

    data() {
        return {
            plan: {},
        }
    },

    async mounted() {
        let userInfo = auth.getContextUserInfo()
        let planDefaultId = userInfo.planDefault

        // Загрузим план по умолчанию текущего пользователя
        let resApi = await daoApi.loadStore(
            'm/Plan/getPlan', [planDefaultId]
        )
        let planDefault = resApi.records[0]

        //
        this.plan = planDefault
    }

}
</script>
