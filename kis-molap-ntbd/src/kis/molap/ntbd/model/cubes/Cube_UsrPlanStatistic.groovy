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
 * Зависимости от Game и PlanTask нет, а только от GameTask, т.к. предполагаем, что
 *  - поле Game.plan никогда не редактируется
 *  - состав PlanTask никогда не меняется
 * Таким образом, для отслеживания изменений в статистике плана для пользователя
 * достаточно отследить только добавления в GameTask.
 *
 * Если в PlanTask будут внесены изменения, это повлечет необходимость пересчитать
 * статистику для всех пользователей (что долго), поэтому не будем допускать изменения
 * состава заданий в PlanTask.
 *
 * Редактирование Game.plan вовсе бессмысленная операция.
 */
public class Cube_UsrPlanStatistic extends CubeCustom implements ICalcData {


    static double LAST_PERCENTILE = 999
    static List progressPercentiles = [-4.5, -1.5, -0.5, 1, LAST_PERCENTILE]

    /**
     * Предполагаемое худшее значение progress.
     * Нужно для тех планов, которые никогда не запускались
     */
    public static double PROGRESS_MIN = -5
    public static double PROGRESS_MAX = +4

    public static List getTasksStatisticDummy() {
        List tasksStatistic = []
        for (double progressPercentile : progressPercentiles) {
            tasksStatistic.add([percentile: progressPercentile, count: 0])
        }
        return tasksStatistic
    }

    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create()

        List<String> tables = UtCnv.toList(
                "GameTask",
        )

        Map<Long, Long> cachePlanByGame = new HashMap<>()

        for (String table : tables) {
            List<IVariantMap> audit = auditManager.loadAudit(table, auditAgeFrom, auditAgeTo)

            for (Map<String, Object> dataMap : audit) {
                long usr = UtCnv.toLong(dataMap.get("usr"))
                long game = UtCnv.toLong(dataMap.get("game"))
                long plan = getPlanByGame(game, cachePlanByGame)
                Coord coord = Coord.create()
                coord.put("usr", usr)
                coord.put("plan", plan)
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
                double progress = PROGRESS_MIN
                if (!rec.isValueNull("progress")) {
                    progress = rec.getDouble("progress")
                }
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
            List tasksStatistic = []
            for (double progressPercentile : progressPercentiles) {
                int countForPercentile = UtCnv.toInt(countByPercentiles.get(progressPercentile))
                tasksStatistic.add([percentile: progressPercentile, count: countForPercentile])
            }

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
            String tasksStatisticJson = UtJson.toJson(tasksStatistic)
            valueSingle.put("tasksStatistic", tasksStatisticJson)
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
    PlanTask.plan, 
    PlanTask.task, 
    -- сколько раз выдавалось задание 
    count(GameTask.id) cnt, 
    avg(Cube_UsrTask.progress) progress 

from
    PlanTask
    left join GameTask on (
        PlanTask.id = GameTask.task and 
        GameTask.usr = :usr
    )
    left join Cube_UsrTask on (
        GameTask.usr = Cube_UsrTask.usr and 
        GameTask.task = Cube_UsrTask.task 
    )

where
    PlanTask.plan = :plan 
    
group by
    GameTask.usr, 
    PlanTask.plan, 
    PlanTask.task 

order by
    GameTask.usr, 
    PlanTask.plan
"""

    long getPlanByGame(long game, HashMap<Long, Long> cachePlanByGame) {
        if (cachePlanByGame.containsKey(game)) {
            return cachePlanByGame.get(game)
        } else {
            StoreRecord recPlan = mdb.loadQueryRecord("select * from Game where id = :game", [game: game])
            long plan = recPlan.getLong("plan")
            cachePlanByGame.put(game, plan)
            return plan
        }
    }

}
