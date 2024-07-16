<template>

    <q-circular-progress
        show-value
        rounded
        size="8rem"
        :thickness="0"
        track-color="white"
        class="q-mx-sm result-words-all"
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

.result-words-all {
    font-weight: bold;
    color: #008080;
    background-color: #00808010;
    border-radius: 10em;
}


/*
.result-words2 {
    position: relative;
    top: -0.1em;
    color: #535353;
}

.result-words2-title {
    font-size: 0.5em;
}

.result-words2-value {
    font-size: 0.8em;
}
*/

</style>