package run.game.dao.statistic


import jandcode.commons.datetime.*
import jandcode.core.store.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Statistic_list_Test extends RgmBase_Test {

    XDate dbeg = XDate.create("2024-05-20")
    //XDate dbeg = XDate.create("2024-01-20")
    XDate dend = XDate.create("2025-01-01")
    long usr = 1017

    @Test
    void loadByXXX() {
        Statistic_list list = mdb.create(Statistic_list)

        setCurrentUserId(usr)

        //
        Store stPlan = list.byPlan(dbeg, dend)
        Store stGame = list.byGame(dbeg, dend)
        Store stWord = list.byWord(dbeg, dend)

        //
        println()
        println("byPlan")
        mdb.outTable(stPlan)
        //
        println()
        println("byGame")
        mdb.outTable(stGame)
        //
        println()
        println("byWord")
        mdb.outTable(stWord)
    }


    @Test
    void loadByPlan() {
        //utils.logOn()

        Statistic_list list = mdb.create(Statistic_list)

        setCurrentUserId(usr)

        //
        Store stPlan = list.byPlan(dbeg, dend)

        //
        println()
        println("byPlan")
        mdb.outTable(stPlan)
    }


    @Test
    void loadByWord() {
        //utils.logOn()

        Statistic_list list = mdb.create(Statistic_list)

        setCurrentUserId(usr)

        //
        Store stWord = list.byWord(dbeg, dend)

        //
        println()
        println("byWord")
        mdb.outTable(stWord)
    }


    @Test
    void baseStatistic() {
        Map params = [usr: usr, dbeg: dbeg, dend: dend]
        Statistic_list list = mdb.create(Statistic_list)


        // Загружаем
        Map<String, Map> mapTasksStatistic = list.loadStatistic(params, "game")

        // Печатаем
        println()
        println("byGame")
        for (String key : mapTasksStatistic.keySet()) {
            Map taskStatisticAcc = mapTasksStatistic.get(key)
            println(key + ": " + taskStatisticAcc)
        }


        // Загружаем
        mapTasksStatistic = list.loadStatistic(params, "plan")

        // Печатаем
        println()
        println("byPlan")
        for (String key : mapTasksStatistic.keySet()) {
            Map taskStatisticAcc = mapTasksStatistic.get(key)
            println(key + ": " + taskStatisticAcc)
        }


        // Загружаем
        mapTasksStatistic = list.loadStatistic(params, "word")

        // Печатаем
        println()
        println("byWord")
        for (String key : mapTasksStatistic.keySet()) {
            Map taskStatisticAcc = mapTasksStatistic.get(key)
            println(key + ": " + taskStatisticAcc)
        }
    }


}
