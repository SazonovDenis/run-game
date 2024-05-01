class AnimationBase {

    _active = false

    interval = 100

    data = null
    cfg = {}

    get active() {
        return this._active;
    }

    onStart(data, cfg) {

    }

    onStep(frames) {

    }

    onStop() {

    }

    start(data, cfg) {
        this.data = data
        this.cfg = cfg
        this._active = true
        //
        this.onStart(data, cfg)
        //
        console.info("animation start, data:", data, "cfg:", cfg)
    }

    step(frames) {
        this.onStep(frames)
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