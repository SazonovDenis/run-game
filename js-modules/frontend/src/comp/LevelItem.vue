<template>

    <q-item
        class="item-first"
        clickable v-ripple
    >

        <q-item-section top avatar>
            <q-avatar
                v-if="plan.isDefault"

                icon="star"
                color="grey-2"
                text-color="yellow-8">

                <div class="plan-count-text">
                    {{ plan.count }}
                </div>

            </q-avatar>

            <q-avatar
                v-else-if="plan.isPublic"

                icon="folder-open"
                color="grey-2"
                text-color="grey-8">

                <div class="plan-count-text">
                    {{ plan.count }}
                </div>

            </q-avatar>

            <q-avatar
                v-else

                icon="user"
                color="grey-2"
                text-color="grey-6">

                <div class="plan-count-text">
                    {{ plan.count }}
                </div>

            </q-avatar>

        </q-item-section>

        <q-item-section @click="$emit('planClick', plan.plan)">
            {{ plan.planText }}
        </q-item-section>


        <q-item-section top side>

            <div class="text-grey-8 q-gutter-xs">

                <q-btn
                    v-if="plan.isPublic && plan.isAllowed && viewPlanType==='pesonal'"
                    dense round flat
                    size="1.2em"
                    icon="del"
                    color="grey-8"
                    @click="$emit('delUsrPlan', plan)"
                />

                <q-btn
                    v-if="plan.isPublic && !plan.isAllowed && viewPlanType==='common'"
                    dense round flat
                    size="1.2em"
                    icon="add"
                    color="green-9"
                    @click="$emit('addUsrPlan', plan)"
                />


                <!--
                                                <q-btn flat dense round
                                                       icon="more-h"
                                                       size="1.0em"
                                                />
                -->

            </div>

        </q-item-section>


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

    </q-item>

</template>

<script>

export default {

    name: "LevelItem",

    components: {},

    props: {
        plan: Object,
        viewPlanType: String,
    },

    emits: [
        "addUsrPlan", "delUsrPlan", "planClick",
    ],

    methods: {},

}


</script>

<style scoped>

.item-first {
    border-top: 1px solid silver;
}

.plan-count-text {
    padding: 5px 4px 15px 4px;
    margin-top: 2px;
    width: 6em;
    text-align: center;
    font-size: .6em;
    color: #202020;
    background-color: #dbecfb;
    border-radius: 10px;
}

</style>