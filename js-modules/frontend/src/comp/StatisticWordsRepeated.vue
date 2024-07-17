<template>

    <q-circular-progress
        show-value
        rounded
        size="8rem"
        :thickness="0"
        track-color="white"
        class="q-mx-sm result-words-repeated"
    >
        <div class="col">
            <div class="result-value">{{
                    statistic.wordCountRepeated
                }}
            </div>
            <div class="result-title"
                 v-html="wordsText(statistic.wordCountRepeated)">
            </div>
        </div>
    </q-circular-progress>

</template>

<script>
import utils from "run-game-frontend/src/utils"

export default {
    name: "StatisticWordsRepeated",
    props: {
        statistic: Object,
    },
    methods: {
        wordsText(rating) {
            return utils.wordsText(rating) + "<br>повторено"
        }
    }
}
</script>

<style scoped>

/* --- */

.result-value {
    font-weight: bold;
    font-size: 1.4em;
}

.result-title {
    font-weight: bold;
    font-size: 0.5em;
}

/* --- */

.result-words-repeated {
    font-weight: bold;
    color: #008080;
    background-color: #00808010;
    border-radius: 10em;

    text-align: center;
    font-size: 2em;
}


</style>