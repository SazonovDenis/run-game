<template>

    <div style="width: 5em; height: 5em;">
        <img style="width: 5em; height: 5em;" v-bind:src="backgroundImage">
    </div>

</template>

<script>

import {apx} from "../vendor"

export default {

    props: {
        data: {}
    },

    data() {
        return {}
    },

    computed: {
        backgroundImage() {
            let idx = this.data.p
            if (!idx) {
                idx = 0
            }
            return apx.url.ref("run/game/web/animations/k/k" + idx + ".png")
        },
    }

}

</script>

<style scoped>

.ball-xxx {
    border-radius: 10em;
    background-color: #5b9e3a;
}

</style>