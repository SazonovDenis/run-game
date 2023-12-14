## Создание нового пространства

Чтобы создать пространство, нужно:

1. Объявить пространство в модели, указав:

- `name` - уникальное наименование
- `table` - в какой физической таблице будут располагаться данные пространства
- `class` - класс реализатор
- `<coord>` - _координаты_ пространства

```js title=Space_WellDt.cfx
<space name="Cube_WellDt"
       title="Пространство: скважина + день."
       class="kis.molap.ntbd.model.spaces.Space_WellDt">

    <!-- Координаты -->
    <coord name="well"/>
    <coord name="dt"/>

</space>
```

2. Пространство создает физическую таблицу с координатами пространства, её domain
   объявляется tag.db = "true".

```js title=Space_WellDt.cfx
<domain name="Cube_WellDt" parent="wax.Table" tag.db="true">

    <!-- Координаты -->
    <field name="well" parent="long" title="Скважина"/>
    <field name="dt" parent="date" title="Дата (день)"/>

    <!-- Индекс для координат -->
    <dbindex name="main" fields="well,dt" unique="true"/>

</domain>
```

Поля для значений кубов дополнят сами кубы - наложением из файла с описанием
соответствующего куба.

3. Написать класс, в котором реализовать интерфейc:

- `Space` - расчет координат (см. kis.molap.model.cube.Space)

