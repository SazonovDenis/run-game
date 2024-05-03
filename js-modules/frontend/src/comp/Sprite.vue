<template>

    <div class="rgm-sprite" :style="{width: width, height: height}">
        <img
            :style="{width: width, height: height}"
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
        return {}
    },

    computed: {
        backgroundImage() {
            let idx = this.animation.still
            if (!idx) {
                idx = 0
            }
            return apx.url.ref("run/game/web/sprite/" + this.name + "/k" + idx + ".png")
        },
    }

}

</script>


<style scoped>

.rgm-sprite {
    position: relative;
    _border: 1px solid red;
}

</style>