<template>
    <div class="option">
        Вариант: {{ taskOption.text }}
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        taskOption: null
    },

    methods: {},

    computed: {}
}
</script>


<style>

.option {
    max-width: 20em;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    background-color: #fff5da;
}

</style>

