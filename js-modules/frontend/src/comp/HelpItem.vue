<template>
    <div class="help-item bg-yellow-2 text-grey-9"
         v-if="doShow()"
         @click="closeClick"
    >

        <div class="q-px-sm q-pt-md q-pb-lg">
            <div class="help-text bg-yellow-2" v-html="text"></div>
        </div>

        <q-btn flat no-caps
               size="md"
               label="Ok"
               class="help-btn-close text-grey-9"
               @click="closeClick"
        />

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
        return {}
    },

    computed: {

        helpValueKey() {
            return this.helpKey
        },

        text() {
            // Добавляем пробелы в конец, чтобы последнее слово не перекрыло
            // кнопку "Ок", которая находится на одном уровне с последней срокой.
            return stringDat[this.helpValueKey] + "&nbsp;&nbsp;&nbsp;"
        },

    },

    methods: {

        doShow() {
            if (!this.text) {
                return false
            }

            if (utils.isHelpItemHidden(this.helpValueKey)) {
                return false
            }

            return true
        },

        closeClick() {
            let globalHelpState = ctx.getGlobalState().helpState
            globalHelpState[this.helpValueKey] = false
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

    border-bottom: 2px solid rgba(214, 188, 74, 0.3);
}

.help-item:last-child {
    border-bottom: none;
}

.help-btn-close {
    font-size: 1.3em;
    background-color: rgba(214, 188, 74, 0.3);

    position: absolute;
    right: 0rem;
    bottom: 0rem;
}


</style>