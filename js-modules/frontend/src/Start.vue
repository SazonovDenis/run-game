<template>
    <div>
        <q-btn color="white" text-color="black" label="Start game" @click="click"/>
    </div>
</template>

<script>

import {apx} from './vendor'

export default {

    extends: apx.JcFrame,

    components: {},

    data() {
        return {}
    },

    created() {
    },

    methods: {
        click() {
            this.openFullscreen()
            apx.showFrame({frame: import("./Home")})
        },

        /* View in fullscreen */
        openFullscreen() {
            /* Get the documentElement (<html>) to display the page in fullscreen */
            var elem = document.documentElement;

            if (elem.requestFullscreen) {
                elem.requestFullscreen();
            } else if (elem.webkitRequestFullscreen) { /* Safari */
                elem.webkitRequestFullscreen();
            } else if (elem.msRequestFullscreen) { /* IE11 */
                elem.msRequestFullscreen();
            }
        },

        /* Close fullscreen */
        closeFullscreen() {
            if (document.exitFullscreen) {
                document.exitFullscreen();
            } else if (document.webkitExitFullscreen) { /* Safari */
                document.webkitExitFullscreen();
            } else if (document.msExitFullscreen) { /* IE11 */
                document.msExitFullscreen();
            }
        }

    }
}
</script>
