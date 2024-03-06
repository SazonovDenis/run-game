import {apx} from '../vendor'

export default {

    /**
     * Информация о пользователе
     * @returns {*}
     */
    getUserInfo: function() {
        return apx.cfg.userInfo
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