<template>

    <q-item class=""
            clickable v-ripple
    >
        <q-item-section>
            <div>
                {{ item.planText }}
            </div>
            <div class="game-info">
                {{ dbeg }}
            </div>
        </q-item-section>

        <q-item-section
            top side
        >

            <div class="col">

                <q-badge
                    v-if="item.ratingTaskDiff > 0"
                    style="font-weight: bold;"
                    _class="col"
                    color="white"
                    text-color="green-5"
                    :label="'+' + item.ratingTaskDiff"/>
                <q-badge
                    v-if="item.ratingTaskDiff < 0"
                    style="font-weight: bold;"
                    _class="col"
                    color="white"
                    text-color="red-5"
                    :label="item.ratingTaskDiff"/>

            </div>

        </q-item-section>

    </q-item>

</template>

<script>

import {apx} from "run-game-frontend/src/vendor"

export default {

    name: "StatisticGameItem",

    props: {
        item: Object
    },

    computed: {
        dbeg() {
            let displayFormat2 = "d MMMM, HH:MM"
            return apx.date.toDateTime(this.item.dbeg).toFormat(displayFormat2)
        }
    },

}

</script>

<style scoped>

.game-info {
    font-size: 0.9em;
    color: gray;
}

</style>