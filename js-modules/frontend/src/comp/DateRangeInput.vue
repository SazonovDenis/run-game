<template>

    <q-btn-toggle
        v-model="params.value"
        :options="options"

        no-caps
        rounded
        _unelevated

        class="rgm-toggle"
        toggle-color="blue-7"
        color="white"
        text-color="primary"
    />

</template>

<script>

import {apx} from "../vendor"

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

    setup() {
        return {
            periodOptions: [
                {label: 'Сегодня', value: 'day'},
                {label: 'Неделя', value: 'week'},
                {label: 'Месяц', value: 'month'},
                {label: 'Три месяца', value: 'month3'},
            ],
            periodOptions_text: {
                day: "сегодня",
                week: "неделю",
                month: "месяц",
                month3: "три месяца",
            }
        }
    },

    mounted() {
        // Выберем разрешенные значения для показа (т.е. все, кроме скрытых)
        let options = []
        for (let option of this.periodOptions) {
            if (!this.hiddenValues || this.hiddenValues.indexOf(option.value) === -1) {
                options.push(option)
            }
        }
        this.options = options

/*
        // Если текущее значение (this.params.value) не разрешено,
        // то выберем первое среди разрешенных
        console.info("this.hiddenValues", this.hiddenValues)
        console.info("this.value", this.value)
        if (this.hiddenValues && this.hiddenValues.indexOf(this.params.value) !== -1) {
            this.params.value = options[0].value
        }
*/
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

        getPeriodText(value) {
            return this.periodOptions_text[value]
        },

    },

}
</script>

<style scoped>

.rgm-toggle {
}

</style>