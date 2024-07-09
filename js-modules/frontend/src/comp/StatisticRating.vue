<template>

    <div class="row">

        <template v-if="rating.ratingTaskInc != 0">
            <div class="col result-rating result-rating-inc">
                <div class="result-value"><span
                    class="result-no-bold">&plus;</span>{{
                        rating.ratingTaskInc
                    }}
                </div>
                <div class="result-title">заработано</div>
            </div>
        </template>

        <template v-if="rating.ratingTaskDec != 0">
            <div class="col result-rating result-rating-dec">
                <div class="result-value"><span
                    class="result-no-bold">&minus;</span>{{
                        rating.ratingTaskDec
                    }}
                </div>
                <div class="result-title">потеряно</div>
            </div>
        </template>

    </div>

</template>

<script>
export default {
    name: "StatisticRating",
    props: {
        rating: Object,
    }
}
</script>

<style scoped>

/* --- */

.result-value {
    font-weight: bold;
    font-size: 1.3em;
}

.result-title {
    font-weight: bold;
    font-size: 0.5em;
}

.result-rating .result-title {
    padding-left: 0.5em;
}

/* --- */

.result-rating {
    max-width: 4em;
    border-radius: 10em;
    padding: 0;
    margin: 0 0.3em;
}

.result-rating-inc {
    color: #43a047;
}

.result-rating-dec {
    color: #b30000;
}

.result-no-bold {
    font-weight: normal;
}

</style>