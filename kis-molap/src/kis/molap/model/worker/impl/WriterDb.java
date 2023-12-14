package kis.molap.model.worker.impl;

import jandcode.commons.*;
import jandcode.commons.collect.*;
import jandcode.core.db.*;
import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.coord.*;
import kis.molap.model.service.*;
import kis.molap.model.value.*;

import java.io.*;
import java.util.*;


/**
 * Писатель значений куба в физические таблицы.
 * Знает как координаты и значения писать в БД.
 */
public class WriterDb {


    protected Mdb mdb;
    protected CubeService cubeService;
    protected CubeInfo cubeInfo;
    protected SpaceInfo spaceInfo;

    DbQuery queryInsert;
    DbQuery queryUpdate;


    public WriterDb(Mdb mdb) {
        this.mdb = mdb;
        this.cubeService = mdb.getModel().bean(CubeService.class);
    }


    public void open(CubeInfo cubeInfo) throws Exception {
        this.cubeInfo = cubeInfo;
        this.spaceInfo = cubeService.getSpaceInfo(cubeInfo.getSpace());
        queryInsert = mdb.createQuery(sqlInsert());
        queryUpdate = mdb.createQuery(sqlUpdate());
    }


    public void close() {
        if (queryInsert != null) {
            queryInsert.close();
        }
        if (queryUpdate != null) {
            queryUpdate.close();
        }
    }


    /**
     * Физически обновляет значение value по координате coord.
     * Если запись уже есть - она обновляется, если записи не было - создается.
     *
     * @param coord координата
     * @param value значение по координате
     */
    public void updatePoint(Coord coord, ValueSingle value) throws Exception {
        Map params = new HashMapNoCase();
        params.putAll(coord.getValues());
        params.putAll(value.getValues());

        // Обновляем значения по координате
        int updatedCount = updateCubeRec(params);

        // Координата coord в кубе еще не создана, вставляем
        if (updatedCount == 0) {
            createPoint(coord, value);
        }
    }


    /**
     * Физически обновляет значения values по координате coord.
     * Если запись уже есть - она обновляется, если записи не было - создается.
     *
     * @param coord  координата
     * @param values значения по координате
     */
    public void updatePointList(Coord coord, ValueList values) throws Exception {
        // Удаляем старые значения по координате
        deletePoint(coord);

        // Добавляем значения по координате
        for (int i = 0; i < values.size(); i++) {
            ValueSingle value = values.get(i);
            createPoint(coord, value);
        }
    }


    /**
     * Физически удаляет место (удаляет строку) для значений
     * в таблице пространства по координате coord.
     *
     * @param coord Координата
     */
    public void deletePoint(Coord coord) throws Exception {
        Map params = new HashMapNoCase();
        params.putAll(coord.getValues());
        mdb.deleteRec(spaceInfo.getTable(), params);
    }


    /**
     * Физически создает место (вставляет строку) для значений
     * в таблице пространства по координате coord.
     *
     * @param coord  Координата
     * @param values Начальные значения. Опциональные, могут быть null
     * @throws Exception Если запись уже есть
     */
    private void createPoint(Coord coord, ValueSingle values) throws Exception {
        // Координаты - все
        Map params = new HashMapNoCase();
        params.putAll(coord.getValues());

        // Значения - опционально
        if (values != null) {
            params.putAll(values.getValues());
        }

        //
        insertCubeRec(params);
    }


    private void insertCubeRec(Map params) throws Exception {
        queryInsert.setParams(params);
        queryInsert.execUpdate();
    }


    private int updateCubeRec(Map params) throws Exception {
        queryUpdate.setParams(params);
        int res = queryUpdate.execUpdate();
        return res;
    }


