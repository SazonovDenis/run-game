1. В каком пакете создавать кубы?
2. В каком пакете создавать класс реализатор для куба?
Кубы (объявление в *cfx и класс реализатор) создаются в пакете kis.molap.ntbd.model.cubes;
Имя файла с описнием куба должно совпадать с именем куба. 

3.В каком пакете создавать пространства?
Пространства создаются в пакете kis.molap.ntbd.model.spaces;


Пространство объявленное в моделе появится в физ. базе после пересоздания базы.

Поля, объявленные в кубе, которые используют определнное пространство появятся в таблице
этого пространства так же после пересоздания базы.

Пространство привносит собой в бд таблицу и поля, соответствующей координатам
пространства.

Куб не является описнием или воплощением физ. таблицей, куб является классом для
заполнения таблицей данными, а так же доополняет физ. таблицу полями.

 ****Не хватает описания бизнес логики и модели взаимодействия пространства с кубами!****
 
****Тестирование куба****
Создаем тестовый класс, размещаем его в test/kis/molap/ntbd/model/cubes, 
наследуемся от CubeBase_Test, используем метод "byOneCoord"

```
@Test
void byOneCoord() throws Exception {
    // Создаем куб
    ICalcData cube = createCube()

    // Результат
    ICalcResultStream res = new CalcResultStreamArray()

    // Что считаем
    Coord coord = new CoordImpl()
    coord.put("well", 1041)
    //
    CoordList coords = new CoordListImpl()
    coords.add(coord)
    println()
    println("coords:")
    UtMolapPrint.printCoordList(coords)

    //
    XDate intervalDbeg = XDate.create("2023-01-20")
    XDate intervalDend = XDate.create("2023-01-20")

    // Пересчет
    cube.calc(coords, intervalDbeg, intervalDend, res)

    //
    println()
    println("values:")
    UtMolapPrint.printValuesList(res)
}


```