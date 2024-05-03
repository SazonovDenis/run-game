<template>
    <div class="user-task">
        <div v-if="gameTask">
            <Task :task="gameTask.task" :state="dataState"/>
        </div>

        <div style="position: relative">
            <Sprite name="blow" width="5rem" height="5rem"
                    :style="{position: 'absolute', top: '-2.5rem', left: blowPosLeft()}"
                    :animation="animation"/>
        </div>

        <Goal v-if="gameTask" class="goal" id="goal" :goal="dataState.goal"/>


        <div class="game-field">&nbsp;</div>


        <div v-if="gameTask" class="task-options">
            <TaskOptions :taskOptions="gameTask.taskOptions" :state="dataState"/>
        </div>


        <Ball class="ball" id="ball" :drag="dataState.drag" :ball="dataState.ball"/>


        <div v-if="!wasLoadedAllSounds">

            <br>
            <q-separator/>
            <br>

            <jc-btn
                kind="primary"
                label="Пропустить"
                v-on:click="nextTask"
            />

        </div>

        <div v-if="isEnvDev">

            <br>
            <q-separator/>
            <br>

            <jc-btn
                kind="srcondary"
                label="Пропустить test"
                v-on:click="nextTask"
            />

        </div>

    </div>

</template>

<script>


import Ball from "./Ball"
import Goal from "./Goal"
import Task from "./Task"
import TaskOptions from "./TaskOptions"
import Sprite from "./Sprite"
import ctx from "../gameplayCtx"
import gameplay from "../../src/gameplay"
import dbConst from "../dao/dbConst"
import {jcBase} from "../vendor"

/**
 * Компонент "игровое поле", показываются задание и ответы.
 */
export default {
    components: {Sprite, gameplay, Ball, Goal, Task, TaskOptions},

    props: {
        gameTask: {},
        dataState: {},
    },

    data() {
        return {
            animation: ctx.getGlobalState().sprite.blow,
        }
    },

    methods: {

        blowPosLeft() {
            return "calc(" + Math.round(this.dataState.ball.x) + "px - 2.5rem)"
        },

        nextTask() {
            gameplay.loadNextTask()
        },

    },

    computed: {

        isEnvDev() {
            return jcBase.cfg.envDev
        },

        wasLoadedAllSounds() {
            return (
                this.dataState.taskSoundLoaded ||
                !this.gameTask ||
                !this.gameTask.task ||
                this.gameTask.task.dataType !== dbConst.DataType_word_sound
            )
        },

    },

    async mounted() {
    },

    unmounted() {
    },

}
</script>


<style>

.user-task {
    _position: relative;
    display: block;
}

.game-field {
    _border: 1px dotted red;
    height: 20vh;
}

.task-options {
    _position: absolute;
    bottom: 0;
    width: 100%;
}

.goal {
    _position: relative;
    _bottom: 50px;
    left: 40%;
}

.ball {
    position: absolute;
}


</style>
