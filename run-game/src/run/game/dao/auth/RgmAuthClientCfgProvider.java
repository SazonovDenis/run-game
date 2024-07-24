package run.game.dao.auth;

import jandcode.commons.conf.*;
import jandcode.core.apx.auth.*;
import jandcode.core.auth.*;

/**
 * Наш вариант предоставления информации о пользователе для клиента.
 * Мы хотим по-своему отправлять атрибуты на клиента: перенесем данные
 * о контекстном пользователе с ключа
 * <p>
 * AuthAction.SESSION_KEY_CONTEXT_USER
 * (этим управляет run.game.dao.auth.AuthAction и ему удобно)
 * <p>
 * на ключ
 * "contextUser" (этим управляет клиент и ему удобно так)
 */
public class RgmAuthClientCfgProvider extends AuthClientCfgProvider {

    public void grabClientCfg(Conf cfg) throws Exception {
        super.grabClientCfg(cfg);

        //
        Conf userInfo = cfg.findConf("userInfo", true);
        AuthUser contextUser = (AuthUser) userInfo.get(AuthAction.SESSION_KEY_CONTEXT_USER);

        //
        if (contextUser != null) {
            userInfo.remove(AuthAction.SESSION_KEY_CONTEXT_USER);
            userInfo.put("contextUser", contextUser.getAttrs());
        } else {
            userInfo.remove("contextUser");
        }

    }
}
