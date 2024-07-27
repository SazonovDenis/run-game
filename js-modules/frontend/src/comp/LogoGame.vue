<template>
    <div :class="classLogo">
        <img class="logo-image" v-bind:src="backgroundImage">
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
            return apx.url.ref("run/game/web/img/logo.svg")
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
}

.logo-image {
    width: 4rem;
    height: 4rem;
    opacity: 0.7;
}

.logo img {
    display: flex;
    flex-direction: column;
    align-items: center;
    height: 4rem;
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