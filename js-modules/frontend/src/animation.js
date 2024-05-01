import ctx from "./gameplayCtx"
import AnimationManager from "./AnimationManager"

import AnimationBall from "./animations/AnimationBall"

export default {

    init() {
        if (ctx.animation) {
            return
        }

        // Глобальный менеджер анимации
        ctx.animation = new AnimationManager()

        // Подключаем все анимации (пока вручную)
        ctx.animation.addAnimation(new AnimationBall(), "ball")

        // Стартуем
        ctx.animation.globalAnimationStart()
    },


}