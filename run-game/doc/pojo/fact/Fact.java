package run.game.dao.pojo.fact;

/**
 * Факты о сущностях
 * Например, значения слова, звучание слова и т.п.
 */
public class Fact {

    /**
     * О какой сущности
     */
    Item item;

    /**
     * Тип факта
     */
    FactType type;

    /**
     * Содержание факта (перевод слова, картинка)
     */
    String value;

}
