package kis.molap.ntbd.model.spaces;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.core.db.*;
import kis.molap.model.coord.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;

public class Space_UsrPlan extends SpaceCustom implements Space {


    // ---
    // ISpace
    // ---

    @Override
    public CoordList getCoordsForInterval(XDate intervalDbeg, XDate intervalDend) throws Exception {
        return calcExpandCoordForInterval_internal(intervalDbeg, intervalDend);
    }


    // ---
    // Внутренние утилиты
    // ---

    private CoordList calcExpandCoordForInterval_internal(XDate intervalDbeg, XDate intervalDend) throws Exception {
        CoordList list = CoordList.create();

        // Создадим список usr,plan
        DbQuery query = mdb.openQuery(
                "select distinct GameUsr.usr, Game.plan from GameUsr join Game on (GameUsr.game = Game.id) order by GameUsr.usr, Game.plan",
                UtCnv.toMap("dbeg", intervalDbeg, "dend", intervalDend)
        );
        try {
            while (!query.eof()) {
                Coord coord = Coord.create();
                //
                coord.put("usr", query.getLong("usr"));
                coord.put("plan", query.getLong("plan"));
                //
                list.add(coord);

                query.next();
            }
        } finally {
            query.close();
        }

        //
        return list;
    }


}
