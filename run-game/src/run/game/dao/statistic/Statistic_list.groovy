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
        // Загружаем ---
        long idUsr = getContextOrCurrentUsrId()
        Map params = [usr: idUsr, dbeg: dbeg, dend: dend]

        // Загружаем список
        Store st = mdb.createStore("Statistic." + groupByKey)
        mdb.loadQuery(st, getSql(groupByKey), params)

        // Загружаем статистику
        Store stStatistic = mdb.loadQuery(sqlStatistic(), params)


        // ---
        // Статистику по словам агрегируем по groupByKey
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

    String getSql(String groupByKey){
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
        Set<String> setWords = new HashSet<>()

/*
        /////////////////
        println()
        println("stStatistic")
        for (StoreRecord rec : stStatistic) {
            mdb.outTable(rec)
            List<Map> listTaskStatistic = UtJson.fromJson(new String(rec.getValue("taskStatistic"), "utf-8"))
            println(listTaskStatistic)
            println()
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


                // Отдельно - количество слов.
                // Уже было такое слово?
                if (!setWords.contains(keyByWord)) {
                    setWords.add(keyByWord)
                }

            }

        }


        // Отдельно - количество слов
        //resStatistic.put("wordCount", setWords.size())


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
            // Значение ключа группы
            Map keyValues = rec.getValues()
            String key = createKey(keyValues, groupByKey)

            // Берем статистику для ключа группы
            Map groupStatistic = statistic.get(key)

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

    StoreRecord summStatisticByWord(Map<String, Map> statistic) {
/*
        ////////////////
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


        StoreRecord rating = mdb.createStoreRecord("Statistic.diff")

        for (String key : statistic.keySet()) {
            Map statisticWord = statistic.get(key).get(key)

            //
            int wordCount = UtCnv.toInt(statisticWord.get("wordCount"))
            rating.setValue("wordCount", wordCount + 1)

            //
            double ratingTask = UtCnv.toDouble(statisticWord.get("ratingTask"))
            double ratingQuickness = UtCnv.toDouble(statisticWord.get("ratingQuickness"))
            double ratingTaskDiff = UtCnv.toDouble(statisticWord.get("ratingTaskDiff"))
            double ratingQuicknessDiff = UtCnv.toDouble(statisticWord.get("ratingQuicknessDiff"))

            //
            rating.setValue("ratingTask", ratingTask + rating.getDouble("ratingTask"))
            rating.setValue("ratingQuickness", ratingQuickness + rating.getDouble("ratingQuickness"))
            rating.setValue("ratingTaskDiff", ratingTaskDiff + rating.getDouble("ratingTaskDiff"))
            rating.setValue("ratingQuicknessDiff", ratingQuicknessDiff + rating.getDouble("ratingQuicknessDiff"))

            //
            double ratingTaskInc = UtCnv.toDouble(statisticWord.get("ratingTaskInc"))
            double ratingTaskDec = UtCnv.toDouble(statisticWord.get("ratingTaskDec"))
            double ratingQuicknessInc = UtCnv.toDouble(statisticWord.get("ratingQuicknessInc"))
            double ratingQuicknessDec = UtCnv.toDouble(statisticWord.get("ratingQuicknessDec"))
            //
            if (ratingTaskDiff > 0) {
                rating.setValue("ratingTaskInc", ratingTaskInc + ratingTaskDiff)
            } else {
                rating.setValue("ratingTaskDec", ratingTaskDec - ratingTaskDiff)
            }
            //
            if (ratingQuicknessDiff > 0) {
                rating.setValue("ratingQuicknessInc", ratingQuicknessInc + ratingQuicknessDiff)
            } else {
                rating.setValue("ratingQuicknessDec", ratingQuicknessDec - ratingQuicknessDiff)
            }
        }

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
