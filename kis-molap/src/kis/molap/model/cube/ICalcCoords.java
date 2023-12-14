package kis.molap.model.cube;


import kis.molap.model.coord.*;


/**
 * Работа с координатами куба.
 * <p>
 * Формирование своих "грязных" координат куба и преобразование чужих "грязных" координат в свои.
 */
public interface ICalcCoords {


    /**
     * Выдает набор точек, которые должны быть пересчитаны (загрязненные координаты),
     * из-за изменений, сделанных в исходных таблицах за период с auditDbeg по auditDend.
     * <p>
     * Формат набора координат - аналогично {@link Space#getCoordsForInterval}
     *
     * @param auditAgeFrom возраст начала аудита (включительно)
     * @param auditAgeTo   возраст окончания аудита (включительно)
     * @return Набор точек в координатном пространстве.
     */
    CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception;

    /**
     * Превращает набор точек (например: загрязненных координат) от влияющего куба sourceCubeName
     * в свой набор точек (свои загрязненные координаты).
     * При этом возможно как увеличение количества точек (например: org_id -> org_id всех родителей),
     * так и уменьшение (well_id -> org_id).
     * <p>
     * Используется при инкрементальном расчете: влияющие кубы поставляют
     * свои загрязненные координаты для учета при расчете текущего куба.
     *
     * @param sourceCubeName влияющий куб
     * @param coords         координаты влияющего куба
     * @param coordsRes      коордитаны в своей системе координат
     */
    void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception;


}
