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

/**
 * Зависимости от Game нет, а только от GameTask, т.к. предполагаем, что
 *  - поле Game.plan никогда не редактируется
 * Таким образом, для отслеживания изменений в статистике плана для пользователя
 * достаточно отследить только добавления в GameTask.
 *
 * todo!!!!!Если в PlanFact будут внесены изменения, это повлечет необходимость пересчитать
 * статистику Cube_UsrPlanStatistic для всех пользователей (что долго).
 *
 * Редактирование Game.plan вовсе бессмысленная операция.
 */
public class Cube_UsrPlanStatistic extends CubeCustom implements ICalcData {


    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create()

/*
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
*/

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
            StoreRecord rec = mdb.loadQueryRecord(sqlPlanStatistic(), params)
            //mdb.outTable(rec)


            //
            CalcResult calcResult = new CalcResult()

            //
            Coord newCoord = Coord.create()
            newCoord.put("usr", usr)
            newCoord.put("plan", plan)
            calcResult.coord = newCoord

            //
            ValueSingle valueSingle = ValueSingle.create()
            valueSingle.put("count", rec.getLong("count"))
            valueSingle.put("countFull", rec.getLong("countFull"))
            valueSingle.put("ratingTask", rec.getDouble("ratingTask"))
            valueSingle.put("ratingQuickness", rec.getDouble("ratingQuickness"))
            calcResult.value = valueSingle

            //
            res.addValue(calcResult)

            //
            logCube.logStepStep()
        }
    }

    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }

    String sqlPlanStatistic() {
        """
select
    --PlanFact.plan, 
    sum(case when UsrFact.isHidden is null or UsrFact.isHidden = 0 then 1 else 0 end) count, 
    count(PlanFact.id) countFull, 
    sum(case when UsrFact.isHidden is null or UsrFact.isHidden = 0 then Cube_UsrFact.ratingTask else 0 end) ratingTask, 
    sum(case when UsrFact.isHidden is null or UsrFact.isHidden = 0 then Cube_UsrFact.ratingQuickness else 0 end) ratingQuickness

from
    PlanFact
    left join UsrFact on (
        UsrFact.usr = :usr and
        PlanFact.factQuestion = UsrFact.factQuestion and 
        PlanFact.factAnswer = UsrFact.factAnswer
    )
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        PlanFact.factQuestion = Cube_UsrFact.factQuestion and 
        PlanFact.factAnswer = Cube_UsrFact.factAnswer
    )

where
    PlanFact.plan = :plan 
    
--group by
--    PlanFact.plan 

"""
    }

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
