<template>
    <div>
        <MainWindow/>
    </div>
</template>

<script>

import {apx} from './vendor'
import MainWindow from "./MainPage"

export default {

    extends: apx.JcFrame,

    components: {
        MainWindow,
    },

    created() {
    },

}
</script>
