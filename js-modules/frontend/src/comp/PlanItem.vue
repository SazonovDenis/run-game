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
                    v-if="plan.isPublic && plan.isAllowed"
                    dense round flat no-caps
                    class="item-list-bitton"
                    icon="star-filled"
                    color="green-9"
                    :text-color="itemTextColor(plan)"
                    @click="$emit('delUsrPlan', plan)"
                />

                <q-btn
                    v-if="plan.isPublic && !plan.isAllowed"
                    dense round flat no-caps
                    class="item-list-bitton"
                    icon="star"
                    color="green-9"
                    :text-color="itemTextColor(plan)"
                    @click="$emit('addUsrPlan', plan)"
                />

                <q-icon
                    v-if="plan.isDefault"
                    name="star-star-filled"
                    size="xs"
                    class="q-mr-sm"
                    color="yellow-9"
                />

                <q-icon
                    v-else-if="plan.isOwner"
                    name="star-star-filled"
                    size="xs"
                    class="q-mr-sm"
                    color="green"
                />

            </div>

        </q-item-section>


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
        itemTextColor(plan) {
            if (plan.isPublic && plan.isAllowed) {
                return "green"
            } else {
                return "green-9"
            }
        },
    },

}


</script>

<style scoped>

</style>