    private String getSqlInsertByCoords(String tableName, List<String> fields, List<String> coords) {
        String sql = "";

        //
        sql = sql + "insert into " + tableName + " (\n";
        //
        boolean isFirst = true;
        for (String coordName : coords) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql = sql + ",\n";
            }
            sql = sql + "  " + coordName;
        }
        for (String fieldName : fields) {
            // Бывает, координата совпадает со значением (например, куб MO_dates) - таких повторов не надо
            if (coords.contains(fieldName)) {
                continue;
            }
            sql = sql + ",\n";
            sql = sql + "  " + fieldName;
        }
        //
        sql = sql + "\n) values (\n";
        //
        isFirst = true;
        for (String coordName : coords) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql = sql + ",\n";
            }
            sql = sql + "  " + ":" + coordName;
        }
        for (String fieldName : fields) {
            // Бывает, координата совпадает со значением (например, куб MO_dates) - таких повторов не надо
            if (coords.contains(fieldName)) {
                continue;
            }
            sql = sql + ",\n";
            sql = sql + "  " + ":" + fieldName;
        }
        //
        sql = sql + "\n)";

        //
        return sql;
    }


    private String getSqlUpdateByCoords(String tableName, List<String> fields, List<String> coords) {
        StringBuilder sb = new StringBuilder();

        //
        sb.append("update ");
        sb.append(tableName);

        //
        sb.append(" set ");
        boolean first = true;
        for (String field : fields) {
            if (!first) {
                sb.append(", ");
            }
            sb.append(field);
            sb.append(" = :");
            sb.append(field);
            first = false;
        }

        //
        sb.append(" where ");
        first = true;
        for (String field : coords) {
            if (!first) {
                sb.append(" and ");
            }
            sb.append(field);
            sb.append(" = :");
            sb.append(field);
            first = false;
        }

        //
        return sb.toString();
    }


    String sqlInsert() {
        String sql = "";

        //
        sql = sql + "insert into " + spaceInfo.getTable() + " (\n";
        //
        boolean isFirst = true;
        for (String coordName : spaceInfo.getCoords()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql = sql + ",\n";
            }
            sql = sql + "  " + coordName;
        }
        for (String fieldName : cubeInfo.getFields()) {
            sql = sql + ",\n";
            sql = sql + "  " + fieldName;
        }
        //
        sql = sql + "\n) values (\n";
        //
        isFirst = true;
        for (String coordName : spaceInfo.getCoords()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql = sql + ",\n";
            }
            sql = sql + "  " + ":" + coordName;
        }
        for (String fieldName : cubeInfo.getFields()) {
            sql = sql + ",\n";
            sql = sql + "  " + ":" + fieldName;
        }
        //
        sql = sql + "\n)";

        //
        return sql;
    }


    String sqlUpdate() {
        String sql = "";

        //
        sql = sql + "update " + spaceInfo.getTable() + " set \n";
        //
        boolean isFirst = true;
        for (String fieldName : cubeInfo.getFields()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql = sql + ",\n";
            }
            sql = sql + "  " + fieldName + " = :" + fieldName;
        }
        //
        sql = sql + "\nwhere\n";
        //
        isFirst = true;
        for (String coordName : spaceInfo.getCoords()) {
            if (isFirst) {
                isFirst = false;
            } else {
                sql = sql + " and \n";
            }
            sql = sql + "  " + coordName + " = :" + coordName;
        }
        //
        sql = sql + "";

        //
        return sql;
    }


    /**
     * Импорт содержимого csv-файла в БД командой copy
     * Записи только добавляются, если ключ уже есть - ошибка.
     * Быстрее, чем insertOrUpdateFromCsv.
     *
     * @param cubeInfo куб, чьи данные импортируем
     * @param file     csv-файл. Должен быть доступен для СУБД (например, находиться в системном temp)
     */
    public void insertFromCsv(CubeInfo cubeInfo, File file) throws Exception {
        SpaceInfo spaceInfo = cubeService.getSpaceInfo(cubeInfo.getSpace());

        //
        List<String> coords = new ArrayList<>();
        coords.addAll(spaceInfo.getCoords());
        List<String> coordsAndFields = new ArrayList<>();
        //
        coordsAndFields.addAll(spaceInfo.getCoords());
        // Бывает, координата совпадает со значением (например, куб MO_dates) - таких повторов не надо
        for (String fieldName : cubeInfo.getFields()) {
            if (spaceInfo.getCoords().contains(fieldName)) {
                continue;
            }
            //
            coordsAndFields.add(fieldName);
        }

        //
        String sqlInsert = "copy " + spaceInfo.getTable() + " (" + UtString.join(coordsAndFields, ",") + ") from '" + file.getCanonicalPath() + "'";
        mdb.execQuery(sqlInsert);
    }

    /**
     * Импорт содержимого csv-файла в БД командой copy
     * Записи добавляются или обновляются (если ключ уже есть).
     * Медленнее, чем insertFromCsv.
     *
     * @param cubeInfo куб, чьи данные импортируем
     * @param file     csv-файл. Должен быть доступен для СУБД (например, находиться в системном temp)
     */
    public void insertOrUpdateFromCsv(CubeInfo cubeInfo, File file) throws Exception {
        SpaceInfo spaceInfo = cubeService.getSpaceInfo(cubeInfo.getSpace());


        // Список координат
        List<String> coords = new ArrayList<>();
        coords.addAll(spaceInfo.getCoords());

        // Список координаты+поля
        List<String> coordsAndFields = new ArrayList<>();
        coordsAndFields.addAll(spaceInfo.getCoords());
        for (String fieldName : cubeInfo.getFields()) {
            // Бывает, координата совпадает со значением (например, куб MO_dates) - таких повторов не надо
            if (spaceInfo.getCoords().contains(fieldName)) {
                continue;
            }
            //
            coordsAndFields.add(fieldName);
        }

        // Список координаты+поля
        List<String> fields = new ArrayList<>();
        for (String fieldName : cubeInfo.getFields()) {
            // Бывает, координата совпадает со значением (например, куб MO_dates) - таких повторов не надо
            if (spaceInfo.getCoords().contains(fieldName)) {
                continue;
            }
            //
            fields.add(fieldName);
        }

        //
        String sTableName = spaceInfo.getTable();

        //
        String sCoordsAndFields = UtString.join(coordsAndFields, ", ");

        //
        List<String> setFields = new ArrayList<>();
        for (String fieldName : fields) {
            setFields.add(fieldName + " = tmp_" + sTableName + "." + fieldName);
        }
        String sSetFields = UtString.join(setFields, ", ");

        //
        List<String> whereCoords = new ArrayList<>();
        for (String coordName : coords) {
            whereCoords.add(sTableName + "." + coordName + " = tmp_" + sTableName + "." + coordName);
        }
        String sWhereCoords = UtString.join(whereCoords, " and ");

        //
        String sCoords = UtString.join(coords, ", ");

        //
        List<String> selectFull = new ArrayList<>();
        for (String fieldName : coordsAndFields) {
            selectFull.add("tmp_" + sTableName + "." + fieldName);
        }
        String sSelectFull = UtString.join(selectFull, ", ");


        //
        String sqlTmpTableCreate = "create temp table tmp_" + sTableName + " as select * from " + sTableName + " limit 0";
        String sqlTmpTableFillFromCsv = "copy tmp_" + sTableName + " (" + sCoordsAndFields + ") from '" + file.getCanonicalPath() + "'";
        String sqlTmpTableIndex = "create index tmp_idx ON tmp_" + sTableName + "(" + sCoords + ")";
        String sqlUpdate = "update " + sTableName + "\n" +
                "set " + sSetFields + "\n" +
                "from tmp_" + sTableName + "\n" +
                "where " + sWhereCoords;
        String sqlDelete = "delete from " + sTableName + "\n" +
                "using tmp_" + sTableName + "\n" +
                "where " + sWhereCoords;
        String sqlInsert = "insert into " + sTableName + " (" + sCoordsAndFields + ") (\n" +
                "  select\n" +
                "    " + sSelectFull + "\n" +
                "  from\n" +
                "    tmp_" + sTableName + "\n" +
                "    left join " + sTableName + " on ( \n" +
                "      " + sWhereCoords + "\n" +
                "    ) \n" +
                "    where " + sTableName + " is null\n" +
                ")";
        String sqlTmpTableDrop = "drop table tmp_" + sTableName;

        //
        mdb.execQuery(sqlTmpTableCreate);
        mdb.execQuery(sqlTmpTableFillFromCsv);
        mdb.execQuery(sqlTmpTableIndex);

        // Удалим те строки, которые будем вставлять (для пучковых кубов это обязательный шаг)
        if (cubeInfo.isBunch()) {
            mdb.execQuery(sqlDelete);
        }

        // Обновим те строки, которые уже есть в таблице
        if (sSetFields.length() > 0) {
            // Если у куба нет полей, а только координаты (например, как у куба Cube_Dates),
            // то делать update смысла нет, только insert
            mdb.execQuery(sqlUpdate);
        }
        // Вставим те строки, которых еще нет в таблице
        mdb.execQuery(sqlInsert);


        // Удалим временную таблицу
        mdb.execQuery(sqlTmpTableDrop);
    }


}
