<template>
    <div class="user-info">
        <div class="user-name">{{ name }}</div>
        <div class="user-icon" v-bind:style="{backgroundColor: color}">&nbsp;</div>
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        user: {},
    },

    computed: {
        name() {
            if (this.user.id > 0) {
                return this.user.name
            } else {
                return "Гость"
            }
        },
        color() {
            if (this.user.id > 0) {
                return this.user.color
            } else {
                return "grey"
            }
        },
    },

    data() {
        return {}
    },

}
</script>


<style>


.user-info {
    display: flex;
    flex-direction: row;

    background-color: #eeeeee;
    border-radius: 5em;
    padding: 0.5em;
    margin: 0.2em;
}

.user-name {
}

.user-icon {
    width: 1.2em;
    height: 1.2em;
    margin-left: 0.5em;
    border-radius: 5em;
}

</style>
