package run.game.dao.statistic

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.game.*

class Statistic_list extends RgmMdbUtils {
    //^c цифры скачут
    /**
     *
     */
    @DaoMethod
    DataBox byPlan(XDate dbeg, XDate dend) {
        return byInternal(dbeg, dend, "plan")
    }

    @DaoMethod
    DataBox byGame(XDate dbeg, XDate dend) {
        return byInternal(dbeg, dend, "game")
    }

    @DaoMethod
    DataBox byWord(XDate dbeg, XDate dend) {
        DataBox res = byInternal(dbeg, dend, "word")

        // Дополним факты в плане "богатыми" данными для вопроса и ответа
        Store st = res.get("items")
        ServerImpl serverImpl = mdb.create(ServerImpl)
        serverImpl.fillFactBody(st)

        //
        return res
    }

    DataBox byInternal(XDate dbeg, XDate dend, String groupByKey) {
        // ---
        // Загружаем

        // Параметры
        long idUsr = getContextOrCurrentUsrId()
        XDateTime paramDbeg = dbeg.toDateTime()
        XDateTime paramDend = dend.toDateTime().addDays(1).addMSec(-1000)
        Map params = [usr: idUsr, dbeg: paramDbeg, dend: paramDend]

        // Загружаем
        Store st = mdb.createStore("Statistic." + groupByKey)
        mdb.loadQuery(st, getSql(groupByKey), params)

        // Загружаем статистику по фактам для всех игр, сыгранных за указанный период.
        // ВАЖНО! Ключевая зависимость:
        // Статистика расчитывается кубом Cube_UsrGameStatistic. Рассчитываем,
        // что куб на каждую игру содержит статистику по КАЖДОМУ выданному факту
        // на момент ОКОНЧАНИЯ игры.
        Store stStatistic = mdb.loadQuery(sqlStatistic(), params)


        // ---
        // Статистику по фактам агрегируем по groupByKey
        Map<String, Map> statistic = groupStatisticBy(stStatistic, groupByKey)

        // Распределяем агрегированную статистику в список
        distributeStatistic(statistic, st, groupByKey)


        // ---
        // Статистику по словам агрегируем по словам
        Map<String, Map> statisticByWord = groupStatisticBy(stStatistic, "word")

        // Суммируем агрегированную статистику
        StoreRecord rating = summStatisticByWord(statisticByWord)


        // ---
        DataBox res = new DataBox()
        res.put("items", st)
        res.put("rating", rating)

        //
        return res
    }

    String getSql(String groupByKey) {
        switch (groupByKey) {
            case "game": {
                return sqlGame()
            }
            case "plan": {
                return sqlPlan()
            }
            case "word": {
                return sqlWord()
            }
            default: {
                throw new XError("Bad groupByKey: " + groupByKey)
            }
        }
    }

    Map<String, Map> groupStatisticBy(Store stStatistic, String groupByKey) {
/*
        /////////////////
        println()
        println("stStatistic")
        mdb.outTable(stStatistic)
        /////////////////
*/

        // Копим статистику из BLOB по записям stStatistic
        Map<String, Map> resStatistic = new HashMap<>()

/*
        /////////////////
        println()
        println("stStatistic")
        for (StoreRecord rec : stStatistic) {
            //println()
            //mdb.outTable(rec)
            List<Map> listTaskStatistic = UtJson.fromJson(new String(rec.getValue("taskStatistic"), "utf-8"))
            println(listTaskStatistic)
        }
        /////////////////
*/

        //
        for (StoreRecord rec : stStatistic) {

            // Извлекаем статистику по фактам из BLOB
            List<Map> listTaskStatistic = UtJson.fromJson(new String(rec.getValue("taskStatistic"), "utf-8"))

            // Разносим статистику:
            // каждую пару фактов копим с группировкой по groupByKey
            for (Map taskStatistic : listTaskStatistic) {
                double ratingTaskRec = UtCnv.toDouble(taskStatistic.get("ratingTask"))
                double ratingQuicknessRec = UtCnv.toDouble(taskStatistic.get("ratingQuickness"))
                double ratingTaskDiffRec = UtCnv.toDouble(taskStatistic.get("ratingTaskDiff"))
                double ratingQuicknessDiffRec = UtCnv.toDouble(taskStatistic.get("ratingQuicknessDiff"))

                // Значение ключа группы
                Map keyValues = rec.getValues()
                keyValues.putAll(taskStatistic)
                String key = createKey(keyValues, groupByKey)

                // Уже был такой ключ группы?
                Map statisticGroup = resStatistic.get(key)
                if (statisticGroup == null) {
                    // Новая группа
                    statisticGroup = new HashMap()
                    resStatistic.put(key, statisticGroup)
                }

                // Значение ключа по словам
                String keyByWord = createKey(keyValues, "word")

                // Уже был такой ключ по словам внутри группы?
                Map statisticWord = statisticGroup.get(keyByWord)
                if (statisticWord == null) {
                    // Новый элемент внутри группы
                    statisticWord = new HashMap()
                    statisticGroup.put(keyByWord, statisticWord)
                }

                // Копим статистику для ключа по словам (внутри группы)
                // Разницу - суммируем за период
                double ratingTaskDiffAcc = UtCnv.toDouble(statisticWord.get("ratingTaskDiff"))
                double ratingQuicknessDiffAcc = UtCnv.toDouble(statisticWord.get("ratingQuicknessDiff"))
                statisticWord.put("ratingTaskDiff", ratingTaskDiffAcc + ratingTaskDiffRec)
                statisticWord.put("ratingQuicknessDiff", ratingQuicknessDiffAcc + ratingQuicknessDiffRec)
                // Итог - берем последний (не суммируем)
                statisticWord.put("ratingTask", ratingTaskRec)
                statisticWord.put("ratingQuickness", ratingQuicknessRec)
            }

        }


        //
        return resStatistic
    }


