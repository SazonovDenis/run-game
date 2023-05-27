<template>
    <tst-panel>
        <jc-btn @click="emit('xxx')">emit xxx</jc-btn>
    </tst-panel>
</template>

<script>

import {apx} from '../vendor'

export default {

    components: {
        apx,
    },

    methods: {
        emit(ev) {
            console.info("emit xxx!")
            apx.app.eventBus.emit("xxx", {info: "yes"})
        }
    },

    created() {
        apx.app.eventBus.on("xxx", (ev) => {
            console.info("xxx!!!")
            console.info("ev:", ev)
        })
    }
}
</script>
