package run.game.testdata

import jandcode.core.apx.test.*
import org.junit.jupiter.api.*
import run.game.testdata.fixture.*

/**
 * Формирует данные для таблицы WordDistance.
 * Запустить, когда меняется состав слов.
 * Запускается отдельно, а не в составе db-create, т.к. работает неприлично долго.
 */
class Fill_WordDistance_Test extends Apx_Test {

    @Test
    @Disabled
    public void fill_WordDistance_toCsvFile() throws Exception {
        utils.logOn()
        UtWordDistance ut = mdb.create(UtWordDistance)
        ut.MACH_COUNT_FOR_WORD = 25
        ut.toCsvFile(new File("temp/WordDistance.csv"))
    }

}
