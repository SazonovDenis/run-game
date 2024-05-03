import AnimationBase from "../AnimationBase"

class SpriteAnimation extends AnimationBase {

    interval = 100
    name = "SpriteAnimation"

    onStart(data, cfg) {
        // Важная деталь: количество кадров stillCount записано в data.stillCount,
        // а не передается в cfg, потому что количество кадров храним в globalState.sprite
        cfg.stillCount = data.stillCount

        // Начнем
        data.still = 0
    }

    onStep(frames) {
        let data = this.data
        let cfg = this.cfg

        data.still = data.still + Math.round(frames)

        if (data.still > cfg.stillCount) {
            this.stop()
            return
        }
    }

    onStop() {
        let data = this.data

        data.still = 0
    }


}


export default SpriteAnimation