<template>
    <div v-bind:class="'ball-panel ' + getClass"
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
            // Хак реактивности. Это написано исключительно чтобы заставить сдвинуться не только
            // при изменении положения this.ball.y, но и при при изменении размера this.ball.value
            // (это тоже повлияет на положение, т.к. шар растет из "центра").
            this.ball.value
            //
            return this.ball.x - this.getSize() / 2 + "px"
        },

        top() {
            // Хак реактивности. Это написано исключительно чтобы заставить сдвинуться не только
            // при изменении положения this.ball.y, но и при при изменении размера this.ball.value
            // (это тоже повлияет на положение, т.к. шар растет из "центра").
            this.ball.value
            //
            return this.ball.y - this.getSize() / 2 + "px"
        },

        getClass() {
            if (this.ball.ballIsTrue) {
                return "ball-panel-true"
            } else {
                return "ball-panel-false"
            }
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
    user-select: none;
    width: 1em;
    height: 1em;
    border-radius: 10em;
    z-index: 2000;
}

.ball-panel-true {
    border: 1px solid green;
    background-color: rgba(100, 200, 0, 0.3);
}

.ball-panel-false {
    border: 1px solid #c8c8c8;
    background-color: rgba(103, 115, 89, 0.3);
}

</style>
