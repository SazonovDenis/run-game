package kis.molap.ntbd.model.spaces;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.core.db.*;
import kis.molap.model.coord.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;

public class Space_Plan extends SpaceCustom implements Space {


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

        // Создадим список plan
        DbQuery query = mdb.openQuery(
                "select id plan from Plan",
                UtCnv.toMap("dbeg", intervalDbeg, "dend", intervalDend)
        );
        try {
            while (!query.eof()) {
                Coord coord = Coord.create();
                //
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
