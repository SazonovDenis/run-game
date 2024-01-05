<template>
    <div class="main-window-img">
        <img v-bind:src="backgroundImage">
    </div>
</template>

<script>

import {apx} from "../vendor"

export default {

    name: "LogoGame",

    computed: {
        backgroundImage() {
            return apx.url.ref("run/game/web/img/cube.png")
        },
    },

}

</script>

<style scoped>

.main-window-img img {
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 5rem;
}

.main-window-img {
    padding-top: 1rem;
    display: flex;
    flex-direction: column;
    align-items: center;

    height: 6rem;
}

</style>