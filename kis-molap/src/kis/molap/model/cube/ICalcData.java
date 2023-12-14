package kis.molap.model.cube;


import jandcode.commons.datetime.*;
import kis.molap.model.coord.*;
import kis.molap.model.value.*;

/**
 * Расчет значений куба.
 * <p>
 * Обычный способ расчета - на одну координату появляется одна и только одна запись.
 * <p>
 * При записи значений по координате MoWorker НЕ удаляет значения по этой координате, а только обновляет (или добавляет).
 */
public interface ICalcData {


    /**
     * Расчитывает значения куба для точки coords, для интервала дат от intervalDbeg до intervalDend (включительно).
     * Основной реализатор бизнес-логики.
     * Работает как для отдельных точек в координатном пространстве, так и по всему пространству.
     * <p>
     * Каждой дате на интервале от intervalDbeg до intervalDend должно быть создано значение, хотя бы пустое,
     * иначе СТАРЫЕ значения куба не будут удалены/очищены.
     *
     * @param coords       точки в координатном пространстве для расчета.
     *                     Может быть null, тогда расчет должен вестись по всем координатам.
     *                     Если в coords будет передана дата - она должна игнорироваться,
     *                     а браться по intervalDbeg..intervalDend.
     * @param intervalDbeg первая дата интервала
     * @param intervalDend последняя дата интервала
     * @param res          список результатов.
     *                     Сюда помещаем результат вычисления, значение куба по всем точками coords,
     *                     и в интервале от intervalDbeg до intervalDend.
     *                     Для заполнения res значениями использовать метод res.addValue(...)
     */
    void calc(Iterable<Coord> coords, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res) throws Exception;


}
