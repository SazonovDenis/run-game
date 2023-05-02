<template>
    <div class="question">
        <div v-if="this.task.sound">
            <q-btn color="blue" text-color="black" label="Play" @click="play"/>
        </div>

        <span>Вопрос: {{ task.text }} </span>
    </div>
</template>

<script>


export default {
    components: {},

    props: {
        task: {}
    },

    methods: {
        play() {
            if (this.task.sound) {
                this.audio.play()
            }
        }
    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                if (this.task.sound) {
                    this.audio.src = this.task.sound
                } else {
                    this.audio.src = ""
                }
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