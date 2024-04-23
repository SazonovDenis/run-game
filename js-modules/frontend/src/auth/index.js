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
    }

}