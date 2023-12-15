package kis.molap.ntbd.model.spaces;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.core.db.*;
import kis.molap.model.coord.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;

public class Space_UsrTask extends SpaceCustom implements Space {


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

        // Создадим список well для интервала intervalDbeg..intervalDend (только скважины, которые работают на интервале)
        DbQuery query = mdb.openQuery(
                "select distinct well from WellStatus where dbeg <= :dend and dend >= :dbeg order by well",
                UtCnv.toMap("dbeg", intervalDbeg, "dend", intervalDend)
        );
        try {
            while (!query.eof()) {
                Coord coord = Coord.create();
                //
                long well = query.getLong("well");
                coord.put("well", well);
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
