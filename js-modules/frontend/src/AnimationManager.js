class AnimationManager {

    animationID = null
    globalAnimationFrame = 0

    animationsTimeLast = {}
    animations = {}

    addAnimation(animationName, animation) {
        this.animations[animationName] = animation
    }

    animationStart(animationName, data) {
        let animation = this.animations[animationName]

        //
        if (!animation) {
            return false
        }
        if (animation.active) {
            return false
        }

        //
        console.info("animation start: ", animationName)
        animation.start(data)

        //
        return true
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
        console.info("animation stop: " + animationName)
        animation.stop()

        //
        return true
    }

    canAnimationStep(animationName) {
        let animation = this.animations[animationName]

        if (!animation) {
            return false
        }
        if (!animation.active) {
            return false
        }

        //
        let animationTimeNow = window.performance.now();
        let animationsTimeLast = this.animationsTimeLast[animationName]
        let animationsTimeDiff = animationTimeNow - animationsTimeLast

        //
        if (!animationsTimeLast) {
            this.animationsTimeLast[animationName] = animationTimeNow
            return true
        }

        //
        if (animationsTimeDiff >= animation.interval) {
            this.animationsTimeLast[animationName] = animationTimeNow
            return true
        }

        //
        return false;
    }


    globalAnimationStart() {
        this.animationID = window.requestAnimationFrame(this.onGlobalAnimationFrame.bind(this))
    }

    globalAnimationStop() {
        window.cancelAnimationFrame(this.animationID)
        this.animationID = null
    }

    onGlobalAnimationFrame() {
        for (let animationName in this.animations) {
            if (this.canAnimationStep(animationName)) {
                let animation = this.animations[animationName]
                //console.info("animation", animationName, "data", animation.data)

                //
                animation.step()
            }
        }

        //
        this.globalAnimationFrame = this.globalAnimationFrame + 1

        //
        this.globalAnimationStart()
    }


}

export default AnimationManager