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
            <div class="result-title">
                повторено
            </div>
        </div>
    </q-circular-progress>

</template>

<script>
export default {
    name: "StatisticWordsRepeated",
    props: {
        statistic: Object,
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
    color: #00307e;
    background-color: #00307E19;
    border-radius: 10em;
}

.result-words-done {
    font-weight: bold;
    color: #43a047;
}

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

</style>