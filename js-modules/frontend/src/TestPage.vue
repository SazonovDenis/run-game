<template>

    <MenuContainer title="Test">

        <div class="col q-gutter-md">

            <LogoGame/>

            <div class="col q-gutter-md doc-text">
                <h3>
                    Игровой тренажер &laquo;Word&nbsp;strike&raquo;
                </h3>

            </div>

            <q-separator/>

            <div>
                this.animationFrame: {{ this.globalAnimationFrame }}
            </div>

            <div class="bar-xxx"
                 :style="{width: animations.animation1.p + 'px'}">
                {{ animations.animation1.p }}
            </div>


            <q-separator/>

            <ImagesFrameTest :data="animations.animation3"/>

            <q-separator/>


            <BallTest :data="animations.animation2"/>

            <q-separator/>


            <div class="row" style="margin-top: 1.5em;">

                <div class="q-pa-sm">
                    <jc-btn label="x11"
                            style="min-width: 5em;"
                            @click="x11()">
                    </jc-btn>
                </div>

                <div class="q-pa-sm">
                    <jc-btn label="x11x"
                            style="min-width: 5em;"
                            @click="x11x()">
                    </jc-btn>
                </div>

                <div class="q-pa-sm">
                    <jc-btn label="x22"
                            style="min-width: 5em;"
                            @click="x22()">
                    </jc-btn>
                </div>

                <div class="q-pa-sm">
                    <jc-btn label="x22x"
                            style="min-width: 5em;"
                            @click="x22x()">
                    </jc-btn>
                </div>
            </div>

        </div>

    </MenuContainer>

</template>


<script>

import MenuContainer from "./comp/MenuContainer"
import LogoGame from "./comp/LogoGame"
import BallTest from "./comp/BallTest"
import ImagesFrameTest from "./comp/ImagesFrameTest"

import ctx from "./gameplayCtx"
import gameplay from "./gameplay"
import animation from "./animation"
import AnimationBase from "./AnimationBase"


class Animation1 extends AnimationBase {

    interval = 100

    onStart(data) {
        data.p = 10
    }

    onStep(frames) {
        let data = this.data

        data.p = Math.round(data.p + 5 * frames)

        if (data.p >= 300) {
            this.stop()
        }
    }

    onStop() {
        let data = this.data

        data.p = 10
    }

}

class Animation3 extends AnimationBase {

    interval = 100

    onStart(data) {
        data.p = 0
    }

    onStep(frames) {
        let data = this.data

        data.p = data.p + 1 * Math.round(frames)

        if (data.p >= 10) {
            this.stop()
        }
    }

    onStop() {
        let data = this.data

        data.p = 0
    }

}

class Animation2 extends AnimationBase {

    interval = 5

    onStart(data) {
        data.x = 100
        data.sx = 1
        data.y = 100
        data.sy = 3
        data.size = 100
        data.visible = true
    }

    onStep(frames) {
        let data = this.data

        data.x = data.x + data.sx * frames;
        data.y = data.y + data.sy * frames;

        if (data.x >= 500) {
            data.sx = -1
        }
        if (data.x <= 0) {
            data.sx = 1
        }

        if (data.y >= 100) {
            data.sy = -1
        }
        if (data.y <= -100) {
            data.sy = 1
        }

        //
        data.x = Math.round(data.x * 10) / 10
        data.y = Math.round(data.y * 10) / 10

        //
        data.sx = Math.round(data.sx * 1.05 * 1000) / 1000
        data.sy = Math.round(data.sy * 1.01 * 1000) / 1000
    }

    onStop() {
        let data = this.data

        //
        data.visible = false
    }
}

export default {

    components: {
        BallTest,
        ImagesFrameTest,
        MenuContainer, LogoGame, gameplay, animation
    },

    data() {
        return {
            animationID: null,
            globalAnimationTimeLast: null,
            animationTimerStep: 0,
            globalAnimationFrame: 0,

            animationsTimeLast: {},
            animations: {
                animation1: {
                    p: 0
                },
                animation2: {},
                animation3: {},
            },
        }
    },

    methods: {

        doStart() {
        },

        doStop() {
        },

        x11() {
            ctx.animation.animationStart("animation1", this.animations.animation1)
        },

        x11x() {
            ctx.animation.animationStart("animation3", this.animations.animation3)
            //ctx.animation.animationStop("animation1")
        },

        x22() {
            ctx.animation.animationStart("animation2", this.animations.animation2)
        },

        x22x() {
            ctx.animation.globalAnimationStop()
            //ctx.animation.animationStop("animation2")
        },

    },

    computed: {},

    async mounted() {
        ctx.animation.addAnimation(new Animation1(), "animation1")
        ctx.animation.addAnimation(new Animation2(), "animation2")
        ctx.animation.addAnimation(new Animation3(), "animation3")
    },

    unmounted() {
    },

}

</script>

<style lang="less" scoped>

.bar-xxx {
    background-color: red;
    height: 3em;
}

</style>