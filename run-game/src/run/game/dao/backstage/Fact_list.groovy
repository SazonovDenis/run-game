package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.util.*

class Fact_list extends BaseMdbUtils {

    public Store loadBy_item_factType(long idItem, long factType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlItemFactByFactType(), [id: idItem, factType: factType])
        return st
    }

    public Store loadBy_item_value_factType(long item, String value, long factType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlBy_item_value_factType__equal(), [item: item, value: value, factType: factType])
        return st
    }

    public Store loadBy_factType(long factType, Collection<Long> tagTypes) {
        Store st = mdb.createStore("Fact.list")

        // Факты (большим общим списком)
        mdb.loadQuery(st, sqlBy_factType(), [factType: factType])

        // Тэги фактов (большим общим списком)
        Store stTags = mdb.loadQuery(sqlBy_factType__tags(tagTypes), [factType: factType])

        // Распределим тэги
        spreadTags(stTags, st)

        //
        return st
    }

    public Store loadBy_value_factType(String value, long factType) {
        Store st = mdb.createStore("Fact.list")

        mdb.loadQuery(st, sqlBy_value_factType(), [value: value, factType: factType])

        return st
    }

    /**
     * Ищем факты по типу и значению.
     * <br>
     * Результат имеет поле tag, куда подгружены значения тэгов word-lang и word-translate-direction.
     *
     * @param factValue
     * @param factType
     * @return
     */
    public Store findBy_factType_value(long factType, String factValue) {
        Store st = mdb.createStore("Fact.list")

        // Факты
        mdb.loadQuery(st, sqlBy_value_factType__like(), [value: "%" + factValue + "%", factType: factType])

        //
        return st
    }

    /**
     * Ищем факты по типу и значению, с дополнительной фильтрацией по тэгам.
     * Каждый тэг фильтруется по условию или, т.е. достаточно найти одно совпадение по тэгу,
     * чтобы запись попала в результат.
     * <br>
     * Результат имеет поле tag, куда подгружены значения искомых тэгов.
     *
     * @param factType
     * @param factValue
     * @param tags
     * @return
     */
    public Store findBy_factType_value(long factType, String factValue, Collection<Long> tagTypes) {
        Store st = mdb.createStore("Fact.list")

        //
        if (tagTypes == null || tagTypes.size() == 0) {
            return findBy_factType_value(factType, factValue)
        }

        // Факты
        Map params = [factType: factType, value: "%" + factValue + "%"]
        mdb.loadQuery(st, sqlBy_value_factType__like(), params)

        // Тэги фактов (для загруженных id)
        Store stTags = loadTagsByIds(st.getUniqueValues("id"), tagTypes)

        // Распределим тэги
        spreadTags(stTags, st)

        //
        return st
    }


    Store loadTagsByIds(Collection ids, Collection<Long> tagTypes) {
        Store stTag = mdb.loadQuery(sqlTag_ByTagTypes(ids, tagTypes, "FactTag", "fact"))
        return stTag
    }

    void spreadTags(Store stTag, Store stDest) {
        Map<Object, List<StoreRecord>> mapTagLoaded = StoreUtils.collectGroupBy_records(stTag, "fact")

        // Заполним поле tag
        for (StoreRecord recDest : stDest) {
            Map<Long, String> mapTag = new HashMap<>()

            // Найдем в загруженном список StoreRecord со значениями
            List<StoreRecord> lstTagRecs = mapTagLoaded.get(recDest.getLong("id"))

            // Превратим список StoreRecord со значениями тэгов в Map тегов
            if (lstTagRecs != null) {
                for (StoreRecord recTag : lstTagRecs) {
                    mapTag.put(recTag.getLong("tagType"), recTag.getString("tagValue"))
                }
            }

            // Запишем Map тегов в получателя
            if (mapTag.size() != 0) {
                recDest.setValue("tag", mapTag)
            }
        }
    }


    protected String sqlTag(Collection ids, String tagTableName, String tagRefFieldName) {
        String strIds
        if (ids.size() == 0) {
            strIds = "0"
        } else {
            strIds = ids.join(",")
        }
        return """
select
    ${tagTableName}.*
from
    ${tagTableName}
where
    ${tagTableName}.${tagRefFieldName} in ( ${strIds} )
"""
    }


    protected String sqlTag_ByTagTypes(Collection ids, Collection tagTypes, String tagTableName, String tagRefFieldName) {
        String strIds
        if (ids.size() == 0) {
            strIds = "0"
        } else {
            strIds = ids.join(",")
        }
        String strTagTypes
        if (tagTypes.size() == 0) {
            strTagTypes = "0"
        } else {
            strTagTypes = tagTypes.join(",")
        }
        return """
select
    ${tagTableName}.*
from
    ${tagTableName}
where
    ${tagTableName}.tagType in (${strTagTypes}) and
    ${tagTableName}.${tagRefFieldName} in (${strIds})
"""
    }


