package run.game.dao.backstage

import jandcode.core.dbm.mdb.*
import jandcode.core.store.*

class Fact_list extends BaseMdbUtils {

    public Store loadFactsByDataType(long idItem, long dataType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlItemFactByDataType(), [id: idItem, dataType: dataType])
        return st
    }

    public Store loadFactsByDataType(long dataType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlFactByDataType(), [dataType: dataType])
        return st
    }

    public Store loadFactsByValueDataType(String value, long dataType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlFactsByValueDataType(), [value: value, dataType: dataType])
        return st
    }

    public Store loadFactsByValueDataType(long item, String value, long dataType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlItemFactsByValueDataType(), [item: item, value: value, dataType: dataType])
        return st
    }

    public Store findFactsByValueDataType(String value, long dataType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlFactsByValueDataTypeLike(), [value: "%" + value + "%", dataType: dataType])
        return st
    }

    public Store loadFactsByTagValue(String tagValue, long tagType) {
        TagType_list tagTypeList = mdb.create(TagType_list)
        StoreRecord recTag = tagTypeList.loadTagByValue(tagValue, tagType)
        Store st = mdb.loadQuery(sqlFactsByTagValue(), [tag: recTag.getLong("id")])
        return st
    }

    public Store loadFactsByTagValue(long idItem, String tagValue, String tagType) {
        Store st = mdb.loadQuery(sqlFactByTagValue(), [id: idItem, tagType: tagType, tagValue: tagValue])
        return st
    }

    StoreRecord loadFact(long idFact) {
        StoreRecord rec = mdb.createStoreRecord("Fact.list")
        mdb.loadQueryRecord(rec, sqlFact(), [id: idFact])
        return rec
    }

    StoreRecord loadItem(long idItem) {
        StoreRecord rec = mdb.createStoreRecord("Item")
        mdb.loadQueryRecord(rec, sqlItem(), [id: idItem])
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

    String sqlItem() {
        return """
select
    Item.*

from
    Item

where
    Item.id = :id
"""
    }

    String sqlItemFactByDataType() {
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
    Fact.dataType = :dataType

order by
    Fact.id
"""
    }

    String sqlFactsByValueDataType() {
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
    Fact.value = :value and
    Fact.dataType = :dataType

order by
    Fact.id
"""
    }

    String sqlItemFactsByValueDataType() {
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
    Fact.item = :item and
    Fact.value = :value and
    Fact.dataType = :dataType

order by
    Fact.id
"""
    }

    String sqlFactsByValueDataTypeLike() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.dataType factDataType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.value like :value and
    Fact.dataType = :dataType

order by
    Fact.id
"""
    }

    String sqlFactsByTagValue() {
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

where
    FactTag.tag = :tag

order by
    Item.id,
    Fact.dataType,
    Fact.id,
    FactTag.tag
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


}
