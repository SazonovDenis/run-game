package run.game.dao.auth;

import jandcode.core.*;
import jandcode.core.auth.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;

import java.util.*;

/**
 * Реализация {@link AuthProcessor}.
 * <p>
 * Может аутентифицировать пользователей по имени/паролю.
 */
public class RgmAuthProcessor extends BaseComp implements AuthProcessor {

    public boolean isSupportedAuthToken(AuthToken authToken) {
        return authToken instanceof UserPasswdAuthToken;
    }

    public AuthUser login(AuthToken authToken) throws Exception {
        UserPasswdAuthToken token = (UserPasswdAuthToken) authToken;

        ModelService modelService = getApp().bean(ModelService.class);
        Mdb mdb = modelService.getModel().createMdb();
        mdb.connect();

        try {
            Usr_upd upd = mdb.create(Usr_upd.class);
            StoreRecord rec = upd.loadByLoginPassword(token.getUsername(), token.getPasswd());

            //
            if (rec == null) {
                throw new XErrorAuth(XErrorAuth.msg_invalid_user_passwd);
            }

            //
            return createAuthUser(mdb, rec.getLong("id"));

        } finally {
            mdb.disconnect();
        }
    }

    protected AuthUser createAuthUser(Mdb mdb, long idUsr) throws Exception {
        Map<String, Object> attrs = new LinkedHashMap<>();

        // ---
        // Информация о пользователе
        Usr_upd upd = mdb.create(Usr_upd.class);
        Map<String, Object> userInfo = upd.loadInfo(idUsr);

        //
        attrs.putAll(userInfo);

        //
        return new DefaultAuthUser(attrs);
    }

}