/*
    public Store loadFactsByTagValue(String tagValue, long tagType) {
        TagType_list tagTypeList = mdb.create(TagType_list)
        StoreRecord recTag = tagTypeList.loadTagByValue(tagValue, tagType)
        Store st = mdb.loadQuery(sqlFactsByTagValue(), [tag: recTag.getLong("id")])
        return st
    }
*/

/*
    public Store loadFactsByTagValue(long idItem, String tagValue, long tagType) {
        Store st = mdb.loadQuery(sqlFactByTagValue(), [id: idItem, tagType: tagType, tagValue: tagValue])
        return st
    }
*/

    StoreRecord loadFact(long idFact) {
        StoreRecord rec = mdb.createStoreRecord("Fact.list")
        mdb.loadQueryRecord(rec, sqlFact(), [id: idFact])
        return rec
    }

    StoreRecord loadFactWithTags(long idFact, Collection tagTypes) {
        StoreRecord rec = mdb.createStoreRecord("Fact.list")
        StoreRecord recLoaded = mdb.loadQueryRecord(sql__withTags(tagTypes), [id: idFact])
        //
        rec.setValues(recLoaded)
        Long tagType = recLoaded.getLong("tagType")
        String tagValue = recLoaded.getString("tagValue")
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

    String sql__withTags(Collection<Long> tagTypes) {
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

    String sqlBy_factType() {
        return """
select
    Fact.item,
    --Item.value itemValue,
    
    Fact.id,
    Fact.factType factType,
    Fact.value factValue

from
    Fact

where
    Fact.factType = :factType
"""
    }


    String sqlBy_factType__tags(Collection tagTypes) {
        String tagTypesStr = UtString.join(tagTypes, ",")

        return """
select
    Fact.id fact,
    
    FactTag.tagType, 
    FactTag.tagValue

from
    Fact
    join FactTag on (
        FactTag.fact = Fact.id and
        FactTag.tagType in (${tagTypesStr})
    )

where
    Fact.factType = :factType

order by
    Fact.id
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

    String sqlBy_item_value_factType__equal() {
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

    String sqlBy_value_factType__like() {
        return """
select
    Item.id item,
    Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.factType factType,
    Fact.value factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.value like :value and
    Fact.factType = :factType

order by
    Fact.id
"""
    }

    String sqlBy_value_factType__withTags(Map<Long, String> tags) {
        String tagFilterSql = tagFilterSql(tags)

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
        (${tagFilterSql})
    )

where
    Fact.value like :value and
    Fact.factType = :factType

order by
    Fact.id
"""
    }

    String tagFilterSql(Map<Long, String> tags) {
        String cond = ""
        for (Long tagTypeId : tags.keySet()) {
            if (cond.length() != 0) {
                cond = cond + " and "
            }
            cond = cond + "(FactTag.tagType = " + tagTypeId + " and FactTag.tagValue = :_" + tagTypeId + ")"
        }
        return cond
    }

    Map sqlParamsByTags(Map<Long, String> tags) {
        Map params = new HashMap()
        for (Long tagTypeId : tags.keySet()) {
            String tagType = "_" + tagTypeId
            String tagValue = tags.get(tagTypeId)
            params.put(tagType, tagValue)
        }
        return params
    }

/*
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
*/


}
