package run.game.dao.pojo.fact;

/**
 * Тег (тип и значение).
 * В базе разворачивается в два поля.
 * <p>
 * НЕ самостоятельная таблица БД.
 */
public class Tag {

    /**
     * Тип тэга
     */
    TagType type;

    /**
     * Значение тэга
     */
    String value;

}
