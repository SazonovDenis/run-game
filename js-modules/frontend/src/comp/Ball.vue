<template>
    <div class="ball-panel"
         v-bind:style="{ display: display, width: width,  height: height , left: left,  top: top }">
        {{ ball.text }}
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        ball: {},
    },

    data() {
        return {
            widthBase: 50
        }
    },

    methods: {
        getSize() {
            let d = this.widthBase

            if (this.$el && this.$el.offsetWidth && this.$el.offsetWidth > 0) {
                d = this.$el.offsetWidth
            }

            return d
        }
    },

    computed: {

        width() {
            return this.widthBase * this.ball.value + 'px'
        },

        height() {
            return this.widthBase * this.ball.value + 'px'
        },

        left() {
            return this.ball.x - this.getSize() / 2 + "px"
        },

        top() {
            return this.ball.y - this.getSize() / 2 + "px"
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
    border: 1px solid green;
    user-select: none;
    width: 1em;
    height: 1em;
    border-radius: 10em;
    background-color: rgba(100, 200, 0, 0.3);
    z-index: 2000;
}

</style>
