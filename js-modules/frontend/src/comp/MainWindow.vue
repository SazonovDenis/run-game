<template>
    <q-btn color="white" text-color="black" label="Next" v-on:click="nextTask"/>

    <UserTaskPanel :usrTask="usrTask" :dataState="dataState"/>

</template>


<script>

import UserTaskPanel from "./UserTaskPanel"
import gameplay from "../gameplay"
import {apx} from "../vendor"
import {daoApi} from "../dao/index"

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

            taskIdx: 1,
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

    mounted() {
        //console.info("=== UserTaskPanel.test#created")

        //let res = await kisBase.daoApi.invoke("m/Game/choiceTask", [1001])


        apx.app.eventBus.on("loadedUsrTask", this.onLoadedUsrTask)

        //
        gameplay.init(this.dataState)
        gameplay.nextTask()
    }

}

</script>


<style scoped>

</style>