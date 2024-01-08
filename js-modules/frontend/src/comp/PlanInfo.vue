<template>

    <div class="game-info">

        <div class="game-info__text">{{ planText }}</div>

        <div class="game-info__count" style="_padding-top: 0.5em">
            Всего баллов:
            <span class="game-info__rating">{{ rating }}</span>
            из
            <span class="game-info__rating-max">{{ ratingMax }}</span>
        </div>

        <div class="game-info__count">
            Баллы за скорость:
            <span class="game-info__rating-quickness">{{ ratingQuickness }}</span>
        </div>

    </div>

</template>

<script>

export default {
    name: "PlanInfo",

    props: {
        planText: null,
        ratingMax: null,
        rating: null,
        ratingQuickness: null,
    },

}

</script>

<style lang="less" scoped>

.game-info {
    font-size: 1.5em;
    text-align: center;

    &__text {
        font-size: 1.5em;
        color: #34558b;
    }

    &__count {
        color: #4c4c4c;
    }

    &__rating {
        font-size: 5em;
        color: #5b9e3a;
    }

    &__rating-max {
        font-size: 2.5em;
    }

    &__rating-quickness {
        font-size: 1.5em;
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