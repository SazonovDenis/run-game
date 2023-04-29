<template>
    <div
        v-touch-pan.prevent="handlePan"
        v-touch-pan.prevent.mouse="handlePan"
        className="game-touch-area"
    >
        HELLO!

    </div>
</template>

<script>

export default {
    methods: {

        handlePan({evt, ...newInfo}) {
            console.info(newInfo)
        }

    },

}


</script>

<style>

.game-touch-area {
    width: 100%;
    height: 250px;
    border-radius: 3px;
    border: 1px dotted red;
    padding: 8px;
}

</style>
