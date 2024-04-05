<template>
    <div class="row task-bar">

        <TaskValue class="task-value"
                   :task="task"
                   :showTaskHint="state.showTaskHint"
                   :doShowText="doShowText"
        />

        <div class="task-help-icon">
            <q-icon size="2em" name="help" @click="showHint"/>
        </div>

    </div>
</template>

<script>

import {apx} from "../vendor"
import dbConst from "../dao/dbConst"
import ctx from "../gameplayCtx"
import utils from '../utils'
import TaskValue from "../comp/TaskValue"


/**
 * Задание в игре. Вопрос показанный в правильном состоянии (текст скрыт, если нужно)
 */
export default {
    components: {TaskValue},

    props: {
        task: {},
        state: {},
    },

    methods: {

        loadAudioTask() {
            // Останавливаем текущий звук
            try {
                this.audio.pause()
            } catch(e) {
                console.error(e)
            }

            //
            this.state.showTaskHint = false

            // Новый звук
            this.state.taskSoundLoaded = false
            this.audio.src = utils.getAudioSrc(this.task)
        },

        play() {
            if (this.canPlaySound) {
                try {
                    this.audio.play()
                } catch(e) {
                    console.error(e)
                }
            }
        },

        showHint() {
            this.state.showTaskHint = true

            //
            this.play()

            //
            ctx.eventBus.emit("showHint", true)
        },


        onTaskOptionSelected(taskOption) {
            // Играть звук, если была подсказка - значит идет заучивание
            if (taskOption.isTrue && this.state.showTaskHint) {
                this.play()
            }
        },

        onSoundLoaded() {
            this.state.taskSoundLoaded = true

            // Автоматически играем звук, если тип попроса это разрешает (тап вопроса - правописание или звук)
            if (this.canPlaySoundAuto()) {
                this.audio.play()
            }
        },

        onSoundError() {
            this.state.taskSoundLoaded = false
            //console.info("onSoundError: " + this.audio.src)
        },

        canPlaySoundAuto() {
            return (
                this.state.showTaskHint ||
                this.task.dataType === dbConst.DataType_word_sound ||
                this.task.dataType === dbConst.DataType_word_spelling
            )
        },


    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                this.loadAudioTask()
            }, deep: true
        }
    },

    computed: {

        wave() {
            return apx.url.ref("run/game/web/img/wave.png")
        },

        canPlaySound() {
            return (
                this.task != null &&
                this.task.valueSound != null &&
                this.state.taskSoundLoaded &&
                this.canPlaySoundAuto()
            )
        },

        doShowText() {
            return (
                this.task != null &&
                (this.task.valueSpelling != null || this.task.valueTranslate != null) &&
                (
                    this.state.showTaskHint ||
                    this.task.dataType !== dbConst.DataType_word_sound
                )
            )
        },

        doShowSound() {
            return (
                this.task != null &&
                this.task.valueSound != null
            )
        },

    },

    mounted() {
        let audio = this.audio = new Audio()
        audio.addEventListener('loadeddata', this.onSoundLoaded, false)
        audio.addEventListener('error', this.onSoundError, false)

        //
        ctx.eventBus.on("taskOptionSelected", this.onTaskOptionSelected)

        //
        this.loadAudioTask()
    },

    unmounted() {
        ctx.eventBus.off("taskOptionSelected", this.onTaskOptionSelected)
    },

}
</script>


<style scoped>

.task-bar {
    height: 3.5em;
    margin: 5px;

    user-select: none;
    border-radius: 0.5rem;
    background-color: #e6ffda;
}

.task-value {
    flex-grow: 100;
    padding: 0.5em;
    font-size: 1.6em;
}

.task-help-icon {
    padding: 0.8em;
    color: #404040;
}

</style>