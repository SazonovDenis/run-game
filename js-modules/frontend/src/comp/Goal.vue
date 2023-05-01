<template>
    <div class="goal" v-bind:style="{ height: height, display: display }">
        <div>{{ goal.text }}</div>
        <div class="goal-value">{{ goal.value }}</div>
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        goal: {},
    },

    methods: {},

    computed: {

        height() {
            return 2 * this.goal.value + 'em'
        },

        display() {
            if (this.goal.value <= 0) {
                return "none"
            } else {
                return "block"
            }
        },

    }
}
</script>


<style>

.goal {
    padding: 10px;
    text-align: center;
    position: absolute;
    width: 7em;
    _height: 10em;
    bottom: 50px;
    left: 40%;
    border-radius: 1em;
    background-color: rgba(0, 143, 200, 0.3);
    z-index: 1000;
    display: block;
}

.goal-value {
    font-size: 200%;
}

</style>
