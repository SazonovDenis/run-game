import {reactive} from 'vue'
import auth from "./auth"

import {initQiasarInstance} from './quasar-instance'


export default {

    globalState: null,

    /**
     * Состояние игрового мира.
     * Глобальный контекст, созданный так, чтобы globalState был реактивным.
     */
    getGlobalState: function() {
        if (this.globalState) {
            return this.globalState
        } else {
            //
            const quasar = initQiasarInstance()
            Jc.cfg.is = quasar.platform.is

            //
            let helpState = {}
            let filterSettings = {}
            let userInfo = auth.getUserInfo()
            if (userInfo.settings) {
                helpState = userInfo.settings.helpState
                filterSettings = userInfo.settings.filterSettings
            }
            if (!helpState) {
                helpState = {}
            }
            if (!filterSettings) {
                filterSettings = {}
            }

            //
            this.globalState = reactive({
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

                // Информация о спрайтах, в т.ч. метаинформация.
                // Надо вообще-то метаинформацию (stillCount) вынести отдельно,
                // потом сделаем общий репозитарий спрайтов с метаинформацией о каждом,
                // чтобы бралось автоматом из графических файлов
                sprite: {
                    blow: {
                        stillCount: 9,
                    }
                },

                // Показанные или скрытые пункты помощи
                helpState: helpState,

                // Конфигурация разных фильтров
                filterSettings: filterSettings,
            })

            return this.globalState
        }
    }

}