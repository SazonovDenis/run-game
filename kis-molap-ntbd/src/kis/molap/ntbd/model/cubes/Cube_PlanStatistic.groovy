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
import kis.molap.model.util.UtMolapCube
import kis.molap.model.value.*
import kis.molap.ntbd.model.*

public class Cube_PlanStatistic extends CubeCustom implements ICalcData {


    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create()

        List<String> tables = UtCnv.toList(
                "PlanTask"
        )

        for (String table : tables) {
            List<IVariantMap> audit = auditManager.loadAudit(table, auditAgeFrom, auditAgeTo)

            for (Map<String, Object> dataMap : audit) {
                Long plan = UtCnv.toLong(dataMap.get("plan"))
                Coord coord = Coord.create()
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

        //
        Store st = mdb.loadQuery(sql_PlanStatistic(coords), params)
        //mdb.outTable(st)

        //
        for (StoreRecord rec : st) {
            long plan = rec.getLong("plan")
            long cnt = rec.getLong("cnt")

            //
            Coord newCoord = Coord.create()
            newCoord.put("plan", plan)

            //
            ValueSingle valueSingle = ValueSingle.create()
            valueSingle.put("cnt", cnt)

            //
            CalcResult calcResult = new CalcResult()
            calcResult.coord = newCoord
            calcResult.value = valueSingle

            //
            res.addValue(calcResult)

            //
            logCube.logStepStep()
        }
    }

    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }

    String sql_PlanStatistic(Iterable<Coord> coords) {
        String where_str
        if (coords != null) {
            String ids = UtString.join(UtMolapCube.coordsValueToList(coords, "plan"), ",")
            where_str = "and PlanTask.plan in (" + ids + ")"
        } else {
            where_str = ""
        }

        return """     

-- Список всех заданий в плане (со статистикой)
select
    PlanTask.plan, 
    count(*) cnt 

from
    PlanTask

where
    (1=1)
    ${where_str}
    
group by
    PlanTask.plan 
"""
    }

}
