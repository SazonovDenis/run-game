import ctx from "./gameplayCtx"
import AnimationManager from "./AnimationManager"

import TimerAnimation from "./animations/TimerAnimation"
import BallAnimation from "./animations/BallAnimation"
import TaskOptionsVisibleAnimation from "./animations/TaskOptionsVisibleAnimation"
import SpriteAnimation from "./animations/SpriteAnimation"

export default {

    init() {
        if (ctx.animation) {
            return
        }

        // Глобальный менеджер анимации
        ctx.animation = new AnimationManager()

        // Подключаем все анимации (пока вручную)
        ctx.animation.addAnimation(new TimerAnimation())
        ctx.animation.addAnimation(new BallAnimation())
        ctx.animation.addAnimation(new TaskOptionsVisibleAnimation())
        ctx.animation.addAnimation(new SpriteAnimation())

        // Стартуем
        ctx.animation.globalAnimationStart()
    },


}