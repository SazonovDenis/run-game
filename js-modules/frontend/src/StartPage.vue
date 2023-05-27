<template>
    <div>
        <q-btn color="white" text-color="black" label="Start game" @click="click"/>
    </div>
</template>

<script>

import {apx, jcBase} from './vendor'
import utils from './utils'

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
            if (!jcBase.cfg.envDev) {
                utils.openFullscreen()
            }
            apx.showFrame({frame: import("./HomePage")})
        },

    }
}
</script>
