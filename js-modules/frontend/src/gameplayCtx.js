import {reactive} from 'vue'

export default {

    /**
     * Не меняющиеся настройки приложения
     */
    settings: {
        ballWidth: 32,
        ballHeihth: 32,
        goalSize: 16,
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
                // Поле flag - хак реактивности. Это написано исключительно,
                // чтобы заставить перерисоваться при изменении
                flag: {},

                // Состояние раунда: иноформация о раунде
                game: {
                    id: null,
                    plan: null,
                    planText: null,
                    dbeg: null,
                    dend: null,
                    //
                    done: null,
                },

                // Состояние раунда: иноформация о ходе выполнения заданий в раунде
                tasksResult: {},

                // Состояние раунда: текущее задание
                gameTask: {
                    task: null,
                    taskOptions: null
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
                    taskSoundLoaded: false,
                },

                sprite: {
                    blow: {
                        stillCount: 9,
                    }
                }

            })

            return this.globalState
        }
    }

}