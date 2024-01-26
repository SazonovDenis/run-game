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
            UsrUpd upd = mdb.create(UsrUpd.class);
            StoreRecord rec = upd.loadByLoginPassword(token.getUsername(), token.getPasswd());

            //
            if (rec == null) {
                throw new XErrorAuth(XErrorAuth.msg_invalid_user_passwd);
            }

            //
            return createAuthUser(rec.getLong("id"), rec.getString("login"), rec.getString("text"));

        } finally {
            mdb.disconnect();
        }
    }

    protected AuthUser createAuthUser(long id, String login, String text) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("id", id);
        attrs.put("login", login);
        attrs.put("text", text);

        return new DefaultAuthUser(attrs);
    }

}