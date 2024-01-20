package kis.molap.ntbd.model.spaces;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.core.db.*;
import kis.molap.model.coord.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;

public class Space_UsrFact extends SpaceCustom implements Space {


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
                "select distinct GameTask.usr, Task.factQuestion, Task.factAnswer from GameTask join Task on (GameTask.task = Task.id) order by GameTask.usr",
                UtCnv.toMap("dbeg", intervalDbeg, "dend", intervalDend)
        );
        try {
            while (!query.eof()) {
                Coord coord = Coord.create();
                //
                coord.put("usr", query.getLong("usr"));
                coord.put("factQuestion", query.getLong("factQuestion"));
                coord.put("factAnswer", query.getLong("factAnswer"));
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
