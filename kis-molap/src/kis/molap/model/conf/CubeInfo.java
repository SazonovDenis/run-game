package kis.molap.model.conf;


import jandcode.commons.named.*;

import java.util.*;


/**
 * Куб - информация о кубе.
 * Хранитель настроек куба. Разные, в том числе непосредственно кубу не нужные (например, период расчета)
 */
public interface CubeInfo extends INamed {

    /**
     * Имя куба (англ.)
     */
    String getName();

    /**
     *
     */
    String getClassName();

    /**
     * Заголовок куба (рус.)
     */
    String getTitle();

    /**
     * Имя пространства.
     * Совпадает с именем таблицы для этого куба в БД
     */
    String getSpace();

    /**
     * @return =true, если куб является 'пучковым'
     */
    boolean isBunch();

    /**
     * @return Поля куба
     */
    List<String> getFields();

    /**
     * Зависит от изменений в таблицах
     *
     * @return Список влияющих таблиц
     */
    List<String> getDepends();

    /**
     * @return Список влияющих кубов.
     * Метод нужен менеджеру расчета; сам куб при расчете предполагает, что все влияющее уже расчитано.
     */
    List<String> getDependCubes();

}
