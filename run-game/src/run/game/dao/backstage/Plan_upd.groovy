package run.game.dao.backstage

import jandcode.commons.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*

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
        // Plan
        long idPlan = mdb.insertRec("Plan", plan)

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

        // UsrPlan: автор плана - пользователь
        long idUsr = getCurrentUserId()
        mdb.insertRec("UsrPlan", [plan: idPlan, usr: idUsr, isAuthor: true])

        // Проверим наличие заданий или создадим их
        checkPlanTasks(idPlan)

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
        // Проверим, что запись существует и доступна пользователю
        long idPlan = UtCnv.toLong(plan.get("id"))
        Plan_list list = mdb.create(Plan_list)
        list.getPlanUsr(idPlan)

        //
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        recPlan.setValue("id", idPlan)
        recPlan.setValue("text", plan.get("text"))

        //
        mdb.updateRec("Plan", plan)
    }


    @DaoMethod
    void addFact(long idPlan, List<Map> planFact) {
        // Проверим, что запись существует и доступна пользователю
        Plan_list list = mdb.create(Plan_list)
        list.getPlanUsr(idPlan)

        //
        for (Map mapPlanFact : planFact) {
            StoreRecord rec = mdb.createStoreRecord("PlanFact", mapPlanFact)
            rec.setValue("plan", idPlan)
            mdb.insertRec("PlanFact", rec)
        }

        // Проверим наличие заданий или создадим их
        checkPlanTasks(idPlan)
    }


    @DaoMethod
    void delFact(long idPlan, long idPlanFact) {
        // Проверим, что запись существует и доступна пользователю
        Plan_list list = mdb.create(Plan_list)
        list.getPlanUsr(idPlan)

        //
        mdb.execQuery("delete from PlanFact where plan = :plan and id = :idPlanFact",
                [plan: idPlan, planFact: idPlanFact]
        )
    }


    /**
     * Удаляем план со всеми ссылками, кроме Game.
     * Если есть Game, то план невозможно удалить.
     * Если есть чужие UsrPlan, то план невозможно удалить.
     * @param idPlan
     */
    @DaoMethod
    void del(long idPlan) {
        // Метки доступа
        long idUsr = getCurrentUserId()
        mdb.execQuery("delete from UsrPlan where plan = :plan and usr = :usr", [plan: idPlan, usr: idUsr])
        // Другие ссылки
        mdb.execQuery("delete from PlanFact where plan = :plan", [plan: idPlan])
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
    }


    /**
     * Исключить план из списка планов пользователя.
     */
    @DaoMethod
    void delUsrPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        StoreRecord rec = mdb.loadQueryRecord(sqlUsrPlan(), [usr: idUsr, plan: idPlan], false)

        if (rec == null || !rec.getBoolean("isAllowed")) {
            throw new XError("План не был добавлен к списку планов пользователя")
        }

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

        if (rec == null || (!rec.getBoolean("isAllowed") && !rec.getBoolean("isAuthor"))) {
            throw new XError("План не был добавлен к списку планов пользователя")
        }

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

        if (rec == null || (!rec.getBoolean("isAllowed") && !rec.getBoolean("isAuthor"))) {
            throw new XError("План не был добавлен к списку планов пользователя")
        }

        //
        mdb.updateRec("UsrPlan", [id: rec.getLong("id"), isHidden: false])
    }


    /**
     * Проверяет, есть ли задания в БД для каждой пары фактов
     * Если не хватает - создадим их
     * @param planFact
     */
    private void checkPlanTasks(long idPlan) {
        Store st = mdb.loadQuery(sqlPlanTasks(), [plan: idPlan])
        mdb.outTable(st)

        // Создадим задания
        Task_upd taskUpd = mdb.create(Task_upd)
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)

        //
        for (StoreRecord rec : st) {
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


}
