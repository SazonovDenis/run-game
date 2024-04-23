package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.auth.*

class Plan_upd extends RgmMdbUtils {


    /**
     * Сохраняет новый план обучения
     *
     * @param plan план
     * @param planFact факты в плане (комбинации фактов)
     * @return id добавленной записи
     */
    @DaoMethod
    long ins(Map plan, List<Map> planFact, List<Map> planTag) {
        // План - не публичный
        plan.put("isPublic", false)

        // Владелец плана - текущий пользователь
        long idUsr = getCurrentUserId()
        Map usrPlan = [usr: idUsr, isOwner: true]

        // Добавляем план в БД
        long idPlan = insInternal(plan, planFact, planTag, usrPlan)

        // Пересчитаем кубы
        RgmCubeUtils cubeUtils = mdb.create(RgmCubeUtils)
        cubeUtils.cubesRecalcPlan(idUsr, idPlan)

        //
        return idPlan
    }


    /**
     * Редактирует план обучения
     *
     * @param plan план
     */
    @DaoMethod
    void upd(Map plan) {
        // Проверим, что запись о плане существует и доступна пользователю
        long idPlan = UtCnv.toLong(plan.get("id"))
        validatePlanEdit(idPlan)

        //
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        recPlan.setValue("id", idPlan)
        recPlan.setValue("text", plan.get("text"))

        //
        mdb.updateRec("Plan", plan)
    }


    @DaoMethod
    void addFact(long idPlan, List<Map> planFact) {
        // Проверим, что запись о плане существует и доступна пользователю
        validatePlanEdit(idPlan)

        //
        for (Map mapPlanFact : planFact) {
            StoreRecord rec = mdb.createStoreRecord("PlanFact.upd", mapPlanFact)
            rec.setValue("plan", idPlan)
            //
            mdb.validateRecord(rec)
            mdb.validateErrors.checkErrors()
            //
            mdb.insertRec("PlanFact", rec)
        }

        // Проверим наличие заданий или создадим их
        checkPlanTasks(idPlan)

        // Пересчитаем кубы
        long idUsr = getCurrentUserId()
        RgmCubeUtils cubeUtils = mdb.create(RgmCubeUtils)
        cubeUtils.cubesRecalcPlan(idUsr, idPlan)
    }


    @DaoMethod
    void delFact(long idPlan, List<Map> planFact) {
        // Проверим, что запись о плане существует и доступна пользователю
        validatePlanEdit(idPlan)

        //
        for (Map mapPlanFact : planFact) {
            mdb.deleteRec("PlanFact", [
                    plan        : idPlan,
                    factQuestion: mapPlanFact.get("factQuestion"),
                    factAnswer  : mapPlanFact.get("factAnswer"),
            ])
        }

        // Пересчитаем кубы
        long idUsr = getCurrentUserId()
        RgmCubeUtils cubeUtils = mdb.create(RgmCubeUtils)
        cubeUtils.cubesRecalcPlan(idUsr, idPlan)
    }


    /**
     * Удаляем план со всеми ссылками, кроме Game.
     * Если есть Game, то план невозможно удалить.
     * Если есть чужие UsrPlan, то план невозможно удалить.
     * @param idPlan
     */
    @DaoMethod
    void del(long idPlan) {
        // Проверим, что запись о плане существует и доступна пользователю
        validatePlanEdit(idPlan)
        validatePlanDel(idPlan)

        // Метки доступа
        long idUsr = getCurrentUserId()
        mdb.execQuery("delete from UsrPlan where plan = :plan and usr = :usr", [plan: idPlan, usr: idUsr])
        // Другие ссылки
        //mdb.execQuery("delete from PlanFact where plan = :plan", [plan: plan])
        mdb.execQuery("delete from PlanTag where plan = :plan", [plan: idPlan])
        // Сама запись
        mdb.execQuery("delete from Plan where id = :plan", [plan: idPlan])
    }


    /**
     * Добавить план к списку планов пользователя.
     */
    @DaoMethod
    void addUsrPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        StoreRecord rec = mdb.loadQueryRecord(sqlUsrPlan(), [usr: idUsr, plan: idPlan], false)

        //
        if (rec != null && rec.getBoolean("isAllowed")) {
            throw new XError("План уже добавлен к списку планов пользователя")
        }

        //
        if (rec == null) {
            mdb.insertRec("UsrPlan", [plan: idPlan, usr: idUsr, isAllowed: true])
        } else {
            mdb.updateRec("UsrPlan", [id: rec.getLong("id"), isAllowed: true])
        }

