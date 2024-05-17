<template>

    <q-item class="item-first"
            clickable v-ripple
    >
        <q-item-section>
            {{ item.text }}
        </q-item-section>

        <q-item-section
            top side
            style="width: 2em; align-content: end;">

            <div class="row">

                <q-badge
                    _class="col"
                    color="green-5"
                    :label="item.rating.rating + ' / ' + item.rating.words"/>
                <q-badge
                    class="col"
                    style="min-width: 3em; text-align: right"
                    color="white"
                    text-color="green-5"
                    :label="'+' + item.rating.ratingInc"/>
                <q-badge
                    class="col"
                    style="min-width: 100%; align: right"
                    color="white"
                    text-color="red-5"
                    :label="'-' + item.rating.ratingDec"/>

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