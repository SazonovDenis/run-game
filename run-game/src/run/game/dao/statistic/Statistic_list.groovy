package run.game.dao.statistic

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.game.ServerImpl

class Statistic_list extends RgmMdbUtils {

    /**
     *
     */
    @DaoMethod
    Store byPlan(XDate dbeg, XDate dend) {
        Store st = mdb.createStore("Statistic.plan")

        //
        long idUsr = getContextOrCurrentUsrId()
        Map params = [usr: idUsr, dbeg: dbeg, dend: dend]

        //
        mdb.loadQuery(st, sqlPlan(), params)

        //
        Map<String, Map> statistic = loadStatistic(params, "plan")

        //
        distributeStatistic(statistic, st, "plan")


        //
        return st
    }

    @DaoMethod
    Store byGame(XDate dbeg, XDate dend) {
        Store st = mdb.createStore("Statistic.game")

        //
        long idUsr = getContextOrCurrentUsrId()
        Map params = [usr: idUsr, dbeg: dbeg, dend: dend]

        //
        mdb.loadQuery(st, sqlGame(), params)

        //
        Map<String, Map> statistic = loadStatistic(params, "game")

        //
        distributeStatistic(statistic, st, "game")


        //
        return st
    }

    @DaoMethod
    Store byWord(XDate dbeg, XDate dend) {
        Store st = mdb.createStore("Statistic.word")

        //
        long idUsr = getContextOrCurrentUsrId()
        Map params = [usr: idUsr, dbeg: dbeg, dend: dend]

        //
        mdb.loadQuery(st, sqlWord(), params)

        //
        Map<String, Map> statistic = loadStatistic(params, "word")

        //
        distributeStatistic(statistic, st, "word")


        // Дополним факты в плане "богатыми" данными для вопроса и ответа
        ServerImpl serverImpl = mdb.create(ServerImpl)
        serverImpl.fillFactBody(st)

        //
        return st
    }


    Map<String, Map> loadStatistic(Map params, String groupByKey) {
        // Загружаем
        Store stStatistic = mdb.loadQuery(sqlBaseStatistic(), params)

/*
        /////////////////
        println()
        println("stStatistic")
        mdb.outTable(stStatistic)
        /////////////////
*/

        // Копим статистику из BLOB по записям stStatistic
        Map<String, Map> mapTasksStatistic = new HashMap<>()

        //
        for (StoreRecord rec : stStatistic) {

            // Извлекаем статистику по фактам из BLOB
            List<Map> listTaskStatistic = UtJson.fromJson(new String(rec.getValue("taskStatistic"), "utf-8"))

            // Разносим статистику: каждую пару фактов копим с группировкой по определенным значениям ключа
            for (Map taskStatistic : listTaskStatistic) {
                double ratingTaskRec = UtCnv.toDouble(taskStatistic.get("ratingTask"))
                double ratingQuicknessRec = UtCnv.toDouble(taskStatistic.get("ratingQuickness"))
                double ratingTaskDiffRec = UtCnv.toDouble(taskStatistic.get("ratingTaskDiff"))
                double ratingQuicknessDiffRec = UtCnv.toDouble(taskStatistic.get("ratingQuicknessDiff"))

                // Значение ключа
                Map keyValues = rec.getValues()
                keyValues.putAll(taskStatistic)
                String key = createKey(keyValues, groupByKey)

                // Уже был такой ключ?
                Map taskStatisticAcc = mapTasksStatistic.get(key)
                if (taskStatisticAcc == null) {
                    // Новый элемент накопления статистики
                    taskStatisticAcc = new HashMap()
                    taskStatisticAcc.put("cnt", 0)
                    taskStatisticAcc.put("ratingTask", ratingTaskRec)
                    taskStatisticAcc.put("ratingQuickness", ratingQuicknessRec)
                    mapTasksStatistic.put(key, taskStatisticAcc)
                }

                // Копим статистику для ключа
                long cntAcc = UtCnv.toLong(taskStatisticAcc.get("cnt"))
                double ratingTaskDiffAcc = UtCnv.toDouble(taskStatisticAcc.get("ratingTaskDiff"))
                double ratingQuicknessDiffAcc = UtCnv.toDouble(taskStatisticAcc.get("ratingQuicknessDiff"))
                taskStatisticAcc.put("cnt", cntAcc + 1)
                taskStatisticAcc.put("ratingTaskDiff", ratingTaskDiffAcc + ratingTaskDiffRec)
                taskStatisticAcc.put("ratingQuicknessDiff", ratingQuicknessDiffAcc + ratingQuicknessDiffRec)
            }
        }


        //
        return mapTasksStatistic
    }


