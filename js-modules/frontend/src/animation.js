import ctx from "./gameplayCtx"
import AnimationManager from "run-game-frontend/src/AnimationManager"

export default {

    init() {
        if (ctx.animation) {
            return
        }

        // Инициализация контекста
        ctx.animation = new AnimationManager()
    },


}