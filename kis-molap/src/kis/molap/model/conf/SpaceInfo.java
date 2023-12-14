package kis.molap.model.conf;


import java.util.*;

/**
 * Следит за координатами, расчитывает пространство
 */
public interface SpaceInfo {

    /**
     * Имя
     */
    String getName();

    /**
     *
     */
    String getClassName();

    /**
     * Заголовок (рус.)
     */
    String getTitle();

    /**
     * Имя целевой таблицы в БД
     */
    String getTable();

    /**
     * @return Координаты куба
     */
    List<String> getCoords();

    /**
     * @return Список влияющих таблиц.
     * todo: Метод не реализован. Сейчас используется исключительно для тестов.
     * Вообще - нужен, например Space_WellDt тогда сможет корректно обрабатывать ПОЯВЛЕНИЕ новых скважин.
     * В таком случае можно реализовать как появление, так и исчезновение координат (и, соответственно, очистку таблиц кубов)
     */
    List<String> getDepends();

}