        // Пересчитаем кубы
        RgmCubeUtils cubeUtils = mdb.create(RgmCubeUtils)
        cubeUtils.cubesRecalcPlan(idUsr, idPlan)
    }


    /**
     * Исключить план из списка планов пользователя.
     */
    @DaoMethod
    void delUsrPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        StoreRecord rec = mdb.loadQueryRecord(sqlUsrPlan(), [usr: idUsr, plan: idPlan], false)

        //
        if (rec == null || !rec.getBoolean("isAllowed")) {
            throw new XError("План не был добавлен к списку планов пользователя")
        }

        //
        validatePlanDel(idPlan)

        //
        mdb.updateRec("UsrPlan", [id: rec.getLong("id"), isAllowed: false])
    }


    /**
     * Скрыть план из списка планов пользователя.
     */
    @DaoMethod
    void hideUsrPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        StoreRecord rec = mdb.loadQueryRecord(sqlUsrPlan(), [usr: idUsr, plan: idPlan], false)

        //
        if (rec == null || (!rec.getBoolean("isAllowed") && !rec.getBoolean("isOwner"))) {
            throw new XError("План не был добавлен к списку планов пользователя")
        }

        //
        validatePlanDel(idPlan)

        //
        mdb.updateRec("UsrPlan", [id: rec.getLong("id"), isHidden: true])
    }


    /**
     * Вернуть скрытый план из списка планов пользователя.
     */
    @DaoMethod
    void unhideUsrPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        StoreRecord rec = mdb.loadQueryRecord(sqlUsrPlan(), [usr: idUsr, plan: idPlan], false)

        if (rec == null || (!rec.getBoolean("isAllowed") && !rec.getBoolean("isOwner"))) {
            throw new XError("План не был добавлен к списку планов пользователя")
        }

        //
        mdb.updateRec("UsrPlan", [id: rec.getLong("id"), isHidden: false])
    }


    long insInternal(Map plan, List<Map> planFact, List<Map> planTag, Map usrPlan) {
        // Plan
        StoreRecord recPlan = mdb.createStoreRecord("Plan", plan)
        long idPlan = mdb.insertRec("Plan", recPlan)

        // PlanTag
        for (Map mapPlanTag : planTag) {
            StoreRecord rec = mdb.createStoreRecord("PlanTag", mapPlanTag)
            rec.setValue("plan", idPlan)
            mdb.insertRec("PlanTag", rec)
        }

        // PlanFact
        for (Map mapPlanFact : planFact) {
            StoreRecord rec = mdb.createStoreRecord("PlanFact", mapPlanFact)
            rec.setValue("plan", idPlan)
            mdb.insertRec("PlanFact", rec)
        }

        // UsrPlan: владелец плана
        if (usrPlan != null) {
            StoreRecord recUsrPlan = mdb.createStoreRecord("UsrPlan", usrPlan)
            recUsrPlan.setValue("plan", idPlan)
            mdb.insertRec("UsrPlan", recUsrPlan)
        }

        // Проверим наличие заданий или создадим их
        checkPlanTasks(idPlan)

        //
        return idPlan
    }


    /**
     * Проверяет, есть ли задания в БД для каждой пары фактов
     * Если не хватает - создадим их
     * @param planFact
     */
    private void checkPlanTasks(long idPlan) {
        Store stPlanTasks = mdb.loadQuery(sqlPlanTasks(), [plan: idPlan])

        // Создадим задания
        Task_upd taskUpd = mdb.create(Task_upd)
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)

        //
        for (StoreRecord rec : stPlanTasks) {
            if (rec.getLong("taskCount") == 0) {
                // Создадим задание
                long idFactQuestion = rec.getLong("factQuestion")
                long idFactAnswer = rec.getLong("factAnswer")
                DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, null)
                // Сохраним задание
                taskUpd.saveTask(task)
            }
        }
    }


    private String sqlUsrPlan() {
        return """
select
    UsrPlan.*
from
    UsrPlan
where
    UsrPlan.plan = :plan and
    UsrPlan.usr = :usr
"""
    }

    private String sqlPlanTasks() {
        return """ 
-- Количество заданий для каждой пары фактов в плане
select 
    PlanFact.plan,
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    count(Task.id) as taskCount 

from
    PlanFact
    left join Task on (
        PlanFact.factQuestion = Task.factQuestion and 
        PlanFact.factAnswer = Task.factAnswer
    )
    
where
    PlanFact.plan = :plan    
    
group by
    PlanFact.plan,
    PlanFact.factQuestion, 
    PlanFact.factAnswer 
"""
    }


    void validatePlanEdit(long idPlan) {
        Plan_list list = mdb.create(Plan_list)
        StoreRecord rec = list.getPlan(idPlan)
        //
        if (!rec.getBoolean("isOwner")) {
            throw new XError("Пользователь не имеет права редактировать план")
        }
    }

    void validatePlanDel(long idPlan) {
        long idUsr = getCurrentUserId()
        Usr_upd upd = mdb.create(Usr_upd)
        long planDefault = upd.getPlanDefault(idUsr)
        //
        if (idPlan == planDefault) {
            throw new XError("Невозможно удалить план по умолчанию")
        }
    }

}
