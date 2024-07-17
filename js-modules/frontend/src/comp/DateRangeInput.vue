<template>

    <q-btn-toggle
        v-model="params.value"
        :options="options"

        no-caps
        rounded

        class="rgm-toggle"
        toggle-color="blue-7"
        color="white"
        text-color="primary"
    />

</template>

<script>

import {apx} from "../vendor"
import utils from "../utils"

export default {

    name: "DateRangeInput",

    props: {
        value: String,
        hiddenValues: Array,
    },

    data() {
        return {
            params: {
                value: this.value || "day",
            },

            options: [],
        }
    },

    mounted() {
        // Выберем разрешенные значения для показа (т.е. все, кроме скрытых)
        let options = []
        for (let option of utils.periodOptions) {
            if (!this.hiddenValues || this.hiddenValues.indexOf(option.value) === -1) {
                options.push(option)
            }
        }
        this.options = options
    },

    methods: {

        createParamsPeriod(value) {
            let today = apx.date.today()
            //
            let dend = today
            let dbeg
            //
            if (value === "day") {
                dbeg = apx.date.add(dend, {days: 0})
            } else if (value === "week") {
                dbeg = apx.date.add(dend, {days: -6})
            } else if (value === "month") {
                dbeg = apx.date.add(dend, {days: -30})
            } else if (value === "month3") {
                dbeg = apx.date.add(dend, {days: -30 * 3})
            }
            //
            return {dbeg: dbeg, dend: dend}
        },

    },

}
</script>

<style scoped>

.rgm-toggle {
}

</style>