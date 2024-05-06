package run.game.dao.link

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Link_upd extends RgmMdbUtils {


    /**
     * Отправить исходящий запрос
     * @param mapLink
     */
    @DaoMethod
    long request(Map mapLink) {
        StoreRecord recLink = mdb.createStoreRecord("Link", mapLink)

        //
        long usr = getCurrentUserId()
        long usrTo = recLink.getLong("usrTo")
        long linkType = recLink.getLong("linkType")
        XDateTime dt = XDateTime.now()

        // Получатель существует?
        StoreRecord recUsrLink = mdb.loadQueryRecord("select * from Usr where id = :usrTo", [usrTo: usrTo])
        if (recUsrLink == null) {
            throw new XError("Пользователь не существует.")
        }

        // Отправитель в списке заблокированных?
        StoreRecord recMeIsLocked = mdb.loadQueryRecord(sqlMeIsLocked(), [usrFrom: usr, usrTo: usrTo, dt: dt], false)
        if (recMeIsLocked != null) {
            throw new XError("Вы не можете отправлять запросы пользователю, т.к. пользователь вас заблокировал.")
        }

        // Уже есть исходящий запрос?
        StoreRecord recLinkNow = mdb.loadQueryRecord(sqlWaitingLink(), [usrFrom: usr, usrTo: usrTo], false)
        if (recLinkNow != null) {
            throw new XError("Вы не можете отправлять запросы пользователю, т.к. уже отправили ему запрос. Дождитель ответа или отмените предыдущий запрос.")
        }

        // Отправляем запрос
        StoreRecord recLinkNew = createLinkOutcoming()
        recLinkNew.setValue("linkType", linkType)

        //
        mdb.insertRec("Link", recLinkNew)

        //
        return 0
    }

    /**
     * Принять входящий запрос
     * @param mapLink
     */
    @DaoMethod
    void accept(Map mapLink) {
        StoreRecord recLink = loadIncomingLink(mapLink)


        // Обновим входящий запрос
        recLink.setValue("confirmState", RgmDbConst.ConfirmState_accepted)
        mdb.updateRec("Link", recLink)


        // Отреагируем на принятие запроса
        StoreRecord recLinkNew = createLinkOutcoming_BackToIncoming(recLink)

        //
        long linkType = recLink.getLong("linkType")
        switch (linkType) {
            case RgmDbConst.LinkType_friend: {
                // Добавим отправителя в друзья
                recLinkNew.setValue("linkType", RgmDbConst.LinkType_friend)
                break
            }
            case RgmDbConst.LinkType_parent: {
                // Добавим отправителя в родителей
                recLinkNew.setValue("linkType", RgmDbConst.LinkType_child)
                break
            }
            case RgmDbConst.LinkType_child: {
                // Добавим отправителя в детей
                recLinkNew.setValue("linkType", RgmDbConst.LinkType_parent)
                break
            }
            case RgmDbConst.LinkType_student: {
                // Добавим отправителя в родителей
                recLinkNew.setValue("linkType", RgmDbConst.LinkType_teacher)
                break
            }
            case RgmDbConst.LinkType_teacher: {
                // Добавим отправителя в родителей
                recLinkNew.setValue("linkType", RgmDbConst.LinkType_student)
                break
            }
            default: {
                throw new XError("Запрос неправильного типа")
            }

        }

        //
        mdb.insertRec("Link", recLinkNew)
    }

    /**
     * Отклонить входящий запрос
     * @param mapLink
     */
    @DaoMethod
    void refuse(Map mapLink) {
        StoreRecord recLink = loadIncomingLink(mapLink)

        // Обновим входящий запрос
        recLink.setValue("confirmState", RgmDbConst.ConfirmState_refused)
        mdb.updateRec("Link", recLink)
    }

    /**
     * Отменить свой запрос
     * @param mapLink
     */
    @DaoMethod
    void cancel(Map mapLink) {
        StoreRecord recLink = loadOutcomingLink(mapLink)

        // Обновим исходящий запрос
        recLink.setValue("confirmState", RgmDbConst.ConfirmState_cancelled)
        mdb.updateRec("Link", recLink)
    }

    /**
     * Заблокировать пользователя
     * @param usrTo
     */
    @DaoMethod
    void usrBlock(long usrTo) {
        StoreRecord recLinkNew = createLinkOutcoming()

        //
        recLinkNew.setValue("usrTo", usrTo)
        recLinkNew.setValue("linkType", RgmDbConst.LinkType_blocked)
        recLinkNew.setValue("confirmState", RgmDbConst.ConfirmState_accepted)

        //
        mdb.insertRec("Link", recLinkNew)
    }

    /**
     * Разблокировать пользователя
     * @param usrTo
     */
    @DaoMethod
    void usrUnblock(long usrTo) {
        StoreRecord recLink = loadLinkBlocked(usrTo)

        XDateTime dt = XDateTime.now()
        recLink.setValue("dend", dt)

        mdb.updateRec("Link", recLink)
    }


    /**
     * Возвращает Link, которая будет исходящим запросом
     *
     * @return исходящий запрос
     */
    StoreRecord createLinkOutcoming() {
        long usr = getCurrentUserId()
        XDateTime dt = XDateTime.now()

        StoreRecord recLinkBack = mdb.createStoreRecord("Link")
        recLinkBack.setValue("usrFrom", usr)
        recLinkBack.setValue("dbeg", dt)
        recLinkBack.setValue("dend", UtDateTime.EMPTY_DATETIME_END)
        recLinkBack.setValue("confirmState", RgmDbConst.ConfirmState_waiting)

        return recLinkBack
    }

    /**
     * Возвращает Link, которая будет будет исходящим запросом и
     * смотреть в обратную сторону от входящего запроса.
     * Заполняются поля: usrTo
     *
     * @param recLinkIncoming входящий запрос
     * @return исходящий запрос в ответ на входящий
     */
    StoreRecord createLinkOutcoming_BackToIncoming(StoreRecord recLinkIncoming) {
        StoreRecord recLinkBack = createLinkOutcoming()

        long usrTo = recLinkIncoming.getLong("usrFrom")
        recLinkBack.setValue("usrTo", usrTo)
        recLinkBack.setValue("confirmState", RgmDbConst.ConfirmState_accepted)

        return recLinkBack
    }

    /**
     * Загружаем входящий к нам неотвеченный запрос
     * @param mapLink
     * @return
     */
    StoreRecord loadIncomingLink(Map mapLink) {
        StoreRecord recLink = mdb.createStoreRecord("Link", mapLink)

        //
        long usr = getCurrentUserId()
        long usrTo = recLink.getLong("usrTo")

        // Загрузим входящий запрос
        mdb.loadQueryRecord(recLink, sqlWaitingLink(), [usrFrom: usrTo, usrTo: usr])

        //
        return recLink
    }

    /**
     * Загружаем свой исходящий неотвеченный запрос
     * @param mapLink
     * @return
     */
    StoreRecord loadOutcomingLink(Map mapLink) {
        StoreRecord recLink = mdb.createStoreRecord("Link", mapLink)

        //
        long usr = getCurrentUserId()
        long usrTo = recLink.getLong("usrTo")

        // Загрузим входящий запрос
        mdb.loadQueryRecord(recLink, sqlWaitingLink(), [usrFrom: usr, usrTo: usrTo])

        //
        return recLink
    }

    /**
     * Загружаем запись о блокировке пользователя
     */
    StoreRecord loadLinkBlocked(long usrTo) {
        StoreRecord recLink = mdb.createStoreRecord("Link")

        //
        long usr = getCurrentUserId()
        XDateTime dt = XDateTime.now()

        // Загрузим запись о блокировке
        mdb.loadQueryRecord(recLink, sqlLinkBlocked(), [usrFrom: usr, usrTo: usrTo, dt: dt])

        //
        return recLink
    }


    String sqlMeIsLocked() {
        """
select
    * 
from
    Link 
where 
    linkType = $RgmDbConst.LinkType_blocked and
    usrFrom = :usrTo and
    usrTo = :usrFrom and
    dbeg <= :dt and
    dend > :dt 
"""
    }

    String sqlWaitingLink() {
        """
select
    * 
from
    Link 
where 
    confirmState = $RgmDbConst.ConfirmState_waiting and
    usrFrom = :usrFrom and
    usrTo = :usrTo
"""
    }

    String sqlLinkBlocked() {
        """
select
    * 
from
    Link 
where 
    linkType = $RgmDbConst.LinkType_blocked and
    usrFrom = :usrFrom and
    usrTo = :usrTo and
    dbeg <= :dt and
    dend > :dt 
"""
    }

}
