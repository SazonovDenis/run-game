<template>
    <div class="question">
        Вопрос: {{ task.text }}
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        task: null
    },

    /*
        data() {
            return {
            }
        },
    */

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