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
 * Зависимости от GameUsr нет, а только от GameTask, т.к. предполагаем, что
 *  - поля в GameUsr никогда не редактируются
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


            List taskStatistic = new ArrayList()
            //mdb.outTable(rec)

            StoreRecord recGameTask = mdb.loadQueryRecord(sqlGameTaskCount(), params)
            //mdb.outTable(recGameTask)

            //
            //Store stGameTask = mdb.loadQuery(sqlGameTasks(), params)
            //mdb.outTable(stGameTask)
            taskStatistic.add([xxx: "sdds", yyy: 302.4])


            // ---
            CalcResult calcResult = new CalcResult()

            //
            Coord newCoord = Coord.create()
            newCoord.put("usr", usr)
            newCoord.put("game", game)
            calcResult.coord = newCoord

            //
            ValueSingle valueSingle = ValueSingle.create()
            valueSingle.put("cntTask", recGameTask.getLong("cntTask"))
            valueSingle.put("cntAsked", recGameTask.getLong("cntAsked"))
            valueSingle.put("cntAnswered", recGameTask.getLong("cntAnswered"))
            valueSingle.put("cntTrue", recGameTask.getLong("cntTrue"))
            valueSingle.put("cntFalse", recGameTask.getLong("cntFalse"))
            valueSingle.put("cntHint", recGameTask.getLong("cntHint"))
            valueSingle.put("cntSkip", recGameTask.getLong("cntSkip"))
            valueSingle.put("taskStatistic", UtJson.toJson(taskStatistic))
            calcResult.value = valueSingle

            //
            res.addValue(calcResult)

            //
            logCube.logStepStep()
        }
    }


    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }


    String sqlGameTaskCount() {
        """                                       
-- Список заданий в плане
with Tab_GameTaskList as (
 
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

  
-- Статистика по всем заданиям
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


}
