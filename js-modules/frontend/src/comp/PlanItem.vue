<template>

    <q-item
        class="item-first"
        clickable v-ripple
    >

        <q-item-section top avatar>

            <q-avatar>

                <PlanRaitingCircularProgress :statistic="plan"/>

            </q-avatar>

        </q-item-section>

        <q-item-section @click="$emit('planClick', plan.plan)">
            <div class="plan-text">
                {{ plan.planText }}
            </div>
            <div class="plan-info">
                {{ plan.wordCount }} {{ wordsText(plan.wordCount) }}
            </div>
            <TagTranslateDirection :tags="plan.tags"/>
        </q-item-section>


        <q-item-section top side>

            <div class="text-grey-8 q-gutter-xs">

                <q-btn
                    v-if="plan.isPublic && plan.isAllowed /*&& viewPlanType==='personal'*/"
                    dense rounded flat no-caps
                    class="item-list-bitton"
                    label="Скрыть"
                    icon="del"
                    color="blue-10"
                    @click="$emit('delUsrPlan', plan)"
                />

                <q-btn
                    v-if="plan.isPublic && !plan.isAllowed && viewPlanType==='common'"
                    dense rounded flat no-caps
                    class="item-list-bitton"
                    label="Добавить"
                    icon="add"
                    color="green-9"
                    @click="$emit('addUsrPlan', plan)"
                />

            </div>

        </q-item-section>


        <!--
                <q-item-section
                    top side
                    style="width: 2em; align-content: end;">

                    <div>

                        <q-badge
                            v-if="plan.ratingTask > 0"
                            color="green-5"
                            :label="plan.ratingTask"/>

                    </div>

                </q-item-section>
        -->

    </q-item>

</template>

<script>

import PlanRaitingCircularProgress from "./PlanRaitingCircularProgress"
import TagTranslateDirection from "./TagTranslateDirection"
import utils from "../utils"

export default {

    name: "PlanItem",

    components: {
        PlanRaitingCircularProgress, TagTranslateDirection
    },

    props: {
        plan: Object,
        viewPlanType: String,
    },

    emits: [
        "addUsrPlan", "delUsrPlan", "planClick",
    ],

    methods: {
        ratingText(rating) {
            return utils.ratingText(rating)
        },
        wordsText(wordCount) {
            return utils.wordsText(wordCount)
        },
    },

}


</script>

<style scoped>

</style>