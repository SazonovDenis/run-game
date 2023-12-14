package kis.molap.ntbd.model.cubes;

import jandcode.commons.datetime.*;
import kis.molap.model.coord.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;
import kis.molap.model.value.*;

public class Cube_Dates extends CubeCustom implements ICalcData, ICalcCoords {


    //---
    // ICalcData
    //---

    public void calc(Iterable<Coord> coords, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res) throws Exception {
        //
        logCube.logStepStart();

        XDate dt = intervalDbeg;
        while (dt.compareTo(intervalDend) <= 0) {
            Coord coord = Coord.create();
            coord.put("dt", dt);
            //
            ValueSingle value = ValueSingle.create();
            value.put("dt", dt);
            //
            CalcResult rec = new CalcResult();
            rec.coord = coord;
            rec.value = value;
            //
            res.addValue(rec);
            //
            dt = dt.addDays(1);

            //
            logCube.logStepStep();
        }
    }


    //---
    // ICalcCoords
    //---


    @Override
    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        // Не имеет изменений
        CoordList coordsRes = CoordList.create();

        //
        return coordsRes;
    }


}