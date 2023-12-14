package kis.molap.ntbd.model;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.commons.datetime.impl.*;
import jandcode.core.db.*;
import jandcode.core.dbm.test.*;
import org.junit.jupiter.api.*;

import java.sql.*;
import java.util.*;

public class HeapSpace_Test extends Dbm_Test {

    public void setUp() throws Exception {
        super.setUp();
        utils.logOn();
    }

    String intervalDbegStr = "2022-01-01";
    String intervalDendStr = "2023-03-01";
    XDate intervalDbeg = XDate.create(intervalDbegStr);
    XDate intervalDend = XDate.create(intervalDendStr);

    @Test
    public void test_mdb_openQuery() throws Exception {
        Map params = UtCnv.toMap(
                "dbeg", intervalDbeg,
                "dend", intervalDend
        );

        String sql = """
                with Cube_Dates as (
                  select dt
                  from generate_series(:dbeg, :dend, interval '1 day') as dt
                  order by dt
                )

                select
                  Geo.id geo,
                  Cube_Dates.dt,
                  GeoAttr_Root.name,
                  GeoAttr_Root.geoType,
                  GeoParentIdx.level,
                  GeoParentIdx.parent,
                  GeoAttr_Parent.name as parentName,
                  GeoAttr_Parent.geoType as parentGeoType,
                  GeoData.densityOil,
                  GeoData.gasFactor
                from
                  Cube_Dates
                  join Geo on (1=1)
                  left join GeoAttr GeoAttr_Root on
                    (Geo.id = GeoAttr_Root.geo and GeoAttr_Root.dbeg <= Cube_Dates.dt and GeoAttr_Root.dend > Cube_Dates.dt)
                  left join GeoParentIdx on
                    (GeoParentIdx.geo = Geo.id and GeoParentIdx.dbeg <= Cube_Dates.dt and GeoParentIdx.dend > Cube_Dates.dt)
                  left join GeoAttr GeoAttr_Parent on
                    (GeoParentIdx.parent = GeoAttr_Parent.geo and GeoAttr_Parent.dbeg <= Cube_Dates.dt and GeoAttr_Parent.dend > Cube_Dates.dt)
                  left join GeoData on
                    (GeoParentIdx.parent = GeoData.geo and GeoData.dbeg <= Cube_Dates.dt and GeoData.dend > Cube_Dates.dt)
                where
                  (1=1)
                order by
                  Geo.id, Cube_Dates.dt, GeoParentIdx.level
                """;

        //
        System.out.println("mdb.openQuery");

        //
        DbQuery query = dbm.getMdb().openQuery(sql, params);

        //
        int count = 0;
        while (!query.eof()) {
            count = count + 1;

            query.next();
        }

        //
        System.out.println("count: " + count);

        //
        query.close();

    }

    @Test
    public void test_jdbc_query() throws Exception {
        Map params = UtCnv.toMap(
                "dbeg", intervalDbeg,
                "dend", intervalDend
        );

        String sql = """
                with Cube_Dates as (
                  select dt
                  from generate_series(TO_DATE(?, 'YYYY-MM-DD'), TO_DATE(?, 'YYYY-MM-DD'), interval '1 day') as dt
                  order by dt
                )

                select
                  Geo.id geo,
                  Cube_Dates.dt,
                  GeoAttr_Root.name,
                  GeoAttr_Root.geoType,
                  GeoParentIdx.level,
                  GeoParentIdx.parent,
                  GeoAttr_Parent.name as parentName,
                  GeoAttr_Parent.geoType as parentGeoType,
                  GeoData.densityOil,
                  GeoData.gasFactor
                from
                  Cube_Dates
                  join Geo on (1=1)
                  left join GeoAttr GeoAttr_Root on
                    (Geo.id = GeoAttr_Root.geo and GeoAttr_Root.dbeg <= Cube_Dates.dt and GeoAttr_Root.dend > Cube_Dates.dt)
                  left join GeoParentIdx on
                    (GeoParentIdx.geo = Geo.id and GeoParentIdx.dbeg <= Cube_Dates.dt and GeoParentIdx.dend > Cube_Dates.dt)
                  left join GeoAttr GeoAttr_Parent on
                    (GeoParentIdx.parent = GeoAttr_Parent.geo and GeoAttr_Parent.dbeg <= Cube_Dates.dt and GeoAttr_Parent.dend > Cube_Dates.dt)
                  left join GeoData on
                    (GeoParentIdx.parent = GeoData.geo and GeoData.dbeg <= Cube_Dates.dt and GeoData.dend > Cube_Dates.dt)
                where
                  (1=1)
                order by
                  Geo.id, Cube_Dates.dt, GeoParentIdx.level
                """;


        //
        String url = "jdbc:postgresql://localhost:5432/ntbd_huge";
        String username = "postgres";
        String password = "111";

        //
        Connection con = DriverManager.getConnection(url, username, password);
        PreparedStatement statement = con.prepareStatement(sql);
        statement.setString(1, intervalDbegStr);
        statement.setString(2, intervalDendStr);

        //
        System.out.println("Statement.executeQuery");

        //
        ResultSet rs = statement.executeQuery();

        //
        int count = 0;
        while (!rs.next()) {
            count = count + 1;
        }

        //
        System.out.println("count: " + count);


        //
        statement.close();
        con.close();
    }

}
