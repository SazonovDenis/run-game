import AnimationBase from "../AnimationBase"
import ctx from "../gameplayCtx"
import utilsCore from "../utils2D"

class BallAnimation extends AnimationBase {

    interval = 10

    minDl = 5
    maxDl = 50

    dx = 0
    dy = 0

    onStart(data, cfg) {
        // Скорость броска, пкс/сек
        let dx = 1000 * (cfg.x - cfg.sx) / cfg.duration
        let dy = 1000 * (cfg.y - cfg.sy) / cfg.duration

        // Скорость броска, пкс/кадр
        dx = dx * this.interval / 1000
        dy = dy * this.interval / 1000

        // Просто клик - пусть летит вверх
        if (dx === 0 && dy === 0) {
            dy = -1
        }

        // Чтобы  летело не слишком...
        let dl = Math.sqrt(dx * dx + dy * dy)
        // ... не слишком медленно...
        if (dl < this.minDl) {
            dx = dx * this.minDl / dl
            dy = dy * this.minDl / dl
        }
        // ... и не слишком быстро
        if (dl > this.maxDl) {
            dx = dx * this.maxDl / dl
            dy = dy * this.maxDl / dl
        }

        //
        this.dx = dx
        this.dy = dy

        //
        data.value = 1
        data.x = cfg.x
        data.y = cfg.y
    }

    onStep(frames) {
        let data = this.data
        let cfg = this.cfg

        // Шаг
        data.x = data.x + this.dx * frames
        data.y = data.y + this.dy * frames


        // ---
        // Попали в цель?

        // Пересечения определяем как пересечение отрезка, проведенного
        // от предыдущей точки движения до текущей с прямоугольником цели.
        // Так мы не дадим "проскочить" снаряду сквозь цель
        // при слишком большой скорости движения.
        let rectTrace = {
            x1: data.x,
            x2: data.x - this.dx,
            y1: data.y,
            y2: data.y - this.dy,
        }
        if (utilsCore.intersectRect(rectTrace, cfg.rectGoal)) {
            data.result = {
                goal: true,
                x: data.x,
                y: data.y,
            }

            this.stop()
        }


        // ---
        // Выход шарика за границы экрана?
        if (data.x + ctx.settings.ballWidth > innerWidth || data.x < 0 || data.y + ctx.settings.ballHeihth > innerHeight || data.y < 0) {
            data.result = {
                goal: false,
                x: data.x,
                y: data.y,
            }

            this.stop()
        }


        // ---
        // Продолжение движения - рост или уменьшение шарика по ходу движения
        if (!cfg.ballIsTrue) {
            data.value = data.value - 0.04 * frames
        } else {
            data.value = data.value + 0.02 * frames
        }

        // Ограничение размера шарика
        if (data.value > 5) {
            data.value = 5
        }

        // Шарик кончился - прекращаем полет шарика
        if (data.value <= 0) {
            data.value = 0

            //
            data.result = {
                goal: false,
                x: data.x,
                y: data.y,
            }

            this.stop()
        }
    }

    onStop() {
        let data = this.data

        data.value = 0
    }

}

export default BallAnimation
