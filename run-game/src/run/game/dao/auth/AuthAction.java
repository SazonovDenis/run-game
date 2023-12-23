package run.game.dao.auth;

import jandcode.commons.*;
import jandcode.core.apx.auth.*;
import jandcode.core.auth.*;
import jandcode.core.auth.std.*;
import jandcode.core.web.action.*;

import java.util.*;

/**
 * Action для аутентификации в run-game
 */
public class AuthAction extends BaseAction {

    /**
     * login
     */
    public void login() throws Exception {
        AuthService authSvc = getApp().bean(AuthService.class);
        ActionRequestUtils requestUtils = getReq();

        //
        String login = requestUtils.getParams().getString("login");
        String password = requestUtils.getParams().getString("password");

        // Проверим пользователя
        AuthUser authUser = authSvc.login(new DefaultUserPasswdAuthToken(login, password));
        // Записываем пользователя в сессию
        requestUtils.getSession().put(AuthConsts.SESSION_KEY_USER, authUser);

        // Читаем и возвращаем данные пользователя
        String s = UtJson.toJson(authUser.getAttrs());
        requestUtils.render(s);
    }

    /**
     * login
     */
    public void getUserInfo() throws Exception {
        AuthService authSvc = getApp().bean(AuthService.class);
        ActionRequestUtils requestUtils = getReq();

        // Читаем пользователя из сессии
        AuthUser authUser = authSvc.getCurrentUser();

        // Читаем и возвращаем данные пользователя
        String s = UtJson.toJson(authUser.getAttrs());
        requestUtils.render(s);
    }

    /**
     * logout
     */
    public void logout() throws Exception {
        AuthService authSvc = getApp().bean(AuthService.class);
        ActionRequestUtils requestUtils = getReq();

        // Убираем пользователя из сессии
        requestUtils.getSession().put(AuthConsts.SESSION_KEY_USER, null);

        // Возвращаем в HTTP заголовках сигнал об окончании сессии
        requestUtils.getHttpRequest().getSession().invalidate();

        // Возвращаем пустые данные
        String s = UtJson.toJson(new HashMap());
        requestUtils.render(s);
    }


}
