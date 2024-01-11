<template>

    <div class="game-info">

        <div class="game-info__text">{{ planText }}</div>

        <div class="row">

            <q-circular-progress
                show-value
                rounded
                size="10rem"
                font-size="1rem"
                :value="100*rating/ratingMax"
                :thickness="0.22"
                color="green-7"
                track-color="grey-3"
                class="q-ma-md game-info__rating-info"
            >
                <div class="col">
                    <div>
                        <span class="game-info__rating-value">{{ rating }}</span>
                    </div>
                    <div>
                        <span class="game-info__rating-text">
                            {{ ratingText(rating) }}
                        </span>
                    </div>
                    <div>
                        <span>из </span>
                        <span class="game-info__rating-max">{{ ratingMax }}
                    </span>
                    </div>
                </div>
            </q-circular-progress>

            <div class="game-info__rating-info col self-center">
                <div class="">

                    <div>
                        За скорость:
                    </div>
                    <div>
                    <span class="game-info__rating-quickness">
                        {{ ratingQuickness }}
                    </span>
                    </div>
                    <div class="game-info__rating-quickness-text">
                        {{ ratingText(ratingQuickness) }}
                    </div>

                </div>

            </div>
        </div>

    </div>

</template>

<script>

import utils from "../utils"

export default {
    name: "PlanInfo",

    props: {
        planText: null,
        ratingMax: null,
        rating: null,
        ratingQuickness: null,
    },

    methods: {
        ratingText(rating) {
            return utils.ratingText(rating)
        }
    }

}

</script>

<style lang="less" scoped>

.game-info {
    font-size: 1.5em;
    text-align: center;
    margin: 0 2rem;

    &__text {
        font-size: 1.5em;
        color: #34558b;
    }

    &__rating-info {
        color: #4c4c4c;
    }

    &__rating-value {
        font-size: 4em;
        color: #5b9e3a;
    }

    &__rating-text {
        color: #5b9e3a;
    }

    &__rating-max {
        font-size: 1.5em;
    }

    &__rating-quickness {
        font-size: 1.5em;
        color: #34558b;
    }

    &__rating-quickness-text {
        font-size: .6em;
        color: #34558b;
    }

    &__rating-inc {
        font-size: 1.5em;
        color: #5b9e3a;
    }

    &__rating-dec {
        color: #850000;
    }

}

</style>