<template>
    <div class="question" @click="play">

        <span v-if="doShowSound">
            <q-icon size="2em" name="asterisk"/>
            <span>&nbsp;</span>
        </span>

        <span v-if="doShowText">
            {{ task.text }}
        </span>

        <span>&nbsp;</span>
        <q-icon size="2em" name="help" @click="showHint"/>
    </div>
</template>

<script>

import {apx} from "../vendor"
import dbConst from "../dao/dbConst"
import ctx from "../gameplayCtx"

export default {
    components: {},

    props: {
        task: {},
        state: {},
    },

    data() {
        return {
            alwaysShowText: null
        }
    },

    methods: {

        onTaskOptionSelected(taskOption) {
            this.alwaysShowText = true
        },

        play() {
            if (this.task.sound) {
                try {
                    this.audio.play()
                } catch(e) {
                    console.error(e)
                }
            }
        },

        showHint() {
            if (this.task.text) {
                this.audio.play()
            }
            //
            this.alwaysShowText = true
            //
            ctx.eventBus.emit("showHint", true)
        },

    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                //
                this.alwaysShowText = false

                // Новый звук
                if (this.task.sound) {
                    this.audio.src = apx.url.ref("sound/" + this.task.sound)
                } else {
                    this.audio.src = ""
                }
                //this.audio.src = apx.url.ref("sound/1000-puzzle-english/mp3/campbridge_UK/able.mp3")
            }, deep: true
        }
    },

    computed: {

        doShowText() {
            return (
                this.task.text != null &&
                (
                    this.alwaysShowText ||
                    this.task.dataType == dbConst.DataType_word_spelling
                )
            )
        },

        doShowSound() {
            return (
                this.task.sound != null
            )
        },

    },

    mounted() {
        let audio = this.audio = new Audio()
        audio.addEventListener('loadeddata', function() {
            this.play()
        }, false)

        //
        ctx.eventBus.on("taskOptionSelected", this.onTaskOptionSelected)
    },
}
</script>


<style>

.question {
    user-select: none;
    max-width: 20em;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    background-color: #e6ffda;
}

</style>