import AnimationBase from "../AnimationBase"

class TimerAnimation extends AnimationBase {

    interval = 100
    name = "TimerAnimation"

    onStart(data, cfg) {
        // Начнем
        this.value = 0
    }

    onStep(frames) {
        let data = this.data
        let cfg = this.cfg

        this.value = this.value + this.interval* frames

        if (this.value > cfg.duration) {
            this.stop()
            return
        }
    }

}


export default TimerAnimation