package run.game.testdata

import jandcode.core.apx.test.*
import jandcode.core.dbm.fixture.*
import org.junit.jupiter.api.*
import run.game.testdata.fixture.*

class TestData_Test extends Apx_Test {

    @Test
    public void fill_Tag() throws Exception {
        Fixture fx = new Tag_fb().build(model)
        utils.outTableList(fx.stores, 10)
    }

}
