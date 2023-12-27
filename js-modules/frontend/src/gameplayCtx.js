import {reactive} from 'vue'

export default {

    /**
     * Не меняющиеся настройки приложения
     */
    settings: {
        animationInterval: 10,
        ballWidth: 32,
        ballHeihth: 32,
        goalSize: 16,
        minDl: 200,
        maxDl: 1000,
        valueGoalMax: 10,
        goalHitSizeDefault: 5,
        goalHitSizeHint: 2,
        goalHitSizeError: 1,
    },

    globalState: null,

    /**
     * Состояние игрового мира.
     * Глобальный контекст, созданный так, чтобы globalState был реактивным.
     */
    getGlobalState: function() {
        if (this.globalState) {
            return this.globalState
        } else {
            this.globalState = reactive({
                // Хак реактивности. Это написано исключительно,
                // чтобы заставить перерисоваться при изменении
                flag: {},

                // Иноформация о раунде
                game: {
                    id: null,
                    plan: null,
                    text: "не загружена",
                    countTotal: 0,
                    countDone: 0,
                    done: null,
                    dbeg: null,
                    dend: null,
                },

                // Задание
                gameTask: {
                    task: {},
                    taskOptions: {}
                },

                // Взаимодействие с пользователем и соответствующие анимации
                dataState: {
                    drag: {
                        dtStart: null,
                        dtStop: null,
                        drag: null,
                        x: null,
                        y: null,
                        sx: null,
                        sy: null,
                    },
                    goal: {
                        text: null,
                        value: null,
                    },
                    ball: {
                        text: null,
                        // Размер мяча
                        value: 0,
                        // Мяч в режиме "правильный ответ"
                        ballIsTrue: null,
                    },
                    mode: {
                        modeShowOptions: null,
                        goalHitSize: null,
                    },

                    showTaskHint: false,
                },

            })

            return this.globalState
        }
    }

}