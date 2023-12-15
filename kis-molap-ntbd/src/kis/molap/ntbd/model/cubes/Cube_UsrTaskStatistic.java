package kis.molap.ntbd.model.cubes;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.commons.variant.*;
import jandcode.core.store.*;
import kis.molap.model.coord.*;
import kis.molap.model.coord.impl.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;
import kis.molap.model.service.*;
import kis.molap.model.value.*;

import java.util.*;

public class Cube_UsrTaskStatistic extends CubeCustom implements ICalcData {

    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create();

        List<String> tables = UtCnv.toList(
                "OrgAttr"
        );

        Period cubeRange = ageManager.getMinMaxDt(cubeInfo);

        for (String table : tables) {
            List<IVariantMap> audit = auditManager.loadAudit(table, auditAgeFrom, auditAgeTo);

            for (Map<String, Object> dataMap : audit) {
                Long org = UtCnv.toLong(dataMap.get("org"));
                XDate dbeg = UtCnv.toDate(dataMap.get("dbeg"));
                XDate dend = UtCnv.toDate(dataMap.get("dend"));
                Period period = new Period(dbeg, dend);
                if (!cubeRange.isCross(period)) {
                    continue;
                }
                cubeRange.truncPeriod(period);
                Coord coord = Coord.create();
                coord.put("org", org);
                coord.put("dt", period);
                coords.add(coord);
            }
        }

        return coords;
    }

    public void calc(Iterable<Coord> coords, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res) throws Exception {

        logCube.logStepStart();

        Map params = UtCnv.toMap(
                "dbeg", intervalDbeg,
                "dend", intervalDend
        );

        if (coords == null) {
            CubeService cubeService = mdb.getModel().bean(CubeService.class);
            Space space = cubeService.createSpace(cubeInfo.getSpace(), mdb);
            coords = space.getCoordsForInterval(intervalDbeg, intervalDend);
        }

        for (Coord coord : coords) {
            long usr = UtCnv.toLong(coord.get("usr"));
            long task = UtCnv.toLong(coord.get("task"));
            params.put("usr", usr);

            Store storeByOrg = mdb.loadQuery(sqlOrgProdDirectValues(), params);

            for (StoreRecord rec : storeByOrg) {
                CalcResult calcResult = new CalcResult();

                Coord newCoord = Coord.create();
                newCoord.put("usr", usr);
                newCoord.put("task", task);
                calcResult.coord = newCoord;

                ValueSingle valueSingle = ValueSingle.create();
                double progress= 0;
                valueSingle.put("progress", progress);

                calcResult.value = valueSingle;

                res.addValue(calcResult);

                logCube.logStepStep();
            }
        }
    }

    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }

    private String sqlOrgProdDirectValues() {
        String sqlQueryText = """
                                
                """;

        return sqlQueryText;
    }
}
