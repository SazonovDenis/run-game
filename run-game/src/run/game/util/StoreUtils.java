package run.game.util;

import jandcode.commons.*;
import jandcode.core.store.*;

import java.util.*;
import java.util.function.*;

/**
 * Утилиты для работы с данными в Store
 */
public class StoreUtils {

    /**
     * Объединить два Store
     *
     * @param dest      куда объеденинять
     * @param sour      откуда
     * @param keyField  по какому индексному полю
     * @param fieldsMap соотвестве полей [откуда:куда]
     */
    public static void join(Store dest, Store sour, String keyField, Map<String, String> fieldsMap, boolean doAddByKeyField) {
        StoreIndex destIdx = dest.getIndex(keyField);

        for (StoreRecord recSour : sour) {
            // Ищем целевую запись
            StoreRecord destRec = destIdx.get(recSour.getValue(keyField), false);
            if (destRec == null) {
                if (doAddByKeyField) {
                    destRec = dest.add();
                } else {
                    continue;
                }
            }

            // Копируем значения
            if (fieldsMap == null) {
                // Все значения
                destRec.setValues(recSour.getValues());
            } else {
                // Значения по списку
                for (String fieldSour : fieldsMap.keySet()) {
                    String fieldDest = fieldsMap.get(fieldSour);
                    destRec.setValue(fieldDest, recSour.getValue(fieldSour));
                }
            }
        }
    }

    /**
     * Объединить два Store
     *
     * @param dest     куда объеденинять
     * @param sour     откуда
     * @param keyField по какому индексному полю
     * @param fields   соотвестве полей [куда:откуда]
     */
    public static void join(Store dest, Store sour, String keyField, Collection<String> fields) {
        Map<String, String> fieldsMap = null;

        if (fields != null) {
            // Значения по списку
            fieldsMap = new HashMap<>();
            for (String fieldKey : fields) {

                fieldsMap.put(fieldKey, fieldKey);
            }
        }

        //
        join(dest, sour, keyField, fieldsMap, false);
    }

    /**
     * Объединить данные
     *
     * @param destIdx  куда объеденинять
     * @param sour     откуда
     * @param keyField по какому индексному полю
     * @param fields   соотвестве полей [куда:откуда]
     */
    public static void joinIndex(StoreIndex destIdx, Store sour, String keyField, Map<String, String> fields) {
        for (StoreRecord r : sour.getRecords()) {
            StoreRecord destRec = destIdx.get(r.getValue(keyField), false);
            if (destRec == null) {
                continue;
            }
            for (String fieldTo : fields.keySet()) {
                destRec.setValue(fieldTo, r.getValue(fields.get(fieldTo)));
            }
        }
    }

    public static double getSum(Store store, String field) {
        double res = 0;

        for (StoreRecord rec : store) {
            res = res + rec.getDouble(field);
        }

        return res;
    }

    /**
     * Обновляет поле fieldName в store по значениям из source
     *
     * @param store      что обновляем
     * @param keyField   ключ для поиска в source
     * @param valueField какое поле обновляем
     */
    public static void updateStore(Store store, String keyField, String valueField) {
        String dictName = store.getField(keyField).getDict();
        for (StoreRecord rec : store) {
            String resolve = resolveArray(rec.getString(valueField), dictName, rec.getStore().getDictResolver());
            rec.setValue(valueField, resolve);
        }
    }

    public static String resolveArray(String value, String dictName, IStoreDictResolver resolver) {
        if (resolver == null) {
            return "";
        }

        List vals = new ArrayList<>();

        String[] ss = value.split(",");
        for (String ssValue : ss) {
            Object obj = resolver.getDictValue(dictName, UtCnv.toLong(ssValue), "text");
            vals.add(obj);
        }
        return UtString.join(vals, ", ");
    }

    /**
     * Объеденяет несколько полей в одно поле
     * // |----|----|----|     |--------------|
     * // |Мама|Папа|  Я |  -> |Мама, Папа, Я |
     * // |----|----|----|     |--------------|
     *
     * @param store       что обновляем
     * @param resultField имя поля в которое записываем итоговое значение
     * @param fields      имена полей с которых берем значения и конкатенируем их в resultField как одно значение
     */
    public static void concatenateFields(Store store, String resultField, String... fields) {
        StoreField storeField;
        StoreField storeResultField = store.getField(resultField);
        List<StoreRecord> storeRecords = store.getRecords();

        for (StoreRecord storeRec : storeRecords) {
            String[] mainArray = null;
            int mainArrayLength = 0;
            for (int j = 0; j < fields.length; j++) {
                String[] secondArray;
                storeField = store.getField(fields[j]);

                String obj = (String) storeRec.getValue(storeField.getIndex());
                if (mainArray == null) {
                    mainArray = obj.split(", ");
                    mainArrayLength = mainArray.length;
                } else {
                    if (mainArrayLength == 0) {
                        break;
                    }

                    secondArray = obj.split(", ");
                    int secondArrayLength = secondArray.length - 1;

                    for (int i = 0; i < mainArray.length; i++) {
                        if (secondArrayLength == 0 || secondArrayLength < i) {
                            break;
                        }
                        mainArray[i] = mainArray[i] + ", " + secondArray[i];
                    }

                    if (j == fields.length - 1) {
                        storeRec.setValue(storeResultField.getIndex(), String.join("\n", mainArray));
                    }
                }
            }
        }
    }

