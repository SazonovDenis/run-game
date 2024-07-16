<template>

    <q-item class="item"
            clickable v-ripple
    >
        <q-item-section>
            {{ item.planText }}
        </q-item-section>

        <q-item-section
            top side
        >

            <div _class="col">

                <q-badge
                    v-if="item.ratingTaskDiff > 0"
                    style="font-weight: bold;"
                    _class="col"
                    color="white"
                    text-color="green-9"
                    :label="'+' + item.ratingTaskDiff"/>
                <q-badge
                    v-if="item.ratingTaskDiff < 0"
                    style="font-weight: bold;"
                    _class="col"
                    color="white"
                    text-color="red-5"
                    :label="item.ratingTaskDiff"/>

                <q-badge
                    style="font-weight: bold;"
                    _class="col"
                    color="green-7"
                    :label="item.ratingTask"/>

            </div>

        </q-item-section>

    </q-item>

</template>

<script>

export default {

    name: "StatisticPlanItem",

    props: {
        item: Object
    },

}

</script>

<style scoped>

.item {
    min-height: 3rem;
}

</style>