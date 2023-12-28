package kis.molap.ntbd.model.spaces;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.core.db.*;
import kis.molap.model.coord.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;

public class Space_UsrGame extends SpaceCustom implements Space {


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

        // Создадим список usr,game
        DbQuery query = mdb.openQuery(
                "select distinct GameUsr.usr, GameUsr.game from GameUsr order by GameUsr.usr, GameUsr.game",
                UtCnv.toMap()
        );
        try {
            while (!query.eof()) {
                Coord coord = Coord.create();
                //
                coord.put("usr", query.getLong("usr"));
                coord.put("game", query.getLong("game"));
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
