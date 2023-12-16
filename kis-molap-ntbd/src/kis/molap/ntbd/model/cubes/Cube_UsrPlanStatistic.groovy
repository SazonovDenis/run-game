package kis.molap.ntbd.model.cubes

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.variant.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.model.coord.impl.*
import kis.molap.model.cube.*
import kis.molap.model.cube.impl.*
import kis.molap.model.service.*
import kis.molap.model.value.*
import kis.molap.ntbd.model.*

public class Cube_UsrPlanStatistic extends CubeCustom implements ICalcData {


     static double LAST_PERCENTILE = 999
     static List progressPercentiles = [-1.5, 1.5, LAST_PERCENTILE]

    public static List getTaskInfoDummy(){
        List taskInfo = []
        for (double progressPercentile : progressPercentiles) {
            taskInfo.add([percentile: progressPercentile, count: 0])
        }
        return taskInfo
    }
    
    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create()

        List<String> tables = UtCnv.toList(
                "GameTask",
                "Game",
        )

        Period cubeRange = ageManager.getMinMaxDt(cubeInfo)

        for (String table : tables) {
            List<IVariantMap> audit = auditManager.loadAudit(table, auditAgeFrom, auditAgeTo)

/*          todo
            for (Map<String, Object> dataMap : audit) {
                Long org = UtCnv.toLong(dataMap.get("org"))
                XDate dbeg = UtCnv.toDate(dataMap.get("dbeg"))
                XDate dend = UtCnv.toDate(dataMap.get("dend"))
                Period period = new Period(dbeg, dend)
                if (!cubeRange.isCross(period)) {
                    continue
                }
                cubeRange.truncPeriod(period)
                Coord coord = Coord.create()
                coord.put("org", org)
                coord.put("dt", period)
                coords.add(coord)
            }
*/
        }

        return coords
    }

    public void calc(Iterable<Coord> coords, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res) throws Exception {
        Map params = UtCnv.toMap(
                "dbeg", intervalDbeg,
                "dend", intervalDend
        )

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

        for (Coord coord : coords) {
            long usr = UtCnv.toLong(coord.get("usr"))
            long plan = UtCnv.toLong(coord.get("plan"))

            //
            params.put("usr", usr)
            params.put("plan", plan)
            Store st = mdb.loadQuery(sql_PlanStatistic, params)
            //mdb.outTable(st)

            // --- Накопление

            // Среднее
            double progressSumm = 0
            // Кол-во по процентилям
            Map countByPercentiles = [:]
            //
            for (StoreRecord rec : st) {
                double progress = rec.getDouble("progress")
                // Среднее
                progressSumm = progressSumm + progress
                // Кол-во по процентилям
                for (double progressPercentile : progressPercentiles) {
                    if (progress <= progressPercentile) {
                        int countForPercentile = UtCnv.toInt(countByPercentiles.get(progressPercentile))
                        countForPercentile = countForPercentile + 1
                        countByPercentiles.put(progressPercentile, countForPercentile)
                        break
                    }
                }
            }

            // --- Итого
            // Среднее
            long cnt = st.size()
            double progressAvg = progressSumm / cnt
            progressAvg = CubeUtils.discardExtraDigits(progressAvg, 3)
            // Кол-во по процентилям
            List taskInfo = []
            for (double progressPercentile : progressPercentiles) {
                int countForPercentile = UtCnv.toInt(countByPercentiles.get(progressPercentile))
                taskInfo.add([percentile: progressPercentile, count: countForPercentile])
            }
            //println(taskInfo)

            //
            CalcResult calcResult = new CalcResult()

            //
            Coord newCoord = Coord.create()
            newCoord.put("usr", usr)
            newCoord.put("plan", plan)
            calcResult.coord = newCoord

            //
            ValueSingle valueSingle = ValueSingle.create()
            valueSingle.put("progress", progressAvg)
            String taskInfoJson = UtJson.toJson(taskInfo)
            valueSingle.put("taskInfo", taskInfoJson)
            calcResult.value = valueSingle

            //
            res.addValue(calcResult)

            //
            logCube.logStepStep()
        }
    }

    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }

    String sql_PlanStatistic = """
-- Список всех заданий в плане (со статистикой)
select
    GameTask.usr, 
    Game.plan, 
    GameTask.task, 
    --Cube_UsrTask.progress 
    count(GameTask.id) cnt, 
    avg(Cube_UsrTask.progress) progress 

from
    GameTask
    join Game on (GameTask.game = Game.id)
    left join Cube_UsrTask on (
        GameTask.usr = Cube_UsrTask.usr and 
        GameTask.task = Cube_UsrTask.task 
    )

where
    GameTask.usr = :usr and 
    Game.plan = :plan 
    
group by
    GameTask.usr, 
    Game.plan, 
    GameTask.task 

order by
    GameTask.usr, 
    Game.plan
    
"""

}
