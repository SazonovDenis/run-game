package run.game.dao;

import jandcode.commons.*;
import jandcode.commons.error.*;
import jandcode.core.auth.*;
import jandcode.core.dbm.mdb.*;
import run.game.dao.auth.*;

public class RgmMdbUtils extends BaseMdbUtils {

    AuthService authSvc;

    public void setMdb(Mdb mdb) {
        super.setMdb(mdb);

        // получаем сервис auth
        authSvc = getApp().bean(AuthService.class);
    }

    public AuthUser getCurrentUsr() {
        AuthUser user = authSvc.getCurrentUser();
        return user;
    }

    public AuthUser getContextUsr() {
        // Берем контекстного пользователя из сессии текущего пользователя
        AuthUser authUser = authSvc.getCurrentUser();
        AuthUser contextUser = (AuthUser) authUser.getAttrs().get(AuthAction.SESSION_KEY_CONTEXT_USER);
        return contextUser;
    }

    public long getCurrentUsrId() {
        AuthUser user = getCurrentUsr();
        long id = UtCnv.toLong(user.getAttrs().getLong("id"));
        if (id == 0) {
            throw new XError("Пользователь не указан [" + RgmAppConst.RGM_USER_NOT_SET + "]");
        }
        return id;
    }

    public long getContextOrCurrentUsrId() {
        AuthUser contextUsr = getContextUsr();
        if (contextUsr == null) {
            return getCurrentUsrId();
        } else {
            long id = UtCnv.toLong(contextUsr.getAttrs().getLong("id"));
            return id;
        }
    }


}