    void distributeStatistic(Map<String, Map> statistic, Store st, String groupByKey) {
/*
        ////////////////
        println()
        mdb.outTable(st)
        println()
        for (String keyGroup : statistic.keySet()) {
            Object statisticGroup = statistic.get(keyGroup)
            if (statisticGroup instanceof Map) {
                println(keyGroup)
                for (String key : statisticGroup.keySet()) {
                    Object item = statisticGroup.get(key)
                    println("  " + key + ": " + item)
                }
            }
        }
        ////////////////
*/

        for (StoreRecord rec : st) {
            // Значение ключа для группы groupByKey
            Map keyValues = rec.getValues()
            String key = createKey(keyValues, groupByKey)

            // Берем статистику для ключа группы
            Map groupStatistic = statistic.get(key)

            if (groupStatistic == null) {
                continue
            }

/*
            ///////////////////
            if (groupStatistic == null) {
                println("key: " + key)
                println()
            }
            ///////////////////
*/

            // Суммируем по всем словам внутри группы
            Map sumStatistic = summStatisticMap(groupStatistic)

            // Сумму в запись
            rec.setValues(sumStatistic)

/*
            // Статистика для ключа
            rec.setValue("ratingTask", groupStatistic.get("ratingTask"))
            rec.setValue("ratingQuickness", groupStatistic.get("ratingQuickness"))
            rec.setValue("ratingTaskDiff", groupStatistic.get("ratingTaskDiff"))
            rec.setValue("ratingQuicknessDiff", groupStatistic.get("ratingQuicknessDiff"))
*/
        }
    }

    Map<String, Double> summStatisticMap(Map<String, Map> statistic) {
        Map res = new HashMap()

        for (String key : statistic.keySet()) {
            Map item = statistic.get(key)

            res.put("ratingTask", UtCnv.toDouble(item.get("ratingTask")) + UtCnv.toDouble(res.get("ratingTask")))
            res.put("ratingQuickness", UtCnv.toDouble(item.get("ratingQuickness")) + UtCnv.toDouble(res.get("ratingQuickness")))
            res.put("ratingTaskDiff", UtCnv.toDouble(item.get("ratingTaskDiff")) + UtCnv.toDouble(res.get("ratingTaskDiff")))
            res.put("ratingQuicknessDiff", UtCnv.toDouble(item.get("ratingQuicknessDiff")) + UtCnv.toDouble(res.get("ratingQuicknessDiff")))
        }

        return res
    }

