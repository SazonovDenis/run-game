package run.game.dao.auth;

import jandcode.commons.*;
import jandcode.core.apx.auth.*;
import jandcode.core.auth.*;
import jandcode.core.auth.std.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import jandcode.core.web.action.*;

import java.util.*;

/**
 * Action для аутентификации в run-game
 */
public class AuthAction extends BaseAction {

    public static final String SESSION_KEY_CONTEXT_USER = AuthConsts.class.getName() + ".CONTEXTUSER";

    /**
     *
     */
    public void login() throws Exception {
        ActionRequestUtils requestUtils = getReq();

        // Проверим пользователя
        String login = requestUtils.getParams().getString("login");
        String password = requestUtils.getParams().getString("password");
        //
        loginInternal(login, password);
    }

    /**
     *
     */
    public void loginContextUser() throws Exception {
        ActionRequestUtils requestUtils = getReq();

        // Проверим пользователя
        long usr = requestUtils.getParams().getLong("usr");
        //
        loginContextUserInternal(usr);
    }

    public void register() throws Exception {
        ActionRequestUtils requestUtils = getReq();

        // Добавим пользователя
        StoreRecord rec = insInternal();

        // Проверим пользователя
        String login = rec.getString("login");
        // Пароль не берем из insInternal() - он там будет зашифрован и нам бесполезен
        String password = requestUtils.getParams().getString("password");
        //
        loginInternal(login, password);
    }

    public void updUsr() throws Exception {
        ActionRequestUtils requestUtils = getReq();

        // Отредактируем пользователя
        StoreRecord rec = updInternal();

        // Обновим сессию пользователя
        String login = rec.getString("login");
        // Пароль не берем из updInternal() - он там будет зашифрован и нам бесполезен
        String password = requestUtils.getParams().getString("password");
        //
        loginInternal(login, password);
    }

    /**
     * logout
     */
    public void logout() throws Exception {
        ActionRequestUtils requestUtils = getReq();

        // Убираем пользователя из сессии
        requestUtils.getSession().put(AuthConsts.SESSION_KEY_USER, null);

        // Возвращаем в HTTP заголовках сигнал об окончании сессии
        requestUtils.getHttpRequest().getSession().invalidate();

        // Возвращаем пустые данные
        String s = UtJson.toJson(new HashMap());
        requestUtils.render(s);
    }

    public void logoutContextUser() throws Exception {
        // Убираем контекстного пользователя из сессии текущего пользователя
        ActionRequestUtils requestUtils = getReq();
        AuthUser authUser = (AuthUser) requestUtils.getSession().get(AuthConsts.SESSION_KEY_USER);
        authUser.getAttrs().put(AuthAction.SESSION_KEY_CONTEXT_USER, null);

        // Возвращаем пустые данные
        String s = UtJson.toJson(new HashMap());
        requestUtils.render(s);
    }


    private void loginInternal(String login, String password) throws Exception {
        ActionRequestUtils requestUtils = getReq();

        // Проверим пользователя
        AuthService authSvc = getApp().bean(AuthService.class);
        AuthUser authUser = authSvc.login(new DefaultUserPasswdAuthToken(login, password));

        // Записываем пользователя в сессию
        requestUtils.getSession().put(AuthConsts.SESSION_KEY_USER, authUser);

        // Читаем и возвращаем данные пользователя
        String s = UtJson.toJson(authUser.getAttrs());
        requestUtils.render(s);
    }

    private void loginContextUserInternal(long idUsr) throws Exception {
        // Информация о контекстном пользователе
        AuthUser contextUser;
        //
        ModelService modelService = getApp().bean(ModelService.class);
        Mdb mdb = modelService.getModel().createMdb();
        mdb.connect();
        try {
            Usr_upd upd = mdb.create(Usr_upd.class);
            Map<String, Object> mapUserInfo = upd.getDependentUserInfo(idUsr);
            contextUser = new DefaultAuthUser(mapUserInfo);
        } finally {
            mdb.disconnect();
        }

        // Записываем контекстного пользователя в сессию текущего пользователя
        ActionRequestUtils requestUtils = getReq();
        AuthUser authUser = (AuthUser) requestUtils.getSession().get(AuthConsts.SESSION_KEY_USER);
        authUser.getAttrs().put(AuthAction.SESSION_KEY_CONTEXT_USER, contextUser);

        // Возвращаем данные пользователя
        String s = UtJson.toJson(contextUser.getAttrs());
        requestUtils.render(s);
    }

    private StoreRecord insInternal() throws Exception {
        StoreRecord rec;

        ActionRequestUtils requestUtils = getReq();
        ModelService modelService = getApp().bean(ModelService.class);
        Mdb mdb = modelService.getModel().createMdb();
        mdb.connect();
        try {
            Usr_upd upd = mdb.create(Usr_upd.class);
            Map params = new HashMap(requestUtils.getParams());
            rec = upd.ins(params);
        } finally {
            mdb.disconnect();
        }

        return rec;
    }

    private StoreRecord updInternal() throws Exception {
        StoreRecord rec;

        ActionRequestUtils requestUtils = getReq();
        ModelService modelService = getApp().bean(ModelService.class);
        Mdb mdb = modelService.getModel().createMdb();
        mdb.connect();
        try {
            Usr_upd upd = mdb.create(Usr_upd.class);
            Map params = new HashMap(requestUtils.getParams());
            rec = upd.upd(params);
        } finally {
            mdb.disconnect();
        }

        return rec;
    }


}
