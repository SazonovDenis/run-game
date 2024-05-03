import ctx from "./gameplayCtx"
import AnimationManager from "./AnimationManager"

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
        ctx.animation.addAnimation(new BallAnimation(), "BallAnimation")
        ctx.animation.addAnimation(new TaskOptionsVisibleAnimation(), "TaskOptionsVisibleAnimation")
        ctx.animation.addAnimation(new SpriteAnimation(), "SpriteAnimation")

        // Стартуем
        ctx.animation.globalAnimationStart()
    },


}