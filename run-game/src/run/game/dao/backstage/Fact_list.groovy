package run.game.dao.backstage


import jandcode.core.dbm.mdb.*
import jandcode.core.store.*

class Fact_list extends BaseMdbUtils {

    public Store loadFactsByDataType(long idItem, long dataType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlFactByDataType(), [id: idItem, dataType: dataType])
        return st
    }

    public Store loadFactsByDataType(long idItem, String dataTypeCode) {
        long dataType = getIdByCode("DataType", dataTypeCode)
        return loadFactsByDataType(idItem, dataType)
    }

    public Store loadFactsByTagValue(long idItem, String tagType, String tagValue) {
        Store st = mdb.loadQuery(sqlFactByTagValue(), [id: idItem, tagType: tagType, tagValue: tagValue])
        return st
    }

    StoreRecord loadFact(long idFact) {
        StoreRecord rec = mdb.createStoreRecord("Fact.list")
        mdb.loadQueryRecord(rec, sqlFact(), [id: idFact])
        return rec
    }

    String sqlFact() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.dataType factDataType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.id = :id

order by
    Fact.id
"""
    }

    String sqlFactByDataType() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.dataType factDataType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Item.id = :id and
    Fact.dataType = :dataType

order by
    Fact.id
"""
    }

    String sqlFactByTagValue() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.dataType factDataType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

    join FactTag on (FactTag.fact = Fact.id)
    left join Tag FactTag_Tag on (FactTag.tag = FactTag_Tag.id)
    left join TagType FactTag_TagType on (FactTag_Tag.tagType = FactTag_TagType.id)

where
    Item.id = :id and
    FactTag.code = :dataType

order by
    Item.id,
    Fact.dataType,
    Fact.id,
    FactTag.tag
"""
    }

    long getIdByCode(String tableName, String code) {
        return mdb.loadQueryRecord("select id from " + tableName + " where code = :code", [code: code]).getLong("id")
    }

}
