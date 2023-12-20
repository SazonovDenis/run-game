<template>
    <div>
        Итоги и статистика по игре
        <div>{{ game.text }}</div>
    </div>
</template>

<script>

export default {
    name: "GameInfo",

    props: {
        game: null
    }
}

</script>

<style scoped>

</style>