package run.game.dao.auth;

import jandcode.core.*;
import jandcode.core.auth.*;

import java.util.*;

/**
 * Заглушка для {@link AuthProcessor}.
 * <p>
 * Может аутентифицировать пользователей по имени/паролю:
 * admin/111, user1/111, user2/111.
 */
public class DummyAuthProcessor extends BaseComp implements AuthProcessor {

    public boolean isSupportedAuthToken(AuthToken authToken) {
        return authToken instanceof UserPasswdAuthToken;
    }

    public AuthUser login(AuthToken authToken) throws Exception {
        UserPasswdAuthToken token = (UserPasswdAuthToken) authToken;

        if ("admin".equals(token.getUsername()) && "111".equals(token.getPasswd())) {
            return createUser(1001, token.getUsername());

        } else if ("user1".equals(token.getUsername()) && "111".equals(token.getPasswd())) {
            return createUser(1001, token.getUsername());

        } else if ("user2".equals(token.getUsername()) && "222".equals(token.getPasswd())) {
            return createUser(1002, token.getUsername());

        } else {
            throw new XErrorAuth(XErrorAuth.msg_invalid_user_passwd);
        }
    }

    protected AuthUser createUser(long id, String name) {
        Map<String, Object> attrs = new LinkedHashMap<>();
        attrs.put("id", id);
        attrs.put("login", name);

        if ("admin".equals(name)) {
            attrs.put("name", "Администратор");
            attrs.put("color", "red");

        } else if ("user1".equals(name)) {
            attrs.put("name", "Петя");
            attrs.put("color", "yellow");

        } else if ("user2".equals(name)) {
            attrs.put("name", "Вася");
            attrs.put("color", "green");
        }

        return new DefaultAuthUser(attrs);
    }

}