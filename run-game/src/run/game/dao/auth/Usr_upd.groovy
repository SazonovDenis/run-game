package run.game.dao.auth

import jandcode.commons.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.dao.link.*
import run.game.util.*

public class Usr_upd extends RgmMdbUtils {


    Rnd rnd = new RndImpl()

    public static final String msg_user_exists = "Пользователь уже зарегистрирован"
    public static final String msg_user_not_exists = "Такой пользователь не зарегистрирован"
    public static final String msg_is_required_text = "Указание имени обязательно"
    public static final String msg_is_required_login = "Указание логина обязательно"

    @DaoMethod
    public Map<String, Object> getCurrentUserInfo() {
        long idUsr = getCurrentUsrId()
        return loadInfo(idUsr)
    }

    @DaoMethod
    public Map<String, Object> getDependentUserInfo(idUsr) {
        // Проверим, что пользователь idCurrentUsr имеет право запрашивать данные для idUsr
        long idCurrentUsr = getCurrentUsrId()
        Link_list link_list = getMdb().create(Link_list.class);
        StoreRecord recUsrDependent = link_list.loadLinkDependent(idCurrentUsr, idUsr)
        if (recUsrDependent == null) {
            throw new Exception("Вы не имеете доступа к данным этого пользователя");
        }

        //
        return loadInfo(idUsr)
    }

    @DaoMethod
    public Map<String, Object> getUserPublicInfo(long idUsr) {
        Map<String, Object> userInfo = new HashMap<>()

        //
        getCurrentUsrId()

        //
        StoreRecord rec = loadRec(idUsr)
        userInfo.put("id", rec.getLong("id"))
        userInfo.put("text", rec.getString("text"))

        //
        return userInfo
    }

    @DaoMethod
    public StoreRecord ins(Map params) {
        // --- Проверки

        // Уже есть такой пользователь?
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
        if (UtCnv.isEmpty(login)) {
            login = rnd.text("0123456789abcdef", 32, 32, 32)
            params.put("login", login)
        }

        // Маскировка password
        String password = params.get("password")
        if (!UtCnv.isEmpty(password)) {
            password = UtString.md5Str(password)
            params.put("password", password)
        } else {
            password = null
            params.put("password", password)
        }


        // --- Запись
        long idUsr = mdb.insertRec("Usr", params)


        // --- Создадим окружение пользователя

        // План "Мои слова"
        Plan_upd planUpd = mdb.create(Plan_upd)
        planUpd.insPlanInternal(
                [text: "Мои слова", isPublic: false],
                [],
                [[tag: RgmDbConst.Tag_plan_access_default]],
                [usr: idUsr, isOwner: true]
        )


        // ---
        return loadRec(idUsr)
    }

    long loadPlanDefault(long idUsr) {
        String sql =
                """
select
    PlanTag_access_default.plan
from 
    UsrPlan
    join PlanTag PlanTag_access_default on (
        UsrPlan.usr = :usr and
        UsrPlan.plan = PlanTag_access_default.plan and
        PlanTag_access_default.tag = $RgmDbConst.Tag_plan_access_default
    ) 
where
    UsrPlan.isOwner = 1
"""
        long planDefault = mdb.loadQueryRecord(sql, [usr: idUsr, isDefault: true]).getLong("plan");
        return planDefault
    }

    @DaoMethod
    public StoreRecord upd(Map params) {
        // --- Проверки
        long idUsr = getCurrentUsrId()

        // Нет такого пользователя?
        String login = params.get("login")
        StoreRecord rec = loadByLogin(login)
        //
        if (rec != null && rec.getLong("id") != idUsr) {
            mdb.validateErrors.addError(msg_user_not_exists)
        }

        // Обязательные поля
        if (UtCnv.isEmpty(params.get("text"))) {
            mdb.validateErrors.addError(msg_is_required_text)
        }

        if (UtCnv.isEmpty(params.get("login"))) {
            mdb.validateErrors.addError(msg_is_required_login)
        }

        //
        mdb.validateErrors.checkErrors()


        // --- Значения по умолчанию

        // Маскировка password
        String password = params.get("password")
        if (!UtCnv.isEmpty(password)) {
            password = UtString.md5Str(password)
            params.put("password", password)
        } else {
            password = null
            params.put("password", password)
        }

        // --- Запись
        params.put("id", idUsr)
        mdb.updateRec("Usr", params)


        // ---
        return loadRec(idUsr)
    }

    @DaoMethod
    public void updSettings(Map settings) {
        //
        long idUsr = getCurrentUsrId()

        // Настройки в BLOB
        String valueSettings = UtJson.toJson(settings)

        // Запись
        mdb.updateRec("Usr", [id: idUsr, settings: valueSettings])
    }

    StoreRecord loadRec(long idUsr) {
        StoreRecord rec = mdb.loadQueryRecord(
                "select id, login, text, settings from Usr where id = :id",
                ["id": idUsr],
                false
        )

        //
        return rec
    }

    public Map<String, Object> loadInfo(long idUsr) {
        Map<String, Object> userInfo = new HashMap<>()

        //
        StoreRecord rec = loadRec(idUsr)
        userInfo.putAll(rec.getValues())

        // ---
        // Дополнительная информация
        Link_list link_list = getMdb().create(Link_list.class);

        // План по умолчанию
        long planDefault = loadPlanDefault(idUsr)
        userInfo.put("planDefault", planDefault)

        // Неотвеченные запросы в друзья
        Store stLinksToWaiting = link_list.loadLinksToWaiting(idUsr)
        userInfo.put("linksToWait", DataUtils.storeToList(stLinksToWaiting))

        // Зависимые пользователи
        Store stLinksDependent = link_list.loadLinksDependent(idUsr)
        userInfo.put("linksDependent", DataUtils.storeToList(stLinksDependent))

        // Извлекаем настройки из BLOB
        Map settings = UtJson.fromJson(new String(rec.getValue("settings"), "utf-8"))
        userInfo.put("settings", settings)

        //
        return userInfo
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
        } else {
            password = null
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
