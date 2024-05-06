package run.game.testdata

import jandcode.core.store.Store
import jandcode.core.store.StoreRecord

class TestdataUtils {

    public static void numerateId(Store store) {
        String fieldName = "id"

        long val = 0
        for (StoreRecord rec : store) {
            val = val + 1
            rec.setValue(fieldName, val)
        }
    }


}
