package run.game.dao.game

import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.auth.*
import run.game.dao.backstage.*

/**
 * Имитация игр
 */
class Server_Game_Test extends Server_Test {


    void setUp() throws Exception {
        super.setUp()

        //
        setCurrentUser("user1010", "")
    }

    @Test
    void testGameProcess_1() {
        testGameProcess_PlanDefault()
        Thread.sleep(1000)
        testGameProcess_PlanDefault()
        Thread.sleep(2000)
        testGameProcess_PlanDefault()
        Thread.sleep(1000)
        testGameProcess_PlanDefault()

        //
        Thread.sleep(1000)
        testGameProcess_PlanPublic()
        Thread.sleep(1000)
        testGameProcess_PlanPublic()
    }

    @Test
    void testGameProcess_2() {
        testGameProcess_PlanDefault()
        Thread.sleep(1000)
        testGameProcess_PlanDefault()
        Thread.sleep(2000)
        testGameProcess_PlanDefault()

        //
        Thread.sleep(1000)
        testGameProcess_PlanDefault_allError()
        Thread.sleep(1000)
        testGameProcess_PlanDefault_allError()
        Thread.sleep(1000)
        testGameProcess_PlanDefault_allError()
    }

    @Test
    void testGameProcess_PlanDefault() {
        long idUsr = getCurrentUserId()
        Usr_upd upd = mdb.create(Usr_upd)
        long idPlan = upd.loadPlanDefault(idUsr)

        // Наполняет план planDefault словами
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

        // Наполняет план planDefault словами
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
        // Выбирает общедоступный plan
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
