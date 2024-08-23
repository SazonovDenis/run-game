<template>

    <div class="row q-gutter-x-sm">

        <q-btn
            v-if="showEdit"
            round no-caps class="btn-pair"
            _label="Редактировать"
            color="purple-4"
            icon="edit"
            @click="emitEvent('onPlanEdit')"
        />


        <q-btn
            v-if="showAdd"
            rounded no-caps class="btn-pair btn-pair-text"
            label="Добавить слова"
            color="green-7"
            icon="add"
            style="width: 16rem"
            @click="emitEvent('onPlanAddFact')"
        />

    </div>


</template>

<script>

export default {

    name: "BtnsEditAdd",

    props: {
        showAdd: {type: Boolean, default: true},
        showEdit: {type: Boolean, default: true},
        emits: ["onPlanEdit", "onPlanAddFact"]
    },

    methods: {
        emitEvent(eventName) {
            this.$emit(eventName)
        }
    },

}

</script>

<style scoped>

.btn-pair {
    font-size: 1.2em;
    min-height: 3.6rem;
}

.btn-pair-text {
    font-size: 1.3em;
    width: 16rem;
}

</style>