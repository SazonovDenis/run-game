import ctx from "./gameplayCtx"

class AnimationBase {

    interval = 100

    _active = false
    _data = null
    _cfg = {}

    get name() {
        return this.constructor.name;
    }

    get active() {
        return this._active;
    }

    get data() {
        return this._data;
    }

    get cfg() {
        return this._cfg;
    }

    onStart(data, cfg) {

    }

    onStep(frames) {

    }

    onStop() {

    }

    /**
     * Начать анимацию
     * @param data переменные, которые должна анимировать анимация
     * @param cfg Параметрыы анимации.
     *            Системные: onStop - вызвать после остановки
     */
    start(data, cfg) {
        this._data = data
        //
        if (!cfg) {
            cfg = {}
        }
        this._cfg = cfg
        //
        this._active = true
        //
        this.onStart(data, cfg)
        //
        console.info("animation start: " + this.name + ", data: " + data + ", cfg: " + cfg)
    }

    step(frames) {
        this.onStep(frames)
    }

    stop() {
        this._active = false
        //
        this.onStop()
        //
        if (this.cfg.onStop) {
            this.cfg.onStop()
        }
        //
        console.info("animation stop: " + this.name + ", data: " + this.data)
        //
        ctx.eventBus.emit("animation-stop", {
            animation: this.name, data: this.data
        })
    }

}

export default AnimationBase