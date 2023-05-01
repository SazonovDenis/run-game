<template>
    <div class="question">
        <span>Вопрос: {{ task.text }} </span>
        <q-btn color="blue" text-color="black" label="AUD"/>
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        task: {}
    },

    methods: {},

    computed: {}
}
</script>


<style>

.question {
    max-width: 20em;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    background-color: #e6ffda;
}

</style>