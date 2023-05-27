<template>
    <div class="game-info">
        <span class="game-text">{{ text }}</span>
        <span class="game-info">{{ info }}</span>
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        game: {},
    },

    computed: {
        text() {
            if (this.game.id > 0) {
                return this.game.text
            } else {
                return "Игра не начата"
            }

        },
        info() {
            if (this.game.id > 0) {
                return this.game.countDone + "/" + this.game.countTotal
            } else {
                return ""
            }

        },
    },

    data() {
        return {}
    },

}
</script>


<style>

.game-info {
    background-color: #eeeeee;
    padding: 5px;
}

.game-text {
    font-size: 80%;
    color: #850000;
}

.game-info {
    color: #003eff;
}

</style>
