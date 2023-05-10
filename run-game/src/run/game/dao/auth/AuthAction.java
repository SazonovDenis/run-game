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
        String name = requestUtils.getParams().getString("name");
        String password = requestUtils.getParams().getString("password");
        //
        AuthUser authUser = authSvc.login(new DefaultUserPasswdAuthToken(name, password));
        //
        requestUtils.getSession().put(AuthConsts.SESSION_KEY_USER, authUser);
        //
        String s = UtJson.toJson(authUser.getAttrs());
        requestUtils.render(s);
    }

    /**
     * login
     */
    public void getUserInfo() throws Exception {
        AuthService authSvc = getApp().bean(AuthService.class);
        ActionRequestUtils requestUtils = getReq();
        //
        AuthUser authUser = authSvc.getCurrentUser();
        //
        String s = UtJson.toJson(authUser.getAttrs());
        requestUtils.render(s);
    }

    /**
     * logout
     */
    public void logout() throws Exception {
        AuthService authSvc = getApp().bean(AuthService.class);
        ActionRequestUtils requestUtils = getReq();
        //
        requestUtils.getSession().put(AuthConsts.SESSION_KEY_USER, null);
        requestUtils.getHttpRequest().getSession().invalidate();
        //
        String s = UtJson.toJson(new HashMap());
        requestUtils.render(s);
    }


}
