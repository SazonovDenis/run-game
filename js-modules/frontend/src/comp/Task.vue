<template>
    <div class="question" @click="play">

        <div v-if="doShowSound" class="task-sound">
            <q-icon size="1.5em" name="asterisk"/>
            <span>&nbsp;</span>
        </div>

        <div v-if="doShowText" class="task-text">
            {{ task.text }}
        </div>
        <div v-else class="task-text">
            &nbsp;
        </div>

        <div class="task-help">
            <q-icon size="2em" name="help" @click="showHint"/>
        </div>

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
            this.play()
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

.task-help, .task-sound, .task-text {
    height: 1.3em;
}

.question {
    display: flex;
    flex-direction: row;

    user-select: none;
    _max-width: 20em;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    background-color: #e6ffda;
}

.task-sound {
    color: #7a7a7a;
}

.task-help {
    color: #474747;
}

.task-text {
    width: 100%;
    font-size: 1.5em;
}

</style>