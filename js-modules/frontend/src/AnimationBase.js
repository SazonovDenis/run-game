import ctx from "./gameplayCtx"

class AnimationBase {

    /**
     * Как часто нужно вызывать шаг анимации
     * @type {number} миллисекунд
     */
    interval = 100

    /**
     * Если не указана в потомке - будет присвоена при регистрации (иначе ошибка)
     * @type {string}
     */
    name = null


    _active = false
    _data = null
    _cfg = {}

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
        console.info("animation start: " + this.name + ", data: ", data + ", cfg: ", cfg)
    }

    step(frames) {
        this.onStep(frames)
    }

    stop() {
        this._active = false
        //
        this.onStop()
        //
        if (this.cfg.afterStop) {
            this.cfg.afterStop()
        }
        //
        console.info("animation stop: " + this.name + ", data: ", this.data)
        //
        ctx.eventBus.emit("animation-stop", {
            animation: this.name, data: this.data
        })
    }

}

export default AnimationBase