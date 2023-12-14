package kis.molap.model.value;

/**
 * Выходной поток для приема результата расчета куба.
 */
public interface ICalcResultStream {

    void addValue(CalcResult value) throws Exception;

}
