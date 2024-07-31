<template>

    <template v-if="item.ratingTask === ratingTaskMax">

        <q-badge
            rounded
            class="rgm-bage"
            :text-color="getRatingTextColor(item.ratingTask)"
            :color="getRatingColor(item.ratingTask)"
            :label="getRatingText(item.ratingTask)"
        />

    </template>

    <template v-else>

        <template v-for="item in score">

            <q-icon
                name="circle"
                size="1em"
                :color="item.done ? 'green-8' : 'grey-4'"
                :label="item.text ? item.text : '&nbsp;'"
            />

            <!--
                            :text-color="getRatingTextColor(item.ratingTask)"
                            :color="getRatingColor(item.ratingTask)"
            -->

        </template>

    </template>

</template>

<script>

import appConst from "../dao/appConst"


export default {

    name: "RaitingValue",

    props: {
        item: Object,
    },

    computed: {
        ratingTaskMax() {
            return appConst.RATING_FACT_MAX
        },
        score() {
            let res = []

            for (let i = appConst.RATING_FACT_MAX; i >= 1; i--) {
                //
                let done = false
                if (this.item.ratingTask >= i) {
                    done = true
                }

                //
                let text = "  "
                if (i === 1) {
                    text = this.item.ratingTask
                }

                //
                res.push({
                    done: done,
                    text: text
                })
            }

            return res
        },
    },

    methods: {

        ratingAboveForPosition(i) {
            return this.item.ratingTask < i
        },

        getRatingColor(rating) {
            if (rating === 3) {
                return "green-8"
            } else if (rating >= 2) {
                return "green-3"
            } else if (rating >= 1) {
                return "green-1"
            } else {
                return "blue-grey-1"
            }
        },

        getRatingTextColor(rating) {
            if (rating === 3) {
                return "white"
            } else if (rating >= 2) {
                return "black"
            } else if (rating >= 1) {
                return "black"
            } else {
                return "black"
            }
        },

        getRatingText(rating) {
            if (rating === 3) {
                return "выучено"
            } else {
                return rating
            }
        },

    },

}

</script>

<style scoped>

</style>