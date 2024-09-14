<template>

    <PlanEditPage
        tabMenuName="WordsPage"
        :plan="plan"
        :doEditPlan="false"
        defaultMode="addByText"
        :loaded="dataLoaded"
    />

</template>

<script>

import auth from "./auth"
import {daoApi} from "./dao"
import MenuContainer from "./comp/MenuContainer"
import PlanEditPage from "./PlanEditPage"
import ctx from "run-game-frontend/src/gameplayCtx"

/**
 * Главная страница.
 * Представляет собой редактор плана, включенный в режиме добавления слов.
 * Параметр data.plan - план по умолчанию текущего пользователя.
 */
export default {

    name: "WordsPage",

    components: {
        MenuContainer,
        PlanEditPage,
    },

    data() {
        return {
            dataLoaded: false,

            plan: {},
        }
    },

    async mounted() {
        let globalState = ctx.getGlobalState()

        // Уже загружен план по умолчанию?
        if (!globalState.planDefault) {
            // Текущий пользователь и его план
            let userInfo = auth.getContextUserInfo()
            let planDefaultId = userInfo.planDefault

            // Загрузим план по умолчанию текущего пользователя
            let resApi = await daoApi.loadStore(
                'm/Plan/getPlan', [planDefaultId]
            )
            let planDefault = resApi.records[0]

            // Сохраним на будущее
            globalState.planDefault = planDefault
        }


        //
        this.plan = globalState.planDefault

        //
        this.dataLoaded = true

    }

}
</script>
