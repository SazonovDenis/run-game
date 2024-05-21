<template>

    <q-item class="item-first"
            clickable v-ripple
    >
        <q-item-section>
            <div>
                {{ item.planText }}
            </div>
            <div class="game-info">
                {{ item.dbeg }}
            </div>
        </q-item-section>

        <q-item-section
            top side
            style="width: 7em; align-content: end;">

            <div class="col">

                <q-badge
                    v-if="item.ratingTaskDiff > 0"
                    class="col"
                    _style="min-width: 3em; text-align: right"
                    color="white"
                    text-color="green-5"
                    :label="'+' + item.ratingTaskDiff"/>
                <q-badge
                    v-if="item.ratingTaskDiff < 0"
                    class="col"
                    _style="min-width: 100%; align: right"
                    color="white"
                    text-color="red-5"
                    :label="item.ratingTaskDiff"/>
                <q-badge
                    class="col"
                    color="green-5"
                    :label="item.ratingTask"/>

            </div>

        </q-item-section>

    </q-item>

</template>

<script>

export default {

    name: "StatisticGameItem",

    props: {
        item: Object
    },

}

</script>

<style scoped>

.game-info{
    font-size: 0.9em;
    color: gray;
}

</style>