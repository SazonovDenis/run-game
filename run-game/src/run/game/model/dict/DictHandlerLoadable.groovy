package run.game.model.dict


import jandcode.core.dbm.dict.*
import jandcode.core.dbm.mdb.*
import jandcode.core.store.*

/**
 * Универсальный загрузчик для словарей стандартной структуры
 */
class DictHandlerLoadable extends BaseDictHandlerLoadable {

    void loadDict(Mdb mdb, Dict dict, Store data) throws Exception {
        // имя таблицы берем из атрибута 'table', если нет такого атрибута - то из имени словаря
        String table = dict.getConf().getString("table", dict.getName())
        //
        mdb.loadQuery(data, """
            select
                *
            from
                ${table}
            where
                id<>0 and (deleted is null or deleted = 0)
            order by id
        """)
    }

}
