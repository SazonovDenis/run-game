package kis.molap.ntbd.model.spaces;

import jandcode.commons.datetime.*;
import kis.molap.model.coord.*;
import kis.molap.model.coord.impl.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;

public class Space_Dates extends SpaceCustom implements Space {


    // ---
    // ISpace
    // ---

    public CoordList getCoordsForInterval(XDate intervalDbeg, XDate intervalDend) throws Exception {
        return calcExpandCoordForInterval_internal(intervalDbeg, intervalDend);
    }


    // ---
    // Внутренние утилиты
    // ---

    private CoordList calcExpandCoordForInterval_internal(XDate intervalDbeg, XDate intervalDend) throws Exception {
        CoordList list = CoordList.create();

        //
        Period dt = new Period(intervalDbeg, intervalDend);
        Coord coord = Coord.create();
        coord.put("dt", dt);

        //
        list.add(coord);

        //
        return list;
    }


}