    StoreRecord summStatisticByWord(Map<String, Map> statisticByWord) {
/*
        ////////////////
        println()
        for (String keyGroup : statisticByWord.keySet()) {
            Object statisticGroup = statisticByWord.get(keyGroup)
            if (statisticGroup instanceof Map) {
                println(keyGroup)
                for (String key : statisticGroup.keySet()) {
                    Object item = statisticGroup.get(key)
                    println("  " + key + ": " + item)
                }
            }
        }
        ////////////////
*/


        // Сумма
        double ratingTask = 0
        double ratingQuickness = 0
        double ratingTaskDiff = 0
        double ratingQuicknessDiff = 0

        // Прибавка и потери рейтинга - по отдельности
        double ratingTaskInc = 0
        double ratingTaskDec = 0
        double ratingQuicknessInc = 0
        double ratingQuicknessDec = 0

        // Копим
        for (String key : statisticByWord.keySet()) {
            Map statisticWord = statisticByWord.get(key).get(key)

            //
            double ratingTaskRec = UtCnv.toDouble(statisticWord.get("ratingTask"))
            double ratingQuicknessRec = UtCnv.toDouble(statisticWord.get("ratingQuickness"))
            double ratingTaskDiffRec = UtCnv.toDouble(statisticWord.get("ratingTaskDiff"))
            double ratingQuicknessDiffRec = UtCnv.toDouble(statisticWord.get("ratingQuicknessDiff"))

            // Сумма
            ratingTask = ratingTask + ratingTaskRec
            ratingQuickness = ratingQuickness + ratingQuicknessRec
            ratingTaskDiff = ratingTaskDiff + ratingTaskDiffRec
            ratingQuicknessDiff = ratingQuicknessDiff + ratingQuicknessDiffRec

            // Прибавка и потери рейтинга
            if (ratingTaskDiffRec > 0) {
                ratingTaskInc = ratingTaskInc + ratingTaskDiffRec
            } else {
                ratingTaskDec = ratingTaskDec - ratingTaskDiffRec
            }
            //
            if (ratingQuicknessDiffRec > 0) {
                ratingQuicknessInc = ratingQuicknessInc + ratingQuicknessDiffRec
            } else {
                ratingQuicknessDec = ratingQuicknessDec - ratingQuicknessDiffRec
            }
        }


        //
        StoreRecord rating = mdb.createStoreRecord("Statistic.diff")
        //
        rating.setValue("ratingTask", ratingTask)
        rating.setValue("ratingQuickness", ratingQuickness)
        rating.setValue("ratingTaskDiff", ratingTaskDiff)
        rating.setValue("ratingQuicknessDiff", ratingQuicknessDiff)
        //
        rating.setValue("ratingTaskInc", ratingTaskInc)
        rating.setValue("ratingTaskDec", ratingTaskDec)
        rating.setValue("ratingQuicknessInc", ratingQuicknessInc)
        rating.setValue("ratingQuicknessDec", ratingQuicknessDec)
        //
        rating.setValue("wordCount", statisticByWord.keySet().size())


        //
        return rating
    }

/*
    StoreRecord summStatistic(Store st) {
        StoreRecord rating = mdb.createStoreRecord("Statistic.diff")

        for (StoreRecord rec : st) {
            rating.setValue("ratingTask", rating.getDouble("ratingTask") + rec.getDouble("ratingTask"))
            rating.setValue("ratingQuickness", rating.getDouble("ratingQuickness") + rec.getDouble("ratingQuickness"))
            //
            rating.setValue("ratingTaskDiff", rating.getDouble("ratingTaskDiff") + rec.getDouble("ratingTaskDiff"))
            if (rec.getDouble("ratingTaskDiff") > 0) {
                rating.setValue("ratingTaskInc", rating.getDouble("ratingTaskInc") + rec.getDouble("ratingTaskDiff"))
            } else {
                rating.setValue("ratingTaskDec", rating.getDouble("ratingTaskDec") - rec.getDouble("ratingTaskDiff"))
            }
            //
            rating.setValue("ratingQuicknessDiff", rating.getDouble("ratingQuicknessDiff") + rec.getDouble("ratingQuicknessDiff"))
            if (rec.getDouble("ratingQuicknessDiff") > 0) {
                rating.setValue("ratingQuicknessInc", rating.getDouble("ratingQuicknessInc") + rec.getDouble("ratingQuicknessDiff"))
            } else {
                rating.setValue("ratingQuicknessDec", rating.getDouble("ratingQuicknessDec") - rec.getDouble("ratingQuicknessDiff"))
            }
        }

        return rating
    }
*/


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
-- Все задания, выданные пользователю за указанный период
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
    Game.dbeg <= :dend and 
    GameUsr.usr = :usr and
    GameTask.dtTask is not null 
"""
    }

    String sqlStatistic() {
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
    Game.dbeg <= :dend and 
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
