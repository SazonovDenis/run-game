package run.game.dao.backstage

import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class Plan_upd extends RgmMdbUtils {


    /**
     * Сохраняет новый план обучения
     *
     * @param plan     план
     * @param planFact факты в плане (комбинации фактов)
     * @return id добавленной записи
     */
    @DaoMethod
    long ins(StoreRecord plan, Store planFact, Store planTag) {
        // Plan
        long idPlan = mdb.insertRec("Plan", plan)

        // PlanTag
        for (StoreRecord rec : planTag) {
            rec.setValue("plan", idPlan)
            mdb.insertRec("PlanTag", rec)
        }

        // PlanFact
        for (StoreRecord rec : planFact) {
            rec.setValue("plan", idPlan)
            mdb.insertRec("PlanFact", rec)
        }

        //
        return idPlan
    }


    @DaoMethod
    void addFact(long idPlan, Store planFact) {
        for (StoreRecord rec : planFact) {
            rec.setValue("plan", idPlan)
            mdb.insertRec("PlanFact", rec)
        }
    }


    @DaoMethod
    void delFact(long idPlan, long idPlanFact) {
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
        mdb.execQuery("delete from PlanFact where plan = :plan", [plan: idPlan])
        mdb.execQuery("delete from PlanTask where plan = :plan", [plan: idPlan])
        mdb.execQuery("delete from PlanTag where plan = :plan", [plan: idPlan])
        // Метки доступа
        long idUsr = getCurrentUserId()
        mdb.execQuery("delete from UsrPlan where plan = :plan and usr = :usr", [plan: idPlan, usr: idUsr])
        //
        mdb.execQuery("delete from Plan where id = :plan", [plan: idPlan])
    }


}
