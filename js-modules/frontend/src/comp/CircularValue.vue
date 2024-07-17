<template>

    <q-circular-progress
        show-value
        rounded
        size="10rem"
        font-size="1rem"
        :value="circularValue"
        :thickness="thickness"
        color="blue-10"
        track-color="grey-3"
        class="q-ma-md circle-value"
    >
        <div class="col">
            <div>
                <span class="value-value">
                    {{ value }}
                </span>
            </div>
            <div>
                <span class="value-title">
                    {{ title }}
                </span>
            </div>
            <div>
                <span>из </span>
                <span class="value-max">
                    {{ valueTotal }}
                </span>
            </div>
        </div>
    </q-circular-progress>

</template>

<script>
export default {

    name: "CircularValue",

    props: {
        value: Number,
        title: String,
        valueTotal: Number,
    },

    computed: {
        circularValue() {
            if (!this.valueTotal) {
                return null
            } else {
                return 100 * this.value / this.valueTotal
            }
        },
        thickness() {
            if (!this.valueTotal) {
                return 0
            } else {
                return 0.2
            }
        },
        getClass() {
            if (!this.valueTotal) {
                return "with-count"
            } else {
                return ""
            }
        }
    }

}
</script>

<style scoped>

.circle-value {
    font-size: 1.5em;
    text-align: center;
}

.value-value {
    font-weight: bold;
    font-size: 1.4em;
}

.value-title {
    font-weight: bold;
    font-size: 0.5em;
}

</style>