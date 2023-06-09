package run.game.dao.auth;

import jandcode.commons.*;
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

        ModelService mdbs = getApp().bean(ModelService.class);
        Mdb mdb = mdbs.getModel().createMdb();
        mdb.connect();

        try {
            StoreRecord rec = mdb.loadQueryRecord("select id, login, text from Usr where login = :login and (password = :password or password is null)",
                    UtCnv.toMap(
                            "login", token.getUsername(),
                            "password", UtString.md5Str(token.getPasswd())
                    ),
                    false
            );

            //
            if (rec == null) {
                throw new XErrorAuth(XErrorAuth.msg_invalid_user_passwd);
            }

            //
            return createUser(rec.getLong("id"), rec.getString("login"), rec.getString("text"));

        } finally {
            mdb.disconnect();
        }
    }

    protected AuthUser createUser(long id, String login, String text) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("id", id);
        attrs.put("login", login);
        attrs.put("text", text);

        if ("user1010".equals(login)) {
            attrs.put("color", "red");

        } else if ("user1011".equals(login)) {
            attrs.put("color", "yellow");

        } else if ("user1012".equals(login)) {
            attrs.put("color", "green");

        } else {
            attrs.put("color", "black");
        }

        return new DefaultAuthUser(attrs);
    }

}