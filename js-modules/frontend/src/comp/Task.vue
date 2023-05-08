<template>
    <div class="question" @click="play">
        <span v-if="this.task.sound">
            <q-icon size="2em" name="asterisk"/>
            <span>&nbsp;</span>
        </span>

        <span v-if="this.task.text">
            {{ task.text }}
        </span>

        <span>&nbsp;</span>
        <q-icon size="2em" name="help" @click="getHint"/>
    </div>
</template>

<script>

import {apx} from "../vendor"

export default {
    components: {},

    props: {
        task: {},
        state: {},
    },

    methods: {
        play() {
            if (this.task.sound) {
                try {
                    this.audio.play()
                } catch(e) {
                    console.error(e)
                }
            }
        },
        getHint() {
            this.state.game.modeShowOptions = "hint-true"
        },
    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                if (this.task.sound) {
                    this.audio.src = apx.url.ref("sound/" + this.task.sound)
                } else {
                    this.audio.src = ""
                }
                //this.audio.src = apx.url.ref("sound/1000-puzzle-english/mp3/campbridge_UK/able.mp3")
            }, deep: true,
        }
    },

    computed: {},

    mounted() {
        let audio = this.audio = new Audio()
        audio.addEventListener('loadeddata', function() {
            //loaded = true;
            audio.play();
        }, false);
    },
}
</script>


<style>

.question {
    max-width: 20em;
    margin: 5px;
    padding: 5px;
    border-radius: 5px;
    background-color: #e6ffda;
}

</style>