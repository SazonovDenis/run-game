package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*

class Fact_list extends BaseMdbUtils {

    public Store loadItemFactsByFactType(long idItem, long factType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlItemFactByFactType(), [id: idItem, factType: factType])
        return st
    }

    public Store loadFactsByFactTypeWithTags(long factType, Collection tagTypes) {
        Store st = mdb.createStore("Fact.list")
        Store stLoaded = mdb.loadQuery(sqlFactByFactType(tagTypes), [factType: factType])
        fillTag(stLoaded, st)
        return st
    }

    public Store loadFactsByFactTypeByTags(long factType, Collection tags) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlFactByFactTypeByTags(tags), [factType: factType])
        return st
    }

    public Store loadFactsByValueFactType(String value, long factType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlBy_value_factType(), [value: value, factType: factType])
        return st
    }

    public Store loadFactsByValueFactType(long item, String value, long factType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlBy_item_value_factType(), [item: item, value: value, factType: factType])
        return st
    }

    public Store findBy_value_factType(String factValue, long factType) {
        Store st = mdb.createStore("Fact.list")
        //Collection tagTypes = Arrays.asList(RgmDbConst.TagType_word_lang, RgmDbConst.TagType_word_translate_direction)
        Collection tagTypes = Arrays.asList(RgmDbConst.TagType_word_lang, RgmDbConst.TagType_word_translate_direction)
        Store stLoaded = mdb.loadQuery(sqlBy_value_factType__Like(tagTypes), [value: "%" + factValue + "%", factType: factType])
        fillTag(stLoaded, st)
        return st
    }

    public Store findBy_value_factType_tags(long factType, String factValue, Map<Long, String> tags) {
        if (tags == null || tags.size() == 0) {
            return findBy_value_factType(factValue, factType)
        }

        Map params = [factType: factType, value: "%" + factValue + "%"]
        params.putAll(sqlParams(tags))
        Store stLoaded = mdb.loadQuery(sqlBy_value_factType_tags(tags), params)

        Store st = mdb.createStore("Fact.list")
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
            String tagValue = recLoaded.getString("tagValue")
            recNew.setValue("tag", [(tagType): tagValue])
        }
    }

    String sqlFact() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.factType factType,
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
    Fact.factType factType,
    Fact.value factValue,
    
    FactTag.tagType, 
    FactTag.tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    left join FactTag on (
        FactTag.fact = Fact.id and
        FactTag.tagType in (${tagTypesStr})
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

    String sqlItemFactByFactType() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.factType factType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Item.id = :id and
    Fact.factType = :factType

order by
    Fact.id
"""
    }

    String sqlFactByFactType(Collection tagTypes) {
        String tagTypesStr = UtString.join(tagTypes, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.factType factType,
    Fact.value factValue,
    
    FactTag.tagType, 
    FactTag.tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    left join FactTag on (
        FactTag.fact = Fact.id and
        FactTag.tagType in (${tagTypesStr})
    )

where
    Fact.factType = :factType

order by
    Fact.id
"""
    }

    String sqlFactByFactTypeByTags(Collection tags) {
        String tagsStr = UtString.join(tags, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.factType factType,
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
    Fact.factType,
    Fact.id,
    FactTag.tag
"""
    }

    String sqlBy_value_factType() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.factType factType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.value = :value and
    Fact.factType = :factType

order by
    Fact.id
"""
    }

    String sqlBy_item_value_factType() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.factType factType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.item = :item and
    Fact.value = :value and
    Fact.factType = :factType

order by
    Fact.id
"""
    }

    String sqlBy_value_factType__Like(Collection tagTypes) {
        String tagTypesStr = UtString.join(tagTypes, ",")

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.factType factType,
    Fact.value factValue,
        
    FactTag.tagType, 
    FactTag.tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    left join FactTag on (
        FactTag.fact = Fact.id and
        FactTag.tagType in (${tagTypesStr})
    )

where
    Fact.value like :value and
    Fact.factType = :factType

order by
    Fact.id
"""
    }

    String sqlCond(Map<Long, String> tags) {
        String cond = ""
        for (Long tagType : tags.keySet()) {
            if (cond.length() != 0) {
                cond = cond + " or "
            }
            String tagValue = tags.get(tagType)
            cond = cond + "(FactTag.tagType = " + tagType + " and FactTag.tagValue = :_" + tagType + ")"
        }
        return cond
    }

    Map sqlParams(Map<Long, String> tags) {
        Map params = new HashMap()
        for (Long tagType : tags.keySet()) {
            String tagTypeStr = "_" + tagType
            String tagValue = tags.get(tagType)
            params.put(tagTypeStr, tagValue)
        }
        return params
    }

    String sqlBy_value_factType_tags(Map<Long, String> tags) {
        String tagValuesStr = sqlCond(tags)

        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.factType factType,
    Fact.value factValue,
                   
    FactTag.tagType, 
    FactTag.tagValue

from
    Item
    join Fact on (Fact.item = Item.id)
    join FactTag on (
        FactTag.fact = Fact.id and
        (${tagValuesStr})
    )

where
    Fact.value like :value and
    Fact.factType = :factType

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
    Fact.factType factType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)
    join FactTag on (FactTag.fact = Fact.id)

where
    FactTag.tag = :tag

order by
    Item.id,
    Fact.factType,
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
    Fact.factType factType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)
    join FactTag on (FactTag.fact = Fact.id)

where
    Item.id = :id and
    FactTag.code = :factType

order by
    Item.id,
    Fact.factType,
    Fact.id,
    FactTag.tag
"""
    }


}
