<template>
    <div class="help-item bg-yellow-2 text-grey-9"
         v-if="doShow()"
         @click="closeClick"
    >
        <q-btn flat no-caps
               size="md"
               label="Принято"
               class="help-btn-close text-grey-9"
               @click="closeClick"
        />

        <div class="q-px-sm q-py-md">
            <div class="help-text bg-yellow-2" v-html="text"></div>
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

.help-item {
    position: relative;
    width: 30rem;
    max-width: 60rem;
    flex-grow: 1;
}

.help-btn-close {
    font-size: 1.3em;
    background-color: rgba(214, 188, 74, 0.3);

    position: absolute;
    right: 0rem;
    bottom: 0rem;
}


</style>