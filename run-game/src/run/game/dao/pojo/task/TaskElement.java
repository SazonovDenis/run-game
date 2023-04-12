package run.game.dao.pojo.task;

/**
 * Элемент тестового задания (само задание или вариант ответа)
 * Вся необходимая информация для формирования элемента на экране
 */
public class TaskElement {

    /**
     * Формат элемента (строка, текст, картинка, аудио)
     */
    public TaskElementType type;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }

    /**
     * Представление элемента
     */
    public String value;

}
