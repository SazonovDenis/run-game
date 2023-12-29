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
        StatisticManager_cube statisticManager = mdb.create(StatisticManager_cube)

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

            //
            params.put("usr", usr)
            params.put("game", game)
            StoreRecord rec = statisticManager.loadGameStatistic(game, usr)
            //StoreRecord rec = mdb.loadQueryRecord(sql_UsrGameStatistic, params)
            //mdb.outTable(rec)

            //
            CalcResult calcResult = new CalcResult()

            //
            Coord newCoord = Coord.create()
            newCoord.put("usr", usr)
            newCoord.put("game", game)
            calcResult.coord = newCoord

            //
            ValueSingle valueSingle = ValueSingle.create()
            valueSingle.put("cntTask", rec.getLong("cntTask"))
            valueSingle.put("cntAsked", rec.getLong("cntAsked"))
            valueSingle.put("cntAnswered", rec.getLong("cntAnswered"))
            valueSingle.put("cntTrue", rec.getLong("cntTrue"))
            valueSingle.put("cntFalse", rec.getLong("cntFalse"))
            valueSingle.put("cntHint", rec.getLong("cntHint"))
            valueSingle.put("cntSkip", rec.getLong("cntSkip"))
            calcResult.value = valueSingle

            //
            res.addValue(calcResult)

            //
            logCube.logStepStep()
        }
    }


    String sql_UsrGameStatistic = """
            ${StatisticManager_cube.sql_GameTaskList}
              
            -- Статистика по каждому заданию 
            select
                Tab_GameTaskList.usr,
                Tab_GameTaskList.game,
                count(Tab_GameTaskList.id) cnt,
                sum(Tab_GameTaskList.wasAsked) cntAsked,
                sum(Tab_GameTaskList.wasAnswered) cntAnswered,
                sum(Tab_GameTaskList.answerDuration) answerDuration,
                sum(Tab_GameTaskList.wasTrue) cntTrue,
                sum(Tab_GameTaskList.wasFalse) cntFalse,
                sum(Tab_GameTaskList.wasHint) cntHint,
                sum(Tab_GameTaskList.wasSkip) cntSkip

            from
                Tab_GameTaskList

            where
                Tab_GameTaskList.game = :game

            group by
                Tab_GameTaskList.usr,
                Tab_GameTaskList.game
                
            """

    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }

}
