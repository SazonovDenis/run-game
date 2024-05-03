/**
 * Менеджер анимации.
 */
class AnimationManager {

    globalAnimationInterval = 10
    globalAnimationTimeLast = {}

    globalAnimationID = null
    globalAnimationFrame = 0

    animations = {}
    animationsTimeLast = {}


    addAnimation(animation, animationName) {
        if (animationName) {
            animation.name = animationName
        }

        //
        if (!animation.name) {
            throw new Error("animation.name is null")
        }

        //
        this.animations[animation.name] = animation
    }

    animationStart(animationName, data, cfg) {
        let animation = this.animations[animationName]

        //
        if (!animation) {
            return false
        }
        if (animation.active) {
            return false
        }

        // Чтобы кадры начались сначала
        this.setAnimationTimeLastDiff(animationName, null)

        //
        animation.start(data, cfg)

        //
        return true
    }

    animationRestart(animationName, data, cfg) {
        this.animationStop(animationName)
        this.animationStart(animationName, data, cfg)
    }

    animationStop(animationName) {
        let animation = this.animations[animationName]

        //
        if (!animation) {
            return false
        }
        if (!animation.active) {
            return false
        }

        //
        animation.stop()

        //
        return true
    }

    canAnimationStep(animationName, timeNow) {
        let animation = this.animations[animationName]

        if (!animation) {
            return false
        }
        if (!animation.active) {
            return false
        }

        //
        let animationsTimeDiff = this.getAnimationTimeLastDiff(animationName, timeNow)

        // Не запускалась - пора запускать первый раз
        if (!animationsTimeDiff) {
            return true
        }

        // Пора рисовать кадр?
        if (animationsTimeDiff >= animation.interval) {
            return true
        } else {
            return false
        }
    }

    setAnimationTimeLastDiff(animationName, timeNow) {
        this.animationsTimeLast[animationName] = timeNow
    }

    getAnimationTimeLastDiff(animationName, timeNow) {
        let animationsTimeLast = this.animationsTimeLast[animationName]

        //
        if (!animationsTimeLast) {
            return null
        }

        //
        let animationsTimeDiff = timeNow - animationsTimeLast
        return animationsTimeDiff
    }

    globalAnimationStart() {
        let globalAnimationTimeNow = window.performance.now();
        this.globalAnimationTimeLast = globalAnimationTimeNow
        this.globalAnimationFrame = 0

        //
        this.requestAnimationFrame()
    }

    globalAnimationStop() {
        window.cancelAnimationFrame(this.globalAnimationID)
        //
        this.globalAnimationID = null
    }

    requestAnimationFrame() {
        this.globalAnimationID = window.requestAnimationFrame(this.onGlobalAnimationFrame.bind(this))
    }

    onGlobalAnimationFrame() {
        let timeNow = window.performance.now();
        let globalAnimationDiff = timeNow - this.globalAnimationTimeLast
        if (globalAnimationDiff >= this.globalAnimationInterval) {
            this.globalAnimationTimeLast = timeNow
            this.globalAnimationFrame = this.globalAnimationFrame + 1

            //
            for (let animationName in this.animations) {
                try {
                    if (this.canAnimationStep(animationName, timeNow)) {
                        let animation = this.animations[animationName]

                        // Посчитаем количество кадров, которые должна пройти анимация
                        let frames = 1
                        let animationsTimeDiff = this.getAnimationTimeLastDiff(animationName, timeNow)
                        if (animationsTimeDiff) {
                            frames = animationsTimeDiff / animation.interval
                        }

                        //
                        //console.info("animation: " + animationName + ", frames: " + frames)
                        this.setAnimationTimeLastDiff(animationName, timeNow)
                        animation.step(frames)
                    }
                } catch(e) {
                    console.error("animation: " + animationName)
                    console.error(e)
                }
            }
        }

        //
        this.requestAnimationFrame()
    }


}

export default AnimationManager