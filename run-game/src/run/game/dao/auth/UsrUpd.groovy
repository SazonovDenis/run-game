package run.game.dao.auth

import jandcode.commons.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

public class UsrUpd extends RgmMdbUtils {


    Rnd rnd = new RndImpl()

    public static final String msg_user_exists = "Пользователь уже зарегистрирован"
    public static final String msg_is_required_text = "Указание имени обязательно"
    public static final String msg_is_required_login = "Указание логина обязательно"

    @DaoMethod
    public StoreRecord ins(Map params) {
        // --- Проверки

        // Нет такого пользователя?
        String login = params.get("login")
        StoreRecord rec = loadByLogin(login)
        //
        if (rec != null) {
            mdb.validateErrors.addError(msg_user_exists)
        }

        // Обязательные поля
        if (UtCnv.isEmpty(params.get("text"))) {
            mdb.validateErrors.addError(msg_is_required_text)
        }

        //
        mdb.validateErrors.checkErrors()


        // --- Значения по умолчанию

        // Логин
        if (!params.containsKey("login")) {
            login = rnd.text("0123456789abcdef", 32, 32, 32)
            params.put("login", login)
        }

        // Маскировка password
        String password = params.get("password")
        if (!UtCnv.isEmpty(password)) {
            password = UtString.md5Str(password)
            params.put("password", password)
        }


        // --- Запись
        long id = mdb.insertRec("Usr", params)


        // ---
        return loadRec(id)
    }

    StoreRecord loadRec(long id) {
        StoreRecord rec = mdb.loadQueryRecord(
                "select id, login, text from Usr where id = :id",
                ["id": id],
                false
        )

        //
        return rec
    }

    StoreRecord loadByLoginPassword(String login, String password) {
        // --- Обязательные поля

        // Логин
        if (UtCnv.isEmpty(login)) {
            mdb.validateErrors.addError(msg_is_required_login)
        }

        //
        mdb.validateErrors.checkErrors()


        // --- Значения по умолчанию

        // Маскировка password
        if (!UtCnv.isEmpty(password)) {
            password = UtString.md5Str(password)
        }


        // ---
        String sql
        if (!UtCnv.isEmpty(password)) {
            sql = "select id, login, text from Usr where login = :login and password = :password"
        } else {
            sql = "select id, login, text from Usr where login = :login and password is null"
        }
        StoreRecord rec = mdb.loadQueryRecord(sql, ["login": login, "password": password], false)

        //
        return rec
    }

    StoreRecord loadByLogin(String login) {
        StoreRecord rec = mdb.loadQueryRecord(
                "select id, login, text from Usr where login = :login",
                [
                        "login": login,
                ],
                false
        )

        //
        return rec
    }


}
