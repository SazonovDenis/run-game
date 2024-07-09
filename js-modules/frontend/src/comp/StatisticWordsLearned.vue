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
                    rating.wordCountLearned
                }}
            </div>
            <div class="result-title">
                выучено
            </div>
            <div class="result-words2" v-if="rating.wordCount">
                <span class="result-words2-title">из </span>
                <span class="result-words2-value">{{
                        rating.wordCount
                    }}
                </span>
            </div>
        </div>
    </q-circular-progress>

</template>

<script>
export default {
    name: "StatisticWordsLearned",
    props: {
        rating: Object,
    },
    computed: {
        value() {
            if (!this.rating.wordCount) {
                return null
            } else {
                return 100 * this.rating.wordCountLearned / this.rating.wordCount
            }
        },
        thickness() {
            if (!this.rating.wordCount) {
                return 0
            } else {
                return 0.2
            }
        },
        getClass() {
            if (!this.rating.wordCount) {
                return "with-count"
            } else {
                return ""
            }
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
    font-weight: bold;
    border-radius: 10em;
    color: #43a047;
}

.with-count {
    background-color: #43a04719;
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