package run.game.dao.game

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*

class StatisticManager1 extends RgmMdbUtils {


    // Эти коэффицианты отвечают за вес текущего, предыдущего и пред-предыдущего ответа
    double[] ratingMultiplier = [0.5, 0.3, 0.2]

    @DaoMethod
    public Store getStatisticForGame(long idGame) {
        long idUsr = getCurrentUserId()

        //println()
        //println("getStatisticForGame, idGame: " + idGame)

        // Игра
        Map params = [usr: idUsr, game: idGame]
        StoreRecord recGame = mdb.loadQueryRecord(sqlRecGame(), params)
        //mdb.outTable(recGame)

        // План
        long idPlan = recGame.getLong("plan")

        // Период заданий для плана
        XDateTime dend = recGame.getDateTime("dend")
        XDateTime dbeg = recGame.getDateTime("dbeg")
        // Период заданий - за сутки от начала
        dbeg = dbeg.addDays(-1)
        // Для неазаконченной игры dend
        if (UtDateTime.isEmpty(dend)) {
            dend = UtDateTime.EMPTY_DATETIME_END // XDateTime.now()
        }

        // Все задания, выданные пользователю по плану за период
        params = [dbeg: dbeg, dend: dend, "usr": idUsr, plan: idPlan]
        Store stGameTask = mdb.loadQuery(sqlGameTask(), params)
        //mdb.outTable(stGameTask)

        // Сгруппируем по task
        Store stTaskStatistic = mdb.createStore("StatisticManager1.task.statistic")

        //
        List<Boolean> taskAnswers = []
        for (int n = 0; n < stGameTask.size(); n++) {
            // Текущая и следующая запись
            StoreRecord recGameTask = stGameTask.get(n)
            StoreRecord recGameTaskNext = null
            if (n < stGameTask.size() - 1) {
                recGameTaskNext = stGameTask.get(n + 1)
            }

            // Накопление rating по очередному GameTask
            if (taskAnswers.size() < 3) {
                if (recGameTask.getLong("wasTrue")) {
                    taskAnswers.add(true)
                } else {
                    taskAnswers.add(false)
                }
            }

            // Накопление по очередному task закончено
            if (recGameTaskNext == null || recGameTaskNext.getLong("task") != recGameTask.getLong("task")) {
                // Если не хватает последних результатов (мало раз выполнено task) - считаем, последние были отвечены неправильно
                while (taskAnswers.size() < 3) {
                    taskAnswers.add(false)
                }

                double rating = 0
                for (int i = 0; i < 3; i++) {
                    if (taskAnswers.get(i) == true) {
                        rating = rating + ratingMultiplier[i]
                    }
                }

                //
                StoreRecord recPlanTaskStatistic = stTaskStatistic.add()
                recPlanTaskStatistic.setValue("task", recGameTask.getValue("task"))
                recPlanTaskStatistic.setValue("rating", rating)
                //
                taskAnswers = []
            }

            //
            //n = n + 1
        }

        //
        //mdb.outTable(stTaskStatistic)


        //
        return stTaskStatistic
    }

    String sqlRecGame() {
        return """
select 
    Game.* 
from 
    Game
    join GameUsr on (GameUsr.game = Game.id) 
where
    GameUsr.game = :game and 
    GameUsr.usr = :usr 
"""
    }

    String sqlGameTask() {
        return """
select 
    -- Plan.id as planId,
    -- PlanTask.id as planTaskId,
    -- GameTask.id as gameTaskId,
    -- Game.id as gameId,
    -- Game.dbeg as gameDbeg,
    
    PlanTask.plan,
    PlanTask.task,
    GameTask.*
     
from 
    Plan
    join PlanTask on (
        PlanTask.plan = Plan.id
    )
    left join GameTask on (
        GameTask.task = PlanTask.task and
        GameTask.usr = :usr and
        GameTask.dtTask >= :dbeg and 
        GameTask.dtTask < :dend
    )
    left join Game on (
        Game.id = GameTask.game and
        Game.plan = Plan.id 
    ) 
     
where
    Plan.id = :plan
--    and PlanTask.task <= 1005
     
order by
    PlanTask.task,
    GameTask.dtTask desc,
    GameTask.game desc
"""
    }


}
