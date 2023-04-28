<template>
    <tst-panel>
        <template #tools>
        </template>
        Hello!
    </tst-panel>
</template>

<script>
import {apx, tst} from '../vendor'

export default {
    mixins: [tst.mixins.cfgStore],
    created() {
        this.cfgStore.applyDefault({})
    },
    data() {
        return {}
    },
    methods: {
        applyCfg() {
            let cfg = this.cfg
        },
    }
}
</script>
