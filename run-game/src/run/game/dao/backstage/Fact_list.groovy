package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*
import run.game.util.*

class Fact_list extends BaseMdbUtils {

    public Store loadBy_item_factType(long idItem, long factType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlItemFactByFactType(), [id: idItem, factType: factType])
        return st
    }

    public Store loadBy_item(long idItem, Collection<Long> tagTypes) {
        Store st = mdb.createStore("Fact.list")

        mdb.loadQuery(st, sqlItemFact(), [id: idItem])

        // Тэги фактов (для загруженных id)
        Store stTags = loadTagsByIds(st.getUniqueValues("id"), tagTypes)

        // Распределим тэги
        spreadTags(stTags, st)

        //
        return st
    }

    public Store loadBy_item_value_factType(long idItem, String factValue, long factType) {
        Store st = mdb.createStore("Fact.list")
        mdb.loadQuery(st, sqlBy_item_value_factType__equal(), [item: idItem, factType: factType, factValue: factValue])
        return st
    }

    /**
     * Загружаем все факты указанного типа.
     * В результате поле tag заполняется тэгами, заказанными в tagTypes.
     *
     * @param tagTypes Типы тэгов, которвые должны попасть в поле tag
     */
    public void loadBy_factType(Store stFact, Collection<Long> factTypes, Collection<Long> tagTypes) {
        // Факты (большим общим списком)
        mdb.loadQuery(stFact, sqlBy_factType(factTypes))

        // Тэги фактов (большим общим списком)
        Store stTags = mdb.loadQuery(sqlBy_factType__tags(factTypes, tagTypes))

        // Распределим тэги
        spreadTags(stTags, stFact)
    }

    /**
     * Загружаем факты указанного типа с указанным значением.
     * В результате поле tag не заполняется.
     */
    public Store loadBy_value_factType(long factType, String factValue) {
        Store st = mdb.createStore("Fact.list")

        mdb.loadQuery(st, sqlBy_value_factType(), [factType: factType, factValue: factValue])

        return st
    }

    /**
     * Ищем факты по типу факта и значению, по частичному совпадению.
     * В результате поле tag не заполняется.
     */
    public Store findBy_factType_value(long factType, String factValue) {
        Store st = mdb.createStore("Fact.list")

        // Факты
        Map params = [factType: factType, factValue: "%" + factValue + "%"]
        mdb.loadQuery(st, sqlBy_value_factType__like(), params)

        //
        return st
    }

    /**
     * Ищем факты по типу факта и значению, по частичному совпадению.
     * В результате поле tag заполняется тэгами, заказанными в tagTypes.
     *
     * @param tagTypes Типы тэгов, которвые должны попасть в поле tag
     */
    public Store findBy_factType_value(long factType, String factValue, Collection<Long> tagTypes) {
        Store st = mdb.createStore("Fact.list")

        //
        if (tagTypes == null || tagTypes.size() == 0) {
            return findBy_factType_value(factType, factValue)
        }

        // Факты
        Map params = [factType: factType, factValue: "%" + factValue + "%"]
        mdb.loadQuery(st, sqlBy_value_factType__like(), params)

        // Тэги фактов (для загруженных id)
        Store stTags = loadTagsByIds(st.getUniqueValues("id"), tagTypes)

        // Распределим тэги
        spreadTags(stTags, st)

        //
        return st
    }

    public Store findBy_factType_value(Collection<Long> factTypes, String factValue, Collection<Long> tagTypes) {
        Store st = mdb.createStore("Fact.list")

        // Факты
        Map params = [factValue: "%" + factValue + "%"]
        mdb.loadQuery(st, sqlBy_value_factType__like(factTypes), params)

        // Тэги фактов (для загруженных id)
        Store stTags = loadTagsByIds(st.getUniqueValues("id"), tagTypes)

        // Распределим тэги
        spreadTags(stTags, st)

        //
        return st
    }

    StoreRecord loadFact(long idFact) {
        StoreRecord rec = mdb.createStoreRecord("Fact.list")
        mdb.loadQueryRecord(rec, sqlFact(), [id: idFact])
        return rec
    }

    StoreRecord loadFact(long idFact, Collection<Long> tagTypes) {
        StoreRecord rec = mdb.createStoreRecord("Fact.list")
        mdb.loadQueryRecord(rec, sqlFact(), [id: idFact])

        // Тэги фактов (для загруженных id)
        Store stTags = loadTagsByIds([rec.getLong("id")], tagTypes)

        // Распределим тэги
        spreadTags(stTags, rec)

        //
        return rec
    }

    StoreRecord loadItem(long idItem) {
        StoreRecord rec = mdb.createStoreRecord("Item")
        mdb.loadQueryRecord(rec, sqlItem(), [id: idItem])
        return rec
    }


    // region утилиты

    Store loadTagsByIds(Collection ids, Collection<Long> tagTypes) {
        Store stTag = mdb.loadQuery(sqlTag_ByTagTypes(ids, tagTypes, "FactTag", "fact"))
        return stTag
    }

    void spreadTags(Store stTag, Store stDest) {
        Map<Object, List<StoreRecord>> mapTagLoaded = StoreUtils.collectGroupBy_records(stTag, "fact")

        // Заполним поле tag
        for (StoreRecord recDest : stDest) {
            // Найдем в загруженном список StoreRecord со значениями
            List<StoreRecord> lstTagRecs = mapTagLoaded.get(recDest.getLong("id"))

            // Превратим список lstTagRecs (со значениями тэгов) в Map тегов,
            // запишем Map тегов в получателя
            fillRecordTags(lstTagRecs, recDest)
        }
    }

    void spreadTags(Store stTag, StoreRecord recDest) {
        Map<Object, List<StoreRecord>> mapTagLoaded = StoreUtils.collectGroupBy_records(stTag, "fact")

        // Найдем в загруженном список StoreRecord со значениями
        List<StoreRecord> lstTagRecs = mapTagLoaded.get(recDest.getLong("id"))

        // Превратим список lstTagRecs (со значениями тэгов) в Map тегов,
        // запишем Map тегов в получателя
        fillRecordTags(lstTagRecs, recDest)
    }

    void fillRecordTags(List<StoreRecord> lstTagRecs, StoreRecord recDest) {
        // Заполним поле tag
        Map<Long, String> mapTag = new HashMap<>()

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

    // endregion


    // region sql

    String sqlTag(Collection ids, String tagTableName, String tagRefFieldName) {
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

    String sqlTag_ByTagTypes(Collection ids, Collection tagTypes, String tagTableName, String tagRefFieldName) {
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

    String sqlFact() {
        return """
select
    Fact.item,
    
    Fact.id,
    Fact.fact fact,
    Fact.factType factType,
    Fact.factValue

from
    Fact

where
    Fact.id = :id
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
    --Item.value itemValue,
    
    Fact.id,
    Fact.fact fact,
    Fact.factType factType,
    Fact.factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Item.id = :id and
    Fact.factType = :factType
"""
    }

    String sqlItemFact() {
        return """
select
    Item.id item,
    --Item.value itemValue,
    
    Fact.id,
    Fact.fact fact,
    Fact.factType factType,
    Fact.factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Item.id = :id
"""
    }

    String sqlBy_factType(Collection<Long> factTypes) {
        String factTypesStr = UtString.join(factTypes, ",")

        return """
select
    Fact.item,
    --Item.value itemValue,
    
    Fact.id,
    Fact.fact fact,
    Fact.factType factType,
    Fact.factValue

from
    Fact

where
    Fact.factType in (${factTypesStr})
"""
    }


    String sqlBy_factType__tags(Collection<Long> factTypes, Collection tagTypes) {
        String factTypesStr = UtString.join(factTypes, ",")
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
    Fact.factType in (${factTypesStr})
"""
    }


    String sqlBy_value_factType() {
        return """
select
    Item.id item,
    --Item.value itemValue,
    
    Fact.id,
    Fact.fact fact,
    Fact.factType factType,
    Fact.factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.factValue = :factValue and
    Fact.factType = :factType
"""
    }

    String sqlBy_item_value_factType__equal() {
        return """
select
    Item.id item,
    --Item.value itemValue,
    
    Fact.id,
    Fact.fact fact,
    Fact.factType factType,
    Fact.factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.item = :item and
    Fact.factValue = :factValue and
    Fact.factType = :factType
"""
    }

    String sqlBy_value_factType__like() {
        return """
select
    Item.id item,
    --Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.factType factType,
    Fact.factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.factValue like :factValue and
    Fact.factType = :factType
"""
    }

    String sqlBy_value_factType__like(Collection factTypes) {
        String factTypesStr = UtString.join(factTypes, ",")

        return """
select
    Item.id item,
    --Item.value itemValue,
    
    Fact.id,
    Fact.id fact,
    Fact.factType factType,
    Fact.factValue

from
    Item
    join Fact on (Fact.item = Item.id)

where
    Fact.factValue like :factValue and
    Fact.factType in (${factTypesStr})
"""
    }

    // endregion

}
