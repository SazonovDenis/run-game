<template>
    <div class="help-panel-container row" v-if="helpKey">
        <template v-for="helpKey in helpKeys">
            <HelpItem :helpKey="helpKey" class="q-ma-sm"/>
        </template>
    </div>
</template>

<script>

import HelpItem from "./HelpItem"

export default {

    name: "HelpPanel",

    props: {
        helpKey: null,
    },

    components: {HelpItem},

    computed: {

        helpKeys() {
            if (typeof (this.helpKey) === "string") {
                return [this.helpKey]
            } else {
                return this.helpKey
            }
        },

    },

}
</script>


<style scoped>

.help-panel-container {
}

</style>