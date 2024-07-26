<template>
    <div class="row task-bar">

        <TaskValue class="task-value"
                   :task="task"
                   :showTaskHint="state.showTaskHint"
                   :doShowText="doShowText"
                   ref="taskValue"
        />

        <div class="task-help-icon">
            <q-icon size="2em" name="help" @click="doShowHint"/>
        </div>

    </div>
</template>

<script>

import dbConst from "../dao/dbConst"
import ctx from "../gameplayCtx"
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

        play() {
            this.$refs.taskValue.play()
        },

        doShowHint() {
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

    },

    watch: {
        task: {
            handler(newValue, oldValue) {
                this.$refs.taskValue.play()
            }, deep: true
        }
    },

    computed: {

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

    },

    mounted() {
        ctx.eventBus.on("taskOptionSelected", this.onTaskOptionSelected)

        //
        this.play()
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