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

public class Cube_UsrTaskStatistic extends CubeCustom implements ICalcData {


    int RECOMENDED_REPEAT_COUNT = 3

    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create()

        List<String> tables = UtCnv.toList(
                "GameTask",
        )

        for (String table : tables) {
            List<IVariantMap> audit = auditManager.loadAudit(table, auditAgeFrom, auditAgeTo)

            for (Map<String, Object> dataMap : audit) {
                Long usr = UtCnv.toLong(dataMap.get("usr"))
                Long task = UtCnv.toLong(dataMap.get("task"))
                Coord coord = Coord.create()
                coord.put("usr", usr)
                coord.put("task", task)
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
        for (Coord coord : coords) {
            long usr = UtCnv.toLong(coord.get("usr"))
            long task = UtCnv.toLong(coord.get("task"))

            //
            params.put("usr", usr)
            params.put("task", task)
            StoreRecord rec = mdb.loadQueryRecord(StatisticManager_cube.sql_TaskStatistic, params)
            //mdb.outTable(rec)

            //
            double progress = 0
            progress = progress + 1 * (rec.getDouble("cntTrue") - RECOMENDED_REPEAT_COUNT) / rec.getDouble("cnt")
            progress = progress + 3 * rec.getDouble("cntTrue") / rec.getDouble("cnt")
            progress = progress - 3 * rec.getDouble("cntFalse") / rec.getDouble("cnt")
            progress = progress - 1 * rec.getDouble("cntHint") / rec.getDouble("cnt")
            progress = progress - 1 * rec.getDouble("cntSkip") / rec.getDouble("cnt")
            progress = CubeUtils.discardExtraDigits(progress, 3)

            //
            CalcResult calcResult = new CalcResult()

            //
            Coord newCoord = Coord.create()
            newCoord.put("usr", usr)
            newCoord.put("task", task)
            calcResult.coord = newCoord

            //
            ValueSingle valueSingle = ValueSingle.create()
            valueSingle.put("progress", progress)
            calcResult.value = valueSingle

            //
            res.addValue(calcResult)

            //
            logCube.logStepStep()
        }
    }


    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }

}
