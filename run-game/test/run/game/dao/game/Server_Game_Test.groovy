package run.game.dao.game

import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.auth.*
import run.game.dao.backstage.*

class Server_Game_Test extends Server_Test {


    @Test
    void testGameProcess_PlanDefault() {
        long idUsr = getCurrentUserId()
        Usr_upd upd = mdb.create(Usr_upd)
        long idPlan = upd.loadPlanDefault(idUsr)

        //
        checkOrFillPlan(idPlan)

        // Статистика по уровню
        printTaskStatisticByPlan(idPlan)

        // Игра
        doGameProcess(idPlan)

        // Обновленная статистика по уровню
        printTaskStatisticByPlan(idPlan)
    }


    @Test
    void testGameProcess_PlanDefault_allError() {
        long idUsr = getCurrentUserId()
        Usr_upd upd = mdb.create(Usr_upd)
        long idPlan = upd.loadPlanDefault(idUsr)

        //
        checkOrFillPlan(idPlan)

        // Статистика по уровню
        printTaskStatisticByPlan(idPlan)

        // Игра
        doGameProcess_internal(idPlan, false, true)

        // Обновленная статистика по уровню
        printTaskStatisticByPlan(idPlan)
    }


    @Test
    void testGameProcess_PlanPublic() {
        Store stPlans = mdb.loadQuery("select Plan.id from Plan where Plan.isPublic = 1 order by id")
        long idPlan = stPlans.get(0).getLong("id")

        //
        Plan_upd planUpd = mdb.create(Plan_upd)
        try {
            planUpd.addUsrPlan(idPlan)
        } catch (Exception e) {
            println(e.message)
        }

        // Статистика по уровню
        printTaskStatisticByPlan(idPlan)

        // Игра
        doGameProcess(idPlan)

        // Обновленная статистика по уровню
        printTaskStatisticByPlan(idPlan)
    }


}
