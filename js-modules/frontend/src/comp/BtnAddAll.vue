<template>

    <q-btn
        class="rgm-style"
        no-caps
        icon="add"
        label="Добавить все"
        :disabled="count === 0"
        color="green-7"
        @click="click"
    >
        <q-badge rounded floating
                 v-if="count > 0"
                 color="green-3"
                 class="btn-bage"
                 :label="'+' + count"
        />
    </q-btn>


</template>

<script>

export default {

    name: "BtnAddAll",

    props: {
        count: {type: Number, default: 0},
        emits: ["click"]
    },

    methods: {
        click() {
            this.$emit("click")
        }
    },

}

</script>

<style scoped>

.btn-bage {
    border: 1px solid #43a047;
    color: black;
}

</style>