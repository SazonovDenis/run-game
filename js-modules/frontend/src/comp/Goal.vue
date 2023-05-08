<template>
    <div class="goal-panel" v-bind:style="{ display: display }">
        <span v-for="(x, index) in getValuesArr()"
              v-bind:class="'goal-value ' + getClassName(index)">★</span>
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        goal: {},
    },

    methods: {
        getValuesArr() {
            return new Array(this.goal.valueGoal)
        },

        getClassName(value) {
            if (value >= this.goal.valueDone) {
                return "done-false"
            } else {
                return "done-true"
            }
        },
    },

    computed: {

        display() {
            if (this.goal.valueDone >= this.goal.valueGoal) {
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
    _border: 1px solid red;
    z-index: 1000;
}

.goal-value {
    font-size: 1.5em;
    padding: 2px;
}

.done-true {
    color: rgba(15, 200, 0, 0.5);
}

.done-false {
    color: rgba(10, 10, 10, 0.3);
}

</style>
