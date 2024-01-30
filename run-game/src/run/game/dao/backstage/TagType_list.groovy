package run.game.dao.backstage

import jandcode.core.dao.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*

class TagType_list extends BaseMdbUtils {

    @DaoMethod
    public StoreRecord loadTagByValue(String tagValue, long tagType) {
        StoreRecord recTag = mdb.loadQueryRecord("select * from Tag where tagType = :tagType and value = :value", [tagType: tagType, value: tagValue])
        return recTag
    }

}
