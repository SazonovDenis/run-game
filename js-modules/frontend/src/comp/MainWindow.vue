<template>
    <q-btn color="white" text-color="black" label="Next" v-on:click="nextTask"/>

    <UserTaskPanel :usrTask="usrTask" :dataState="dataState"/>

</template>


<script>

import UserTaskPanel from "./UserTaskPanel"
import gameplay from "../gameplay"
import ctx from "../gameplayCtx"

export default {
    name: "MainWindow",

    components: {
        UserTaskPanel, gameplay
    },

    // Состояние игрового мира
    data() {
        return {
            usrTask: {
                task: {},
                taskOptions: {}
            },

            dataState: {
                drag: {
                    dtStart: null,
                    dtStop: null,
                    drag: null,
                    x: null,
                    y: null,
                    sx: null,
                    sy: null,
                },
                goal: {
                    text: null,
                    value: null,
                },
                ball: {
                    text: null,
                    value: 0,
                    ballIsTrue: null,
                },
                game: {
                    modeShowOptions: null,
                    goalHitSize: null,
                }
            },

            testData_taskIdx: 1,
        }
    },

    methods: {

        // Присваиваем данные задания себе
        nextTask() {
            gameplay.nextTask()
        },

        // Присваиваем данные задания себе
        onLoadedUsrTask(usrTask) {
            this.usrTask = usrTask
        },
    },

    created() {
        gameplay.init(this.dataState)
    },

    mounted() {
        ctx.eventBus.on("loadedUsrTask", this.onLoadedUsrTask)

        //
        gameplay.nextTask()
    }

}

</script>


<style scoped>

</style>