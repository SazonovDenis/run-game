<template>

    <q-item class="item-first"
            clickable v-ripple
    >
        <q-item-section>
            Игра: {{ item.text }}
        </q-item-section>

        <q-item-section
            top side
            style="width: 2em; align-content: end;">

            <div class="row">

                <q-badge
                    _class="col"
                    color="green-5"
                    :label="item.rating.rating + ' / ' + item.rating.words"/>

            </div>

        </q-item-section>

    </q-item>

</template>

<script>

export default {

    name: "StatisticGameItem",

    props: {
        item: Object
    },

}

</script>

<style scoped>

</style>