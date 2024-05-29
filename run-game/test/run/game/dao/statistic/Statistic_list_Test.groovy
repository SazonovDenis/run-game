package run.game.dao.statistic

import jandcode.commons.datetime.*
import jandcode.core.dbm.std.*
import org.junit.jupiter.api.*
import run.game.dao.*

class Statistic_list_Test extends RgmBase_Test {

    XDate dbeg = XDate.create("2024-05-20")
    XDate dend = XDate.create("2024-12-31")
    long usr = 1017

    @Test
    void loadByXXX() {
        Statistic_list list = mdb.create(Statistic_list)

        //setCurrentUserId(usr)

        //
        DataBox boxPlan = list.byPlan(dbeg, dend)
        DataBox boxGame = list.byGame(dbeg, dend)
        DataBox boxWord = list.byWord(dbeg, dend)

        //
        println()
        println("byPlan")
        mdb.outTable(boxPlan.get("items"))
        mdb.outTable(boxPlan.get("rating"))
        //
        println()
        println("byGame")
        mdb.outTable(boxGame.get("items"))
        mdb.outTable(boxGame.get("rating"))
        //
        println()
        println("byWord")
        mdb.outTable(boxWord.get("items"))
        mdb.outTable(boxWord.get("rating"))
    }


    @Test
    void loadByPlan() {
        //utils.logOn()

        Statistic_list list = mdb.create(Statistic_list)

        //setCurrentUserId(usr)

        //
        DataBox boxPlan = list.byPlan(dbeg, dend)

        //
        println()
        println("byPlan")
        mdb.outTable(boxPlan.get("items"))
        mdb.outTable(boxPlan.get("rating"))
    }


    @Test
    void loadByGame() {
        //utils.logOn()

        Statistic_list list = mdb.create(Statistic_list)

        //setCurrentUserId(usr)

        //
        DataBox boxPlan = list.byGame(dbeg, dend)

        //
        println()
        println("byGame")
        mdb.outTable(boxPlan.get("items"))
        mdb.outTable(boxPlan.get("rating"))
    }


    @Test
    void loadByWord() {
        //utils.logOn()

        Statistic_list list = mdb.create(Statistic_list)

        //setCurrentUserId(usr)

        //
        DataBox boxWord = list.byWord(dbeg, dend)

        //
        println()
        println("byWord")
        mdb.outTable(boxWord.get("items"))
        mdb.outTable(boxWord.get("rating"))
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
