<template>

    <div style="position: relative">
        <div class="ball-xxx"
             :style="{position: 'absolute', left: data.x+'px', top: data.y+'px', width: data.size+'px', height: data.size+'px', display: data.visible?'block':'none'}">
            &nbsp;x
        </div>
    </div>

</template>

<script>

export default {

    props: {
        data: {}
    },

    data() {
        return {}
    }
}

</script>

<style scoped>

.ball-xxx {
    border-radius: 10em;
    background-color: #5b9e3a;
}

</style>