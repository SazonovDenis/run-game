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
    align-items: center;

    background-color: #eeeeee;
    border-radius: 5em;
    padding: 0.3em;
    margin: 0 0.3em;
}

.user-name {
    margin-left: 0.5em;
}

.user-icon {
    width: 2em;
    height: 2em;
    margin-left: 0.5em;
    border-radius: 5em;
}

</style>
