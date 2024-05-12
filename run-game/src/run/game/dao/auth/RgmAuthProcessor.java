package run.game.dao.auth;

import jandcode.core.*;
import jandcode.core.auth.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import run.game.dao.link.*;
import run.game.util.*;

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
            return createAuthUser(mdb, rec.getLong("id"), rec.getString("login"), rec.getString("text"));

        } finally {
            mdb.disconnect();
        }
    }

    protected AuthUser createAuthUser(Mdb mdb, long idUsr, String login, String text) throws Exception {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("id", idUsr);
        attrs.put("login", login);
        attrs.put("text", text);

        // ---
        // Дополнительная информация о пользователе

        // План "Мои слова"
        Usr_upd upd = mdb.create(Usr_upd.class);
        long planDefault = upd.getPlanDefault(idUsr);
        attrs.put("planDefault", planDefault);

        // Неотвеченные запросы в друзья
        Link_list link_list = mdb.create(Link_list.class);
        Store stLinksToWaiting = link_list.loadLinksToWaiting(idUsr);
        attrs.put("linksToWait", DataUtils.storeToList(stLinksToWaiting));


        //
        return new DefaultAuthUser(attrs);
    }

}