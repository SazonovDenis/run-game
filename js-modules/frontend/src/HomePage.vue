<template>
    <div>
        <MainWindow/>
    </div>
</template>

<script>

import {apx} from './vendor'
import MainWindow from "./comp/MainWindow"

export default {

    extends: apx.JcFrame,

    components: {
        MainWindow,
    },

    created() {
    },

}
</script>
