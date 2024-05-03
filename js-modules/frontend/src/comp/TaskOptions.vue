<template>
    <div class="options" id="options">
        <div v-for="(taskOption, index) in taskOptions">
            <TaskOption
                v-if="animationData.taskOptionVisible[index]"
                :taskOption="taskOption"
                :state="state"
            />
        </div>

    </div>
</template>

<script>

import TaskOption from "./TaskOption"
import ctx from "../gameplayCtx"

export default {
    components: {TaskOption},

    props: {
        taskOptions: null,
        state: null,
    },

    data() {
        return {
            animationData: {
                showTaskOptions: false,
                taskOptionVisible: [],
            }
        }
    },

    watch: {

        taskOptions: {
            handler(val, oldVal) {
                let cfg = {
                    taskOptionsLength: this.taskOptions.length
                }
                ctx.animation.animationRestart("TaskOptionsVisibleAnimation",
                    this.animationData, cfg
                )
            },
            immediate: true,
        }

    },

    methods: {
        getClassName(taskOption) {
            if (this.state.mode.modeShowOptions == "hint-true") {
                if (taskOption.isTrue) {
                    return "fact-true"
                } else {
                    return "fact-false"
                }
            }
        },
    },

    computed: {},

}
</script>


<style>

.options {
    user-select: none;
    touch-action: none;

    display: flex;
    flex-wrap: wrap;
}

</style>