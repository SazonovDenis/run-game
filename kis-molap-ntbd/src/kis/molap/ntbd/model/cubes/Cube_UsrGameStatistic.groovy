package kis.molap.ntbd.model.cubes

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.variant.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.model.cube.*
import kis.molap.model.cube.impl.*
import kis.molap.model.service.*
import kis.molap.model.value.*
import kis.molap.ntbd.model.*

/**
 * Зависимости от GameUsr нет, а только от добавления GameTask, т.к. предполагаем, что
 *  - поля в GameUsr и GameTask никогда не редактируются
 *  - записи в GameTask и GameUsr только добавляются
 *  - записи в GameTask добавляются по ходу игры
 * Таким образом, для отслеживания изменений в статистике игры для пользователя
 * достаточно отследить только добавления в GameTask.
 */
public class Cube_UsrGameStatistic extends CubeCustom implements ICalcData {


    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create()

        List<String> tables = UtCnv.toList(
                "GameTask",
        )

        for (String table : tables) {
            List<IVariantMap> audit = auditManager.loadAudit(table, auditAgeFrom, auditAgeTo)

            for (Map<String, Object> dataMap : audit) {
                Long usr = UtCnv.toLong(dataMap.get("usr"))
                Long game = UtCnv.toLong(dataMap.get("game"))
                Coord coord = Coord.create()
                coord.put("usr", usr)
                coord.put("game", game)
                coords.add(coord)
            }
        }

