package run.game.dao

import jandcode.core.dbm.mdb.BaseMdbUtils
import jandcode.core.store.Store
import jandcode.core.store.StoreRecord
import kis.molap.model.coord.Coord
import kis.molap.model.coord.CoordList
import kis.molap.model.cube.Cube
import kis.molap.model.service.CubeService
import kis.molap.model.value.impl.CalcResultStreamDb

class RgmCubeUtils extends BaseMdbUtils {

    public void cubesRecalc(long idUsr, long idGame, long idPlan) {
        // Что считаем
        Store stPlanFact = mdb.loadQuery(sqlPlanFact(), [usr: idUsr, plan: idPlan])
        CoordList coordsPlanFact = createCoords(stPlanFact, ["usr", "factQuestion", "factAnswer"])
        CoordList coordsUsrGame = createCoord([usr: idUsr, game: idGame])
        CoordList coordsUsrPlan = createCoord([usr: idUsr, plan: idPlan])

        //
        cubeRecalc("Cube_UsrFactStatistic", coordsPlanFact)
        cubeRecalc("Cube_UsrGameStatistic", coordsUsrGame)
        cubeRecalc("Cube_UsrPlanStatistic", coordsUsrPlan)
    }

    public void cubesRecalcUsrPlan(long idUsr, long idPlan) {
        // Что считаем
        CoordList coordsUsrPlan = createCoord([usr: idUsr, plan: idPlan])

        //
        cubeRecalc("Cube_UsrPlanStatistic", coordsUsrPlan)
    }


    public void cubesRecalcPlan(long idPlan) {
        // Что считаем: какие пользователи уже играли в игры по этому плану?
        Store stUsrPlan = mdb.loadQuery(sqlUsrPlan(), [plan: idPlan])
        CoordList coordsUsrPlan = createCoords(stUsrPlan, ["usr", "plan"])

        //
        cubeRecalc("Cube_UsrPlanStatistic", coordsUsrPlan)
    }


    void cubeRecalc(String cubeName, CoordList coords) {
        // Создаем куб
        CubeService cubeService = getModel().bean(CubeService)
        Cube cube = cubeService.createCube(cubeName, mdb)

        // Результат будем писать в БД
        CalcResultStreamDb res = new CalcResultStreamDb(mdb, cube.getInfo())
        res.open()

        // Расчет и запись
        cube.calc(coords, null, null, res)

        // Запись остатка
        res.close()

    }

    String sqlPlanFact(){
        return """
select
    factQuestion,
    factAnswer,
    :usr usr
from
    PlanFact
where
    plan = :plan
"""
    }

    String sqlUsrPlan(){
        return """
select
    Game.plan,
    GameUsr.usr
from
    Game 
    join GameUsr on (Game.id = GameUsr.game)
where
    Game.plan = :plan
group by
    Game.plan,
    GameUsr.usr
"""
    }

    CoordList createCoord(Map values) {
        CoordList coords = CoordList.create()

        Coord coord = Coord.create()
        for (String field : values.keySet()) {
            coord.put(field, values.get(field))
        }
        coords.add(coord)

        return coords
    }

    CoordList createCoords(Store st, ArrayList<String> fields) {
        CoordList coords = CoordList.create()

        for (StoreRecord rec : st) {
            Coord coord = Coord.create()
            for (String field : fields) {
                coord.put(field, rec.getValue(field))
            }
            coords.add(coord)
        }

        return coords
    }


}
