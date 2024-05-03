<template>

    <div class="rgm-sprite" :style="{width: width, height: height}" ref="spriteImage">
        <img
            class="rgm-sprite-img"
            :style="{height: height, left: imgLeft}"
            v-bind:src="backgroundImage"
        >
    </div>

</template>

<script>

import {apx} from "../vendor"

export default {

    props: {
        name: "",
        width: "1em",
        height: "1em",
        animation: {}
    },

    data() {
        return {
            // todo это положено брать из метаинформации о спрайте
            imgOriginalWidth: 740,
            imgOriginalHeight: 800,
        }
    },

    methods: {},

    computed: {

        imgLeft() {
            let stillIdx = this.animation.still
            if (!stillIdx) {
                stillIdx = 0
            }

            // Учтем сжатие спрайта и сдвинем кадр не на оригинальную ширину imgOriginalWidth,
            // а на пропорционально меньший размер
            let actualWidthK = 1
            let spriteImage = this.$refs.spriteImage
            if (spriteImage) {
                actualWidthK = spriteImage.clientHeight / this.imgOriginalHeight
            }
            let imgActualWidth = actualWidthK * this.imgOriginalWidth

            //
            return "-" + (imgActualWidth * stillIdx) + "px"
        },

        backgroundImage() {
            return apx.url.ref("run/game/web/sprite/" + this.name + "/1.png")
        },

    }

}

</script>


<style scoped>

.rgm-sprite {
    position: relative;
    overflow: hidden;
    _border: 1px solid red;
}

.rgm-sprite-img {
    position: relative;
}

</style>