        return coords
    }

    public void calc(Iterable<Coord> coords, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res) throws Exception {
        Map params = UtCnv.toMap(
                "dbeg", intervalDbeg,
                "dend", intervalDend
        )

        //
        if (coords == null) {
            CubeService cubeService = mdb.getModel().bean(CubeService.class)
            Space space = cubeService.createSpace(cubeInfo.getSpace(), mdb)
            coords = space.getCoordsForInterval(intervalDbeg, intervalDend)
        }

        //
        if (coords instanceof Collection) {
            logCube.logStepStart(coords.size())
        } else if (coords instanceof Map) {
            logCube.logStepStart(coords.size())
        } else {
            logCube.logStepStart()
        }

        //
        for (Coord coord : coords) {
            long usr = UtCnv.toLong(coord.get("usr"))
            long game = UtCnv.toLong(coord.get("game"))


            // ---
            params.put("usr", usr)
            params.put("game", game)

            //
            StoreRecord recGameRec = mdb.loadQueryRecord(sqlGameRec(), params)
            XDateTime gameDbeg = recGameRec.getDateTime("dbeg")
            XDateTime gameDend = recGameRec.getDateTime("dend")

            //
            StoreRecord recGameTaskCount = mdb.loadQueryRecord(sqlGameTaskCount(), params)

            // Период анализа ответов за игру game - от некоторого количества дней назад до конца игры
            params.put("dbeg", gameDend.addDays(-Cube_UsrFactStatistic.RAITING_ANALYZE_DAYS_DIFF))
            params.put("dend", gameDend)
            Store stGameTask = mdb.loadQuery(sqlGameTasks(), params)

            // Период анализа ответов ДО игры game - от некоторого количества дней назад до НАЧАЛА игры
            params.put("dbeg", gameDbeg.addDays(-Cube_UsrFactStatistic.RAITING_ANALYZE_DAYS_DIFF))
            params.put("dend", gameDbeg.addMSec(-1000))
            Store stGameTaskPrior = mdb.loadQuery(sqlGameTasks(), params)

            // Собираем рейтинг фактов в игре
            UtCubeRating utCubeRating = new UtCubeRating()

            // Рейтинг фактов в ТЕКУЩЕЙ игре
            Map<String, Map> taskStatistic = new HashMap<>()
            int pos = 0
            while (pos < stGameTask.size()) {
                Map resMap = new HashMap()
                pos = utCubeRating.stepCollectRaiting(stGameTask, pos, resMap)
                //
                String key = resMap.get("factQuestion") + "_" + resMap.get("factAnswer")
                taskStatistic.put(key, resMap)
            }

            // Рейтинг фактов в ПРЕДЫДУЩЕЙ игре
            Map<String, Map> taskStatisticPrior = new HashMap<>()
            pos = 0
            while (pos < stGameTaskPrior.size()) {
                Map resMap = new HashMap()
                pos = utCubeRating.stepCollectRaiting(stGameTaskPrior, pos, resMap)
                //
                String key = resMap.get("factQuestion") + "_" + resMap.get("factAnswer")
                taskStatisticPrior.put(key, resMap)
            }

            // Сравниваем рейтинг ДО и ПОСЛЕ игры
            for (String key : taskStatistic.keySet()) {
                Map resMap = taskStatistic.get(key)
                double ratingTask = UtCnv.toDouble(resMap.get("ratingTask"))
                double ratingQuickness = UtCnv.toDouble(resMap.get("ratingQuickness"))
                //
                Map resMapPrior = taskStatisticPrior.get(key)
                if (resMapPrior != null) {
                    double ratingTaskPrior = UtCnv.toDouble(resMapPrior.get("ratingTask"))
                    double ratingQuicknessPrior = UtCnv.toDouble(resMapPrior.get("ratingQuickness"))
                    resMap.put("ratingTaskDiff", CubeUtils.discardExtraDigits(ratingTask - ratingTaskPrior))
                    resMap.put("ratingQuicknessDiff", CubeUtils.discardExtraDigits(ratingQuickness - ratingQuicknessPrior))
                } else {
                    // Нет предыдущей записи - значит весь рост - текущий
                    resMap.put("ratingTaskDiff", CubeUtils.discardExtraDigits(ratingTask))
                    resMap.put("ratingQuicknessDiff", CubeUtils.discardExtraDigits(ratingQuickness))
                }
            }


            // ---
            CalcResult calcResult = new CalcResult()

            //
            Coord newCoord = Coord.create()
            newCoord.put("usr", usr)
            newCoord.put("game", game)
            calcResult.coord = newCoord

            //
            ValueSingle valueSingle = ValueSingle.create()
            valueSingle.put("cntTask", recGameTaskCount.getLong("cntTask"))
            valueSingle.put("cntAsked", recGameTaskCount.getLong("cntAsked"))
            valueSingle.put("cntAnswered", recGameTaskCount.getLong("cntAnswered"))
            valueSingle.put("cntTrue", recGameTaskCount.getLong("cntTrue"))
            valueSingle.put("cntFalse", recGameTaskCount.getLong("cntFalse"))
            valueSingle.put("cntHint", recGameTaskCount.getLong("cntHint"))
            valueSingle.put("cntSkip", recGameTaskCount.getLong("cntSkip"))
            valueSingle.put("taskStatistic", UtJson.toJson(taskStatistic.values()))
            calcResult.value = valueSingle

            //
            res.addValue(calcResult)

            //
            logCube.logStepStep()
        }
    }


    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }


    String sqlGameRec() {
        return """
select
    Game.*
from
    Game
    join GameUsr on (Game.id = GameUsr.game)
where
    Game.id = :game and
    GameUsr.usr = :usr
"""
    }

    String sqlGameTaskCount() {
        """                                       
with 

-- Список заданий в игре
Tab_GameTaskList as (
 
select
    GameTask.*,
    (case when GameTask.dtTask is not null then 1 else 0 end) wasAsked,
    (case when GameTask.dtAnswer is not null then 1 else 0 end) wasAnswered,
    (extract('epoch' from GameTask.dtAnswer) - extract('epoch' from GameTask.dtTask)) as answerDuration

from
    GameTask
    
where
    GameTask.game = :game and 
    GameTask.usr = :usr 
    
order by
    GameTask.dtTask,
    GameTask.id
)

  
-- Статистика по заданиям игры
select 
    count(Tab_GameTaskList.id) cntTask,
    sum(Tab_GameTaskList.wasAsked) cntAsked,
    sum(Tab_GameTaskList.wasAnswered) cntAnswered,
    sum(Tab_GameTaskList.wasTrue) cntTrue,
    sum(Tab_GameTaskList.wasFalse) cntFalse,
    sum(Tab_GameTaskList.wasHint) cntHint,
    sum(Tab_GameTaskList.wasSkip) cntSkip,
    sum(Tab_GameTaskList.answerDuration) answerDuration

from
    Tab_GameTaskList
"""
    }

    String sqlGameFacts() {
        return """
-- Список заданий для пользователя в игре
select
    Task.factQuestion,
    Task.factAnswer
     
from 
    GameTask
    join Task on (
        GameTask.task = Task.id
    ) 
     
where
    GameTask.game = :game and
    GameTask.usr = :usr
     
group by
    Task.factQuestion,
    Task.factAnswer
"""
    }

    String sqlGameTasks() {
        return """          
with 

Tab_GameTasks as (
${sqlGameFacts()}
)


-- Для фактов из заданной игры получим список заданий из разных других игр за указанный период 
select
    GameTask.*,
    Task.factQuestion,
    Task.factAnswer
     
from 
    Tab_GameTasks
    join Task on (
        Task.factQuestion = Tab_GameTasks.factQuestion and
        Task.factAnswer = Tab_GameTasks.factAnswer
    )
    join GameTask on (
        GameTask.task = Task.id and
        GameTask.usr = :usr and
        GameTask.dtTask >= :dbeg and 
        GameTask.dtTask <= :dend
    )
     
order by
    Task.factQuestion,
    Task.factAnswer,
    GameTask.dtTask desc,
    GameTask.game desc
"""
    }


}
