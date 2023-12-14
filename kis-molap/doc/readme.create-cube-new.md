## Создание нового куба

### Объявление куба

Чтобы создать новый куб, нужно:

1. Объявить куб в модели, указав

- `name` - уникальное наименование
- `space` - пространство
- `class` - класс реализатор

- `<depend>` - изменения в каких таблицах будут поступать кубу
- `<dependCube>` - данные из каких других кубов нужны кубу (влияющие кубы)

```js title=Cube_WellProd.cfx
<cube name="Cube_WellProd"
      title="Подневные данные по добыче для скважин"
      space="Space_WellDt"
      class="kis.molap.ntbd.model.cubes.Cube_WellProd">

    <!-- Зависит от изменений в таблицах -->
    <depend name="LiquidProd"/>
    <depend name="LiquidProdBsw"/>

    <!-- Зависит от кубов -->
    <dependCube name="Cube_Dates"/>
    <dependCube name="Cube_WorkedTime"/>
    <dependCube name="Cube_DensityOilWell"/>

</cube>
```

2. Дополнить физическую таблицу, объявленную в space, _полями_, т.е. показателями, которые
   расчитывает куб.

```js title=Cube_WellProd.cfx
<!-- Дополнение физической таблицы для space -->
<domain name="Cube_WellDt">

    <!-- Физические поля лдля показателей куба -->
    <field name="oil" parent="double" title="Добыча нефти (т)"/>
    <field name="liquidProd" parent="double" title="Добыча жидкости (м³)"/>
    <field name="liquidProdBsw" parent="double" title="Обводненность (%)"/>

</domain>
```

3. Написать класс, реализатор интерфейса `Cube`, в котором реализовать интерфейсы:

- `ICalcData` - расчет значений (см. kis.molap.model.cube.ICalcData)
- `ICalcCoords` - отслеживание изменений (см. kis.molap.model.cube.ICalcCoords)

### Проверка куба

Чтобы проверить, правильно ли зарегистрировался куб выполните в тесте:

```
@Test
public void info() throws Exception {
    ModelService modelService = app.bean(ModelService.class);
    WorkerService workerService = modelService.getModel().bean(WorkerService.class);
    Worker worker = workerService.createWorker();

    Store stInfo = worker.loadInfo(null);
    UtOutTable.outTable(stInfo);
}
```

В общем списке кубов должен появиться новый куб (см. строку `Cube_WellProd...`):

```text
+-------------------+----------+----------+-------+-------+--------+------------+---------------------+--------------------+---------------------+-----------------+
|     cube_name     |  dt_min  |  dt_max  |done_id|done_dt|rec_rate|one_rec_cost|one_rec_durationdirty|one_rec_durationcalc|one_rec_durationwrite|interval_rec_cost|
+-------------------+----------+----------+-------+-------+--------+------------+---------------------+--------------------+---------------------+-----------------+
|Cube_Dates         |2022-01-01|2023-12-31|      0|<null> |  <null>|      <null>|               <null>|              <null>|               <null>|           <null>|
|Cube_DensityOilGeo |2022-01-01|2023-12-31|      0|<null> |  <null>|      <null>|               <null>|              <null>|               <null>|           <null>|
|Cube_DensityOilWell|2022-01-01|2023-12-31|      0|<null> |  <null>|      <null>|               <null>|              <null>|               <null>|           <null>|
|Cube_WellProd      |<null>    |<null>    |      0|<null> |  <null>|      <null>|               <null>|              <null>|               <null>|           <null>|
|Cube_WorkedTime    |2022-01-01|2023-12-31|      0|<null> |  <null>|      <null>|               <null>|              <null>|               <null>|           <null>|
+-------------------+----------+----------+-------+-------+--------+------------+---------------------+--------------------+---------------------+-----------------+
```
