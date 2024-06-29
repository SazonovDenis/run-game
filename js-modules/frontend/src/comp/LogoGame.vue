<template>
    <div :class="classLogo">
        <img v-bind:src="backgroundImage">
    </div>
</template>

<script>

import {apx} from "../vendor"

export default {

    name: "LogoGame",

    props: {
        loading: false,
    },

    computed: {
        classLogo() {
            return "logo" + (this.loading ? " animated" : "")
        },
        backgroundImage() {
            return apx.url.ref("run/game/web/img/cube.png")
        },
    },

}

</script>

<style scoped>

.logo {
    padding-top: 1rem;
    display: flex;
    flex-direction: column;
    align-items: center;

    height: 6rem;
}

.logo img {
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 5rem;
}

@keyframes fade {
    from {
        opacity: 1;
    }
    50% {
        opacity: 0.2;
    }
    to {
        opacity: 1;
    }
}

.animated {
    animation-name: fade;
    animation-delay: .2s;
    animation-duration: 1s;
    animation-iteration-count: infinite;
}

</style>