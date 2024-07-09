package run.game.dao.statistic

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.core.apx.dbm.sqlfilter.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import kis.molap.ntbd.model.cubes.*
import run.game.dao.*
import run.game.dao.game.*

/**
 * Показывает статистику за период в разрезе: слов, игр, уровней
 */
class Statistic_list extends RgmMdbUtils {


    public String groupByKey = null
    public Store stItems = null
    public Store stStatistic = null
    public Map<String, Map> statistic = null
    public Map<String, Map> statisticByWord = null

    /**
     *
     */
    @DaoMethod
    DataBox byPlan(XDate dbeg, XDate dend) {
        // Параметры
        Map params = prepareParams(dbeg, dend)

        // Загружаем
        return byInternal(params, "plan")
    }

    @DaoMethod
    DataBox byGame(XDate dbeg, XDate dend) {
        // Параметры
        Map params = prepareParams(dbeg, dend)

        // Загружаем
        return byInternal(params, "game")
    }

    @DaoMethod
    DataBox byWord(XDate dbeg, XDate dend) {
        // Параметры
        Map params = prepareParams(dbeg, dend)

        // Загружаем
        DataBox res = byInternal(params, "word")

        // Дополним факты в плане "богатыми" данными для вопроса и ответа
        Store st = res.get("items")
        ServerImpl serverImpl = mdb.create(ServerImpl)
        serverImpl.fillFactBody(st)

        //
        return res
    }

    @DaoMethod
    prepareForGame(XDateTime dbeg, XDateTime dend) {
        // Параметры
        long idUsr = getContextOrCurrentUsrId()
        XDateTime paramDbeg = dbeg
        XDateTime paramDend = dend
        Map params = [usr: idUsr, dbeg: paramDbeg, dend: paramDend]

        // Загружаем
        byInternal(params, "word")
    }

    @DaoMethod
    prepareForPlan(long idPlan, XDateTime dbeg, XDateTime dend) {
        // Параметры
        long idUsr = getContextOrCurrentUsrId()
        Map params = [plan: idPlan, usr: idUsr, dbeg: dbeg, dend: dend]

        // Загружаем
        byInternal(params, "day")
    }

    Map prepareParams(XDate dbeg, XDate dend) {
        long idUsr = getContextOrCurrentUsrId()
        XDateTime paramDbeg = dbeg.toDateTime()
        XDateTime paramDend = dend.toDateTime().addDays(1).addMSec(-1000)
        Map params = [usr: idUsr, dbeg: paramDbeg, dend: paramDend]

        return params
    }

    /**
     * Период с "2024-10-25" по "2024-10-25" это "один день"
     * @param dbeg Начало периода
     * @param dend Окончание периода
     * @param groupByKey
     * @return
     */
    DataBox byInternal(Map params, String groupByKey) {
        this.groupByKey = groupByKey

        // ---
        // Загружаем
        stItems = mdb.createStore("Statistic." + groupByKey)
        String sql = getSqlItems(groupByKey, params)
        mdb.loadQuery(stItems, sql, params)

        // Загружаем статистику по фактам.
        // Для всех игр, сыгранных за указанный период.
        // ВАЖНО! Ключевая зависимость: Статистика расчитывается кубом Cube_UsrGameStatistic.
        // Рассчитываем, что куб на каждую игру содержит статистику
        // по КАЖДОМУ выданному факту в игре, по состоянию на момент ОКОНЧАНИЯ ИГРЫ.
        // В противном случае (если куб пуст) для факта будет показана пустая
        // или неправильная статистика.
        SqlFilter filter = SqlFilter.create(mdb, sqlStatistic(), params)
        filter.addWhere("plan", "equal")
        stStatistic = filter.load()

        // ---
        // Статистику по фактам агрегируем по groupByKey
        statistic = groupStatisticBy(stStatistic, groupByKey)

        // Распределяем агрегированную статистику в список
        distributeStatistic(statistic, stItems, groupByKey)


        //////////////////////////
        println()
        println("Statistic raw:")
        mdb.outTable(stStatistic, 10)
        //////////////////////////
        println()
        println("Statistic group by " + groupByKey + ":")
        int n = 0
        for (String key : statistic.keySet()) {
            println(groupByKey + ": " + key + ",  statistic: " + statistic.get(key))
            //
            n++
            if (n > 10) {
                break
            }
        }
        //////////////////////////
        println()
        println("Result by " + groupByKey + ":")
        mdb.outTable(stItems, 10)
        //////////////////////////


        // ---
        // Сумма по статистике

        // Статистику по фактам агрегируем по фактам
        statisticByWord = groupStatisticBy(stStatistic, "word")

        // Суммируем статистику по фактам
        StoreRecord statisticSumm = summStatisticByWord(statisticByWord)


        // ---
        DataBox res = new DataBox()
        res.put("items", stItems)
        res.put("rating", statisticSumm)

        //
        return res
    }

    String getSqlItems(String groupByKey, Map params) {
        switch (groupByKey) {
            case "day": {
                return sqlForPlanByDay(params)
            }
            case "plan": {
                return sqlPlan()
            }
            case "game": {
                return sqlGame()
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
        // Копим статистику из BLOB по записям stStatistic
        Map<String, Map> resStatistic = new HashMap<>()

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
        for (StoreRecord rec : st) {
            // Значение ключа для группы groupByKey
            Map recValues = rec.getValues()
            String recKey = createKey(recValues, groupByKey)

            // Берем статистику для ключа группы
            Map groupStatistic = statistic.get(recKey)

            // Если статистики нет, то это означает, что игра еще не завершилась
            if (groupStatistic == null) {
                continue
            }

            // Суммируем по всем словам внутри группы
            Map sumStatistic = summStatistic(groupStatistic)

            // Сумму в запись
            rec.setValue("ratingTaskDiff", UtCnv.toDouble(sumStatistic.get("ratingTaskDiff")))
            rec.setValue("ratingQuicknessDiff", UtCnv.toDouble(sumStatistic.get("ratingQuicknessDiff")))
            if (groupByKey.equals("word") || groupByKey.equals("game")) {
                rec.setValue("ratingTask", UtCnv.toDouble(sumStatistic.get("ratingTask")))
                rec.setValue("ratingQuickness", UtCnv.toDouble(sumStatistic.get("ratingQuickness")))
            }
        }
    }

    Map<String, Double> summStatistic(Map<String, Map> statistic) {
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
        // Сумма
        double ratingTask = 0
        double ratingQuickness = 0
        double ratingTaskDiff = 0
        double ratingQuicknessDiff = 0
        int wordCountLearned = 0

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
            if (ratingTaskRec == UtCubeRating.RATING_FACT_MAX) {
                wordCountLearned = wordCountLearned + 1
            }

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
        rating.setValue("wordCountRepeated", statisticByWord.keySet().size())
        rating.setValue("wordCountLearned", wordCountLearned)
        //rating.setValue("wordCount", 39)


        //
        return rating
    }


    String createKey(Map keyValues, String groupByKey) {
        String key

        XDate dbeg = UtCnv.toDate(keyValues.get("dbeg"))
        long plan = UtCnv.toLong(keyValues.get("plan"))
        long game = UtCnv.toLong(keyValues.get("game"))
        long factQuestion = UtCnv.toLong(keyValues.get("factQuestion"))
        long factAnswer = UtCnv.toLong(keyValues.get("factAnswer"))

        switch (groupByKey) {
            case "day": {
                key = dbeg
                break
            }
            case "plan": {
                key = plan
                break
            }
            case "game": {
                key = game
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
    GameTask.dtTask is not null and 
    GameUsr.usr = :usr and
    Game.dbeg >= :dbeg and
    Game.dbeg <= :dend 
"""
    }


    String sqlPlan() {
        return """
with LstBase as (
${sqlBase()}
)


select 
    LstBase.plan,
    Plan.text as planText,
    
    Cube_UsrPlan.count,
    Cube_UsrPlan.countFull,
    Cube_UsrPlan.ratingTask,
    Cube_UsrPlan.ratingQuickness

from
    LstBase 
    join Plan on (
        LstBase.plan = Plan.id
    )
    left join Cube_UsrPlan on (Cube_UsrPlan.plan = Plan.id and Cube_UsrPlan.usr = :usr)

group by
    LstBase.plan,
    Plan.text,
    
    Cube_UsrPlan.count,
    Cube_UsrPlan.countFull,
    Cube_UsrPlan.ratingTask,
    Cube_UsrPlan.ratingQuickness

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


    String sqlForPlanByDay(Map params) {
        return """
SELECT
    day.date dbeg
from
    generate_series(date '${params.get("dbeg")}', date '${params.get("dend")}', '1 day') day
order by
    day.date
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
    GameUsr.usr = :usr and 
    Game.dbeg >= :dbeg and
    Game.dbeg <= :dend 
    
order by    
    Game.dbeg 
"""
    }

}
