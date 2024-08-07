<template>
    <div v-if="visibleCount > 0"
         class="help-panel-container row q-gutter-x-sm">
        <template v-for="helpKey in helpKeys">
            <HelpItem :helpKey="helpKey"/>
        </template>
    </div>
</template>

<script>

import HelpItem from "./HelpItem"
import utils from "../utils"

export default {

    name: "HelpPanel",

    props: {
        helpKey: null,
    },

    components: {HelpItem},

    computed: {

        helpKeys() {
            let helpKeysArr = utils.getHelpKeysArr(this.helpKey)
            if (helpKeysArr.length === 0) {
                return null
            }
            return helpKeysArr
        },

        visibleCount() {
            return utils.helpItemVisibleCount(this.helpKey)
        }

    },

}
</script>


<style scoped>

.help-panel-container {
    border-radius: 0.3em;
    border: solid 1px #FFFFFF01;
    overflow: hidden;
}

</style>