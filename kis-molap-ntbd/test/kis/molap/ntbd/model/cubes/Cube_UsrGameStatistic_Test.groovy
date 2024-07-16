package kis.molap.ntbd.model.cubes

import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.model.coord.impl.*
import kis.molap.ntbd.model.base.*

class Cube_UsrGameStatistic_Test extends CubeBase_Test {

    void setUp() throws Exception {
        cubeName = "Cube_UsrGameStatistic"
        //
        super.setUp()
    }

    CoordList getCoords_for_byOneCoord() {
        StoreRecord rec = mdb.loadQueryRecord("select * from GameUsr order by id desc limit 1")
        long usr = rec.getLong("usr")
        long game = rec.getLong("game")

        CoordList res = new CoordListImpl()
        Coord coord = new CoordImpl()
        coord.put("usr", usr)
        coord.put("game", game)
        res.add(coord)

        return res
    }

}
