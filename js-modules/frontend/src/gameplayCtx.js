import {reactive} from 'vue'

import {initQiasarInstance} from './quasar-instance'
import auth from "run-game-frontend/src/auth"


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

            // Создаем глобальный контекст (общую кучу данных)
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
                helpState: {},

                // Конфигурация разных фильтров
                viewSettings: {},
            })


            // Заберем userInfo.settings в глобальную кучу
            this.applyUserInfoSetting()


            //
            return this.globalState
        }
    },


    applyUserInfoSetting: function() {
        console.info("applyUserInfoSetting")

        if (!this.globalState) {
            console.warn("this.globalState is null")
            return
        }

        //
        let userInfo = auth.getUserInfo()
        let userInfoSettings = userInfo.settings
        if (!userInfoSettings) {
            userInfoSettings = {}
        }

        //
        this.globalState.helpState = userInfoSettings.helpState
        this.globalState.viewSettings = userInfoSettings.viewSettings

        //
        if (!this.globalState.helpState) {
            this.globalState.helpState = {}
        }
        if (!this.globalState.viewSettings) {
            this.globalState.viewSettings = {}
        }

        console.info("applyUserInfoSetting - ok")

    }

}