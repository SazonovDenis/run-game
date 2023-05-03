<template>
    <div class="ball-panel"
         v-bind:style="{ display: display, width: width,  height: height }">
        {{ ball.text }}
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        ball: {}
    },

    methods: {},

    computed: {

        width() {
            return 2 * this.ball.value + 'em'
        },

        height() {
            return 2 * this.ball.value + 'em'
        },

        display() {
            if (this.ball.value <= 0) {
                return "none"
            } else {
                return "block"
            }
        },

    }

}
</script>


<style>

.ball-panel {
    _width: 2em;
    _height: 2em;
    border-radius: 2em;
    background-color: rgba(100, 200, 0, 0.3);
    z-index: 2000;
    _display: none;
}

</style>
