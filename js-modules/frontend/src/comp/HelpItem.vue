<template>
    <div class="help-container"
         v-if="doShow()"
         @click="closeClick"
    >
        <q-btn icon="close" bordered round dense
               text-color="black"
               class="help-btn-close"
               @click="closeClick"
        />

        <div class="q-pa-sm">
            <div class="help-text" v-html="text"></div>
        </div>
    </div>
</template>

<script>

import ctx from "../gameplayCtx"
import utils from "../utils"
import stringDat from "../stringDat"

export default {

    name: "HelpItem",

    props: {
        helpKey: String,
    },

    data() {
        return {
            // Обернули helpState в свою data
            helpState: ctx.getGlobalState().helpState,
        }
    },

    computed: {

        helpValueKey() {
            return this.helpKey
        },

        text() {
            return stringDat[this.helpValueKey]
        },

    },

    methods: {
        doShow() {
            if (utils.isHelpItemHidden(this.helpState, this.helpValueKey)) {
                return false
            }
            if (!this.text) {
                return false
            }
            return true
        },
        closeClick() {
            this.helpState[this.helpValueKey] = false
            ctx.eventBus.emit("change:settings")
        },
    },

}
</script>


<style scoped>

.help-container {
    position: relative;
    color: #4e4e4e;
    background-color: #fff2c8;
    border-radius: 0.5em;
    border: 1px solid #fff2c8;
    width: 30rem;
    max-width: 60rem;
    flex-grow: 1;
}

.help-btn-close {
    background-color: #fff2c8;
    position: absolute;
    right: -0.3rem;
    top: -0.3rem;
}


</style>