<template>

    <q-item class="item-first"
            clickable v-ripple
    >
        <q-item-section>
            {{ item.planText }}
        </q-item-section>

        <q-item-section
            top side
            style="width: 2em; align-content: end;">

            <div class="row">

                <q-badge
                    _class="col"
                    color="green-5"
                    :label="item.ratingTask + ' / ' + item.count"/>
                <q-badge
                    class="col"
                    style="min-width: 3em; text-align: right"
                    color="white"
                    text-color="green-5"
                    :label="'+' + item.ratingInc"/>
                <q-badge
                    class="col"
                    style="min-width: 100%; align: right"
                    color="white"
                    text-color="red-5"
                    :label="'-' + item.ratingDec"/>

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

</style>