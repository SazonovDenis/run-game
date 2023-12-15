package run.game

import jandcode.core.apx.test.*
import jandcode.core.dbm.sql.*
import org.junit.jupiter.api.*

class DdlPrint_Test extends Apx_Test {

    @Test
    void print_struct_idx() {
        def conf = model.conf.getConf("ddl/struct-idx")
        SqlText sql = mdb.createSqlText(conf)
        println(sql)
    }

    @Test
    void print_cube_audit() {
        def conf = model.conf.getConf("ddl-script/cube-audit")
        SqlText sql = mdb.createSqlText(conf)
        println(sql)
    }

}