    /**
     * Собирает список значений поля valueField.
     *
     * @param store      источник значений
     * @param valueField какое поле собираем
     * @return список со значениями
     */
    public static List<Object> collect_values(Store store, String valueField) {
        List<Object> res = new ArrayList<>();

        for (StoreRecord rec : store) {
            res.add(rec.getValue(valueField));
        }

        return res;
    }

    public static List<Object> collect_values(Store store, String fieldName, String dictField) {
        List<Object> res = new ArrayList<>();

        for (StoreRecord rec : store) {
            res.add(rec.getDictValue(fieldName, dictField));
        }

        return res;
    }

    /**
     * Для каждого значения ключевого поля keyField собирает список значений поля valueField.
     *
     * @param store      источник значений
     * @param keyField   ключевое поле для группировки
     * @param valueField какое поле собираем
     * @return Map<значение ключевого поля, список со значениями>
     */
    public static Map<Long, List<Object>> collectGroupBy_values(Store store, String keyField, String valueField) {
        Map<Long, List<Object>> res = new HashMap<>();

        boolean valueFieldIsDict = store.getField(valueField).getDict() != null;

        for (StoreRecord rec : store) {
            long key = rec.getLong(keyField);

            //
            List<Object> listValue = res.get(key);

            //
            if (listValue == null) {
                listValue = new ArrayList<>();
                res.put(key, listValue);
            }

            //
            if (valueFieldIsDict) {
                listValue.add(rec.getDictValue(valueField));
            } else {
                listValue.add(rec.getValue(valueField));
            }
        }

        return res;
    }

    /**
     * Для каждого значения ключевого поля keyField собирает из store список записей.
     *
     * @param store    источник значений
     * @param keyField ключевое поле для группировки
     * @return Map<значение ключевого поля, список с собранными записями>
     */
    public static Map<Object, List<StoreRecord>> collectGroupBy_records(Store store, String keyField) {
        Map<Object, List<StoreRecord>> res = new HashMap<>();

        for (StoreRecord rec : store) {
            Object keyValue = rec.getValue(keyField);

            //
            List<StoreRecord> listRecords = res.get(keyValue);

            //
            if (listRecords == null) {
                listRecords = new ArrayList<>();
                res.put(keyValue, listRecords);
            }

            //
            listRecords.add(rec);
        }

        return res;
    }

    /**
     * Для каждого значения ключевого поля keyField собирает из store список значений
     * с возможностью кастомной конвертации.
     *
     * @param store    источник значений
     * @param keyField ключевое поле для группировки
     * @param conv     метод для конвертации записи в собираемое значение
     * @return Map<значение ключевого поля, список с собранными значениями>
     */
    public static Map<Object, List<Object>> collectGroupBy_records(Store store, String keyField, Function<StoreRecord, Object> conv) {
        Map<Object, List<Object>> res = new HashMap<>();

        // Распределим по ключу (для каждого keyField)
        Map<Object, List<StoreRecord>> stRecsByKey = StoreUtils.collectGroupBy_records(store, keyField);

        // Раскроем значения и превратим в Map
        for (Object key : stRecsByKey.keySet()) {
            List<Object> repairWorks = new ArrayList<>();

            //
            List<StoreRecord> repairWorkRecs = stRecsByKey.get(key);
            for (StoreRecord recRepairWork : repairWorkRecs) {
                Object val = conv.apply(recRepairWork);
                repairWorks.add(val);
            }

            //
            res.put(key, repairWorks);
        }

        //
        return res;
    }

    /**
     * Поиск записи по значениям
     *
     * @param st       где ищем
     * @param params   значения
     * @param startPos наичная с какой позиции
     * @return индекс записи первого (начиная со startPos) вхождения значени; -1 если не найдено.
     */
    public static int locate(Store st, Map<String, Object> params, int startPos) {
        int res = -1;

        for (int pos = startPos; pos < st.size(); pos++) {
            boolean allEq = true;
            for (String name : params.keySet()) {
                if (!eq(st.get(pos).getValueNullable(name), params.get(name))) {
                    allEq = false;
                    break;
                }
            }
            if (allEq) {
                return pos;
            }
        }

        return res;
    }

    static boolean eq(Object val1, Object val2) {
        if (val1 == null) {
            return val2 == null;
        } else {
            return val1.equals(val2);
        }
    }


}
