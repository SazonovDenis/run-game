class AnimationBase {

    _active = false

    interval = 100

    data = {}

    get active() {
        return this._active;
    }

    onStart() {

    }

    onStep() {

    }

    onStop() {

    }

    start(data) {
        this.data = data
        this._active = true
        //
        this.onStart(data)
        //
        console.info("animation start", this.data)
    }

    step(timer) {
        this.onStep(timer)
    }

    stop() {
        this._active = false
        //
        this.onStop()
        //
        console.info("animation stop", this.data)
    }

}

export default AnimationBase