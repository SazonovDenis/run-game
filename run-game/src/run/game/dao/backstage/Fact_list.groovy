package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*

class Fact_list extends BaseMdbUtils {

    public Store loadItemFactsByDataType(long idItem, long dataType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlItemFactByDataType(), [id: idItem, dataType: dataType])
        return st
    }

    public Store loadFactsByDataTypeWithTags(long dataType, Collection tagTypes) {
        Store st = mdb.createStore("Fact.list")
        Store stLoaded = mdb.loadQuery(sqlFactByDataType(tagTypes), [dataType: dataType])
        fillTag(stLoaded, st)
        return st
    }

    public Store loadFactsByDataTypeByTags(long dataType, Collection tags) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlFactByDataTypeByTags(tags), [dataType: dataType])
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
        Collection tagTypes = Arrays.asList(RgmDbConst.TagType_word_lang, RgmDbConst.TagType_word_translate_direction)
        Store stLoaded = mdb.loadQuery(sqlFactsByValueDataTypeLike(tagTypes), [value: "%" + value + "%", dataType: dataType])
        fillTag(stLoaded, st)
        return st
    }

    public Store findFactsByValueDataTypeByTags(String value, long dataType, Collection tags) {
        if (tags == null || tags.size() == 0) {
            return findFactsByValueDataType(value, dataType)
        }

        Store st = mdb.createStore("Fact.list")
        Store stLoaded = mdb.loadQuery(sqlFactsByValueDataTypeLikeByTags(tags), [value: "%" + value + "%", dataType: dataType])
        fillTag(stLoaded, st)

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

    StoreRecord loadFactWithTags(long idFact, Collection tagTypes) {
        StoreRecord rec = mdb.createStoreRecord("Fact.list")
        StoreRecord recLoaded = mdb.loadQueryRecord(sqlFactWithTags(tagTypes), [id: idFact])
        //
        rec.setValues(recLoaded)
        Long tagType = recLoaded.getLong("tagType")
        Long tagValue = recLoaded.getLong("tagValue")
        rec.setValue("tag", [(tagType): tagValue])
        //
        return rec
    }

    StoreRecord loadItem(long idItem) {
        StoreRecord rec = mdb.createStoreRecord("Item")
        mdb.loadQueryRecord(rec, sqlItem(), [id: idItem])
        return rec
    }

    void fillTag(Store stLoaded, Store st) {
        for (StoreRecord recLoaded : stLoaded) {
            StoreRecord recNew = st.add(recLoaded.getValues())
            Long tagType = recLoaded.getLong("tagType")
            Long tagValue = recLoaded.getLong("tagValue")
            recNew.setValue("tag", [(tagType): tagValue])
        }
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

    String sqlFactWithTags(Collection tagTypes) {
        String tagTypesStr = UtString.join(tagTypes, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.dataType factDataType,
    Fact.value factValue,
    
    Tag.tagType, 
    FactTag.tag tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    left join FactTag on (
        FactTag.fact = Fact.id
    )
    left join Tag on (
        FactTag.tag = Tag.id and
        Tag.tagType in (${tagTypesStr})
    )

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

    String sqlFactByDataType(Collection tagTypes) {
        String tagTypesStr = UtString.join(tagTypes, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.dataType factDataType,
    Fact.value factValue,
    
    Tag.tagType, 
    FactTag.tag tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    left join FactTag on (
        FactTag.fact = Fact.id
    )
    left join Tag on (
        FactTag.tag = Tag.id and
        Tag.tagType in (${tagTypesStr})
    )

where
    Fact.dataType = :dataType

order by
    Fact.id
"""
    }

    String sqlFactByDataTypeByTags(Collection tags) {
        String tagsStr = UtString.join(tags, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.dataType factDataType,
    Fact.value factValue,
    
    FactTag.tag

from
    Item
    join Fact on (Fact.item = Item.id)
    join FactTag on (
        FactTag.fact = Fact.id and
        FactTag.tag in (${tagsStr})
    )

order by
    Item.id,
    Fact.dataType,
    Fact.id,
    FactTag.tag
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

    String sqlFactsByValueDataTypeLike(Collection tagTypes) {
        String tagTypesStr = UtString.join(tagTypes, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.dataType factDataType,
    Fact.value factValue,
        
    Tag.tagType, 
    FactTag.tag tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    left join FactTag on (
        FactTag.fact = Fact.id
    )
    left join Tag on (
        FactTag.tag = Tag.id and
        Tag.tagType in (${tagTypesStr})
    )

where
    Fact.value like :value and
    Fact.dataType = :dataType

order by
    Fact.id
"""
    }

    String sqlFactsByValueDataTypeLikeByTags(Collection tags) {
        String tagsStr = UtString.join(tags, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.dataType factDataType,
    Fact.value factValue,
                   
    Tag.tagType, 
    FactTag.tag tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    join FactTag on (
        FactTag.fact = Fact.id and
        FactTag.tag in (${tagsStr})
    )
    join Tag on (
        FactTag.tag = Tag.id
    )

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
