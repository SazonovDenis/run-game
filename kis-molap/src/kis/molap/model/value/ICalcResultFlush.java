package kis.molap.model.value;

/**
 * Возможность открыть и закрыть выходной поток для приема результата расчета.
 */
public interface ICalcResultFlush extends ICalcResultStream {

    void open() throws Exception;

    void close() throws Exception;

    int size();

}
