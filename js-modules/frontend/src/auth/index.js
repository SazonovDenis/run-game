import {apx} from '../vendor'

export default {

    /**
     * Информация о пользователе
     * @returns {*}
     */
    getUserInfo: function() {
        let userInfo = apx.cfg.userInfo

        // Чтобы правильно вызывалось api, иначе получается не та ошибка,
        // которая на самом деле (пользователь не зарегистрирован),
        // а ошибка передачи параметров в api
        if (!userInfo.planDefault) {
            userInfo.planDefault = 0
        }

        // Где-то после AuthServiceImpl.setCurrentUser поле UserInfo.linksToWait
        // превращается из массива (с ключами 1, 2, ...) в map с ключами #1, #2, ...
        // Поэтому превратим обратно из
        //   "#0": {...},
        //   "#1": {...}
        // в обычный массив
        //   0: {...},
        //   1: {...}
        let linksToWait = []
        for (let key in userInfo.linksToWait) {
            linksToWait.push(userInfo.linksToWait[key])
        }
        userInfo.linksToWait = linksToWait


        //
        return userInfo
    },

    /**
     * Аутентифицирован ли пользователь
     */
    isAuth: function() {
        let userInfo = this.getUserInfo()
        if (!userInfo || userInfo.guest) {
            return false
        } else {
            return true
        }
    },

    /**
     * Информация о пользователе
     * @returns {*}
     */
    getContextUserInfo: function() {
        let userInfo = apx.cfg.userInfo

        if (userInfo.contextUser) {
            userInfo = userInfo.contextUser
        }

        // Чтобы правильно вызывалось api, иначе получается не та ошибка,
        // которая на самом деле (пользователь не зарегистрирован),
        // а ошибка передачи параметров в api
        if (!userInfo.planDefault) {
            userInfo.planDefault = 0
        }

        return userInfo
    },

}