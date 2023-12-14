package kis.molap.model.value;

import kis.molap.model.value.impl.*;

/**
 * Значение координаты. Несколько одиночных значений (для одной координаты).
 * По структуре - List, где элементами являются одиночные значения.
 * <p>
 * Представляет некоторые поля нескольких записей в пространстве куба.
 */
public interface ValueList extends Value {

    static ValueList create() {
        return new ValueListImpl();
    }

    void add(ValueSingle value);

    int size();

    ValueSingle get(int i);

}