    void distributeStatistic(Map<String, Map> mapTasksStatistic, Store st, String groupByKey) {
/*
        ////////////////
        println()
        mdb.outTable(st)
        println()
        for (String key : mapTasksStatistic.keySet()) {
            Map taskStatisticAcc = mapTasksStatistic.get(key)
            println(key + ": " + taskStatisticAcc)
        }
        ////////////////
*/

        for (StoreRecord rec : st) {
            // Значение ключа
            Map keyValues = rec.getValues()
            String key = createKey(keyValues, groupByKey)

            // Берем статистику для ключа
            Map taskStatistic = mapTasksStatistic.get(key)

/*
            ///////////////////
            if (taskStatistic == null) {
                println("key: " + key)
                println()
            }
            ///////////////////
*/

            // Статистика для ключа
            rec.setValue("ratingTask", taskStatistic.get("ratingTask"))
            rec.setValue("ratingQuickness", taskStatistic.get("ratingQuickness"))
            rec.setValue("ratingTaskDiff", taskStatistic.get("ratingTaskDiff"))
            rec.setValue("ratingQuicknessDiff", taskStatistic.get("ratingQuicknessDiff"))
        }
    }


    String createKey(Map keyValues, String groupByKey) {
        String key

        long plan = UtCnv.toLong(keyValues.get("plan"))
        long game = UtCnv.toLong(keyValues.get("game"))
        long factQuestion = UtCnv.toLong(keyValues.get("factQuestion"))
        long factAnswer = UtCnv.toLong(keyValues.get("factAnswer"))

        switch (groupByKey) {
            case "game": {
                key = game
                break
            }
            case "plan": {
                key = plan
                break
            }
            case "word": {
                key = factQuestion + "_" + factAnswer
                break
            }
            default: {
                throw new XError("Bad groupByKey: " + groupByKey)
            }
        }

        return key
    }


    String sqlBase() {
        return """
select 
    Game.id game,
    Game.plan,
    Game.dbeg,
    Game.dend,
    GameTask.id gameTask,
    GameTask.task,
    Task.factQuestion,
    Task.factAnswer
    
from
    Game 
    join GameUsr on (GameUsr.game = Game.id) 
    join GameTask on (GameTask.game = Game.id and GameTask.usr = GameUsr.usr) 
    join Task on (GameTask.task = Task.id)

where
    Game.dbeg >= :dbeg and
    Game.dbeg < :dend and 
    GameUsr.usr = :usr and
    GameTask.dtTask is not null 
"""
    }

    String sqlBaseStatistic() {
        return """
select 
    Game.id game,
    Game.dbeg,
    Game.dend,
    Game.plan,
    GameUsr.usr,
    Cube_UsrGame.taskStatistic
    
from
    Game 
    join GameUsr on (GameUsr.game = Game.id) 
    left join Cube_UsrGame on (Cube_UsrGame.game = Game.id and Cube_UsrGame.usr = GameUsr.usr)

where
    Game.dbeg >= :dbeg and
    Game.dbeg < :dend and 
    GameUsr.usr = :usr 
    
order by    
    Game.dbeg 
"""
    }


    String sqlPlan() {
        return """
with LstBase as (
${sqlBase()}
)


select 
    LstBase.plan,
    Plan.text as planText

from
    LstBase 
    join Plan on (
        LstBase.plan = Plan.id
    )   

group by
    LstBase.plan,
    Plan.text

order by
    Plan.text 

"""
    }


    String sqlGame() {
        return """
with LstBase as (
${sqlBase()}
)


select
    LstBase.game, 
    LstBase.dbeg, 
    LstBase.dend, 
    LstBase.plan,
    Plan.text as planText

from
    LstBase 
    join Plan on (
        LstBase.plan = Plan.id
    )   

group by 
    LstBase.game, 
    LstBase.dbeg, 
    LstBase.dend, 
    LstBase.plan,
    Plan.text

order by
    LstBase.dbeg 

"""
    }


    String sqlWord() {
        return """
with LstBase as (
${sqlBase()}
)


select 
    Fact.item,
    LstBase.factQuestion,
    LstBase.factAnswer

from
    LstBase
    join Fact on (LstBase.factQuestion = Fact.id)

group by 
    Fact.item,
    LstBase.factQuestion,
    LstBase.factAnswer

"""
    }


}
