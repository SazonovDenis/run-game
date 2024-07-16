<template>

    <q-circular-progress
        show-value
        rounded
        size="8rem"
        :value="value"
        :thickness="thickness"
        color="green-7"
        track-color="grey-4"
        :class="'q-mx-sm result-words-learned ' + getClass"
    >
        <div class="col">
            <div class="result-value">{{
                    statistic.wordCountLearned
                }}
            </div>
            <div class="result-title"
                 v-html="wordsText(statistic.wordCountLearned)">
            </div>
            <div class="result-words2" v-if="statistic.wordCount">
                <span class="result-words2-title">из </span>
                <span class="result-words2-value">{{
                        statistic.wordCount
                    }}
                </span>
            </div>
        </div>
    </q-circular-progress>

</template>

<script>
import utils from "run-game-frontend/src/utils"

export default {
    name: "StatisticWordsLearned",
    props: {
        statistic: Object,
    },
    computed: {
        value() {
            if (!this.statistic.wordCount) {
                return null
            } else {
                return 100 * this.statistic.wordCountLearned / this.statistic.wordCount
            }
        },
        thickness() {
            if (!this.statistic.wordCount) {
                return 0
            } else {
                return 0.2
            }
        },
        getClass() {
            if (!this.statistic.wordCount) {
                return "with-count"
            } else {
                return ""
            }
        }
    },
    methods: {
        wordsText(rating) {
            return utils.wordsText(rating) + "<br>выучено"
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

.result-words-learned {
    text-align: center;
    font-weight: bold;
    border-radius: 10em;
    color: #109010;
}

.with-count {
    background-color: #10901010;
}

.result-words2 {
    position: relative;
    top: -0.1em;
    color: #535353;
}

.result-words2-title {
    font-weight: normal;
    font-size: 0.5em;
}

.result-words2-value {
    font-weight: normal;
    font-size: 0.8em;
}

</style>