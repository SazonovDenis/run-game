<template>
    <div>
        <jc-frame-shower-page/>
    </div>
</template>

<script>
import {apx} from './vendor'

// Единственный экран приложения

export default {
    extends: apx.JcApp,
    components: {},

    created() {
        this.title = "RunGame"
    },

    methods: {}
}
</script>
