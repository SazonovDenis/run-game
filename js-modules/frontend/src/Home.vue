<template>
    <decor-page>
        Добро пожаловать
    </decor-page>
</template>

<script>
import {apx} from './vendor'

export default {
    extends: apx.JcFrame,
    created() {
        this.title = 'Добро пожаловать'
    },
}
</script>
