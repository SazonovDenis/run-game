package run.game.dao.game


import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.auth.std.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class StatisticManager_Test extends RgmBase_Test {


    /**
     * Копим статистику:
     * пользователи много раз отвечают
     */
    @Test
    void postTaskAnswer() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        mdb.outTable(statisticManager.getTaskStatistic(getCurrentUserId()))

        //
        Rnd rnd = new RndImpl()
        Server server = mdb.create(ServerImpl)

        //
        for (int i = 0; i < 100000; i++) {
            if (rnd.bool(1, 10)) {
                setCurrentUser(new DefaultUserPasswdAuthToken("admin", "111"))
            } else if (rnd.bool(1, 10)) {
                setCurrentUser(new DefaultUserPasswdAuthToken("user1010", ""))
            } else if (rnd.bool(1, 10)) {
                setCurrentUser(new DefaultUserPasswdAuthToken("user1011", ""))
            } else if (rnd.bool(1, 10)) {
                setCurrentUser(new DefaultUserPasswdAuthToken("user1012", ""))
            }

            // Получаем задание
            DataBox task = server.choiceTask(9999)

            // Печатаем задание
            if (i % 1000 == 0) {
                printTaskOneLine(task)
            }


            // Пользователь отвечает
            StoreRecord recGameTask = task.get("task")
            long idGameTask = recGameTask.getLong("id")

            //
            boolean wasTrue = false
            boolean wasFalse = false
            boolean wasHint = false
            boolean wasSkip = false

            //
            if (rnd.bool(1, 5)) {
                wasHint = true
            } else if (rnd.bool(1, 10)) {
                wasSkip = true
            } else {
                // Выбираем правильный ответ с некоторой вероятностью
                if (getCurrentUserId() == 1000) {
                    if (recGameTask.getString("text") == "cranberry" && rnd.bool(10, 1)) {
                        wasTrue = true
                    } else if (recGameTask.getString("text") == "клюква" && rnd.bool(1, 1)) {
                        wasTrue = true
                    } else {
                        wasFalse = true
                    }
                } else {
                    if (recGameTask.getString("text") == "овощи" && rnd.bool(1, 2)) {
                        wasTrue = true
                    } else {
                        wasFalse = true
                    }
                }
            }

            // Иногда пользователь думает
            if (rnd.bool(1, 10)) {
                System.sleep(rnd.num(5, 15))
            }


            // Отправляем ответ пользователя
            server.postTaskAnswer(idGameTask, [wasTrue: wasTrue, wasFalse: wasFalse, wasHint: wasHint, wasSkip: wasSkip])
        }

        //
        mdb.outTable(statisticManager.getTaskStatistic(getCurrentUserId()))
    }


    /**
     * Статистика по пользователям
     */
    @Test
    void getUsrStatisticByPlan() {
        long idPlan = 1000

        //
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        mdb.outTable(statisticManager.getTaskStatisticByPlan(1010, idPlan))
        mdb.outTable(statisticManager.getTaskStatisticByPlan(1011, idPlan))
        mdb.outTable(statisticManager.getTaskStatisticByPlan(1012, idPlan))

        //
        sw.start()
        mdb.outTable(statisticManager.getTaskStatisticByPlan(1000, idPlan))
        sw.stop()
        sw.printItems()
    }


    /**
     * Статистика по пользователям
     */
    @Test
    void loadUsrStatistic() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        mdb.outTable(statisticManager.getTaskStatistic(1010))
        mdb.outTable(statisticManager.getTaskStatistic(1011))
        mdb.outTable(statisticManager.getTaskStatistic(1012))

        //
        sw.start()
        mdb.outTable(statisticManager.getTaskStatistic(1000))
        sw.stop()
        sw.printItems()
    }

    /**
     * Статистика по планам
     */
    @Test
    void loadPlanStatistic() {
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)

        //
        mdb.outTable(statisticManager.getPlanStatistic())
    }


}
