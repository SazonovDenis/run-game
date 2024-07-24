<template>

    <div>

        <q-circular-progress
            instant-feedback
            rounded
            size="3rem"
            font-size="1rem"
            :value="value"
            :thickness=".3"
            color="green-7"
            track-color="grey-2"
            :class="'circular '+ getClass"
        >

        </q-circular-progress>


        <div class="col circular-value-text statistic-type-learned">

            <div v-if="statistic.wordCountLearned > 0">
                <span class="value-value statistic-type-learned">
                    {{ statistic.wordCountLearned }}
                </span>
            </div>

            <!--
            <div class="total" v-if="statistic.wordCount">
                <span class="total-title">из </span>
                <span class="total-value">{{ statistic.wordCount }}</span>
            </div>
            -->

        </div>

    </div>


</template>

<script>

export default {

    name: "PlanRaitingCircularProgress",

    props: {
        statistic: {},
    },

    computed: {
        value() {
            if (this.statistic.wordCount > 0 && this.statistic.wordCountLearned > 0) {
                return 100 * this.statistic.wordCountLearned / this.statistic.wordCount
            } else {
                return null
            }
        },
        getClass() {
            if (this.statistic.wordCount > 0 && this.statistic.wordCountLearned === this.statistic.wordCount) {
                return "statistic-type-learned-all"
            } else {
                return ""
            }
        },
    }

}
</script>

<style scoped>


.circular {
    text-align: center;
    font-weight: bold;
}

.value-value {
    font-weight: bold;
    font-size: 0.8em;
}

.total {
    color: #4e4e4e;
    margin-top: 0.2em;
}

.total-title {
    font-weight: bold;
    _font-weight: normal;
    font-size: 0.5em;
}

.total-value {
    font-weight: bold;
    _font-weight: normal;
    font-size: 0.7em;
}


.circular-value-text {
    width: 3rem;
    height: 3rem;
    position: absolute;
    left: 1px;
    top: 1px;
    text-align: center;
    align-content: center;
    _opacity: 0.9;
}

</style>