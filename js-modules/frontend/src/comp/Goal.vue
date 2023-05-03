<template>
    <div class="goal-panel" v-bind:style="{ display: display }">
        <div class="goal-value-bar" v-bind:style="{ height: height }">
            <div class="goal-value">{{ goal.text }}</div>
            <div class="goal-value-text">{{ goal.value }}</div>
        </div>
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

.goal-panel {
    text-align: center;
    width: 7em;
    height: 8em;
    border-radius: 1em;
    border: 2px solid rgba(0, 143, 200, 0.6);
    z-index: 1000;
    display: block;
}

.goal-value {
    padding: 5px;
}

.goal-value-bar {
    border-radius: 1em;
    background-color: rgba(0, 143, 200, 0.3);
}

.goal-value-text {
    font-size: 200%;
}

</style>
