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
        let ui = this.getUserInfo()
        if (!ui || ui.guest) {
            return false
        } else {
            return true
        }
    }

}