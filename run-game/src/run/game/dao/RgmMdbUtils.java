package run.game.dao;

import jandcode.commons.*;
import jandcode.commons.error.*;
import jandcode.core.auth.*;
import jandcode.core.dbm.mdb.*;

public class RgmMdbUtils extends BaseMdbUtils {

    AuthService authSvc;

    public void setMdb(Mdb mdb) {
        super.setMdb(mdb);

        // получаем сервис auth
        authSvc = getApp().bean(AuthService.class);
    }

    public AuthUser getCurrentUser() {
        AuthUser user = authSvc.getCurrentUser();
        return user;
    }

    public long getCurrentUserId() {
        AuthUser user = getCurrentUser();
        long id = UtCnv.toLong(user.getAttrs().getLong("id"));
        if (id == 0) {
            throw new XError("Пользователь не указан [RGM_USER_NOT_SET]");
        }
        return id;
    }

}
