# Пучковые кубы

## Общее

"Пучковый" куб, это `куб`, который на одну координату может дать ноль, одну или более
одной записи. При записи в БД старые записи удаляются по координате и потом добавляются
(а не добавляются или апдейтятся, как в обычном IMoCube кубе).

Имя физической таблицы совпадает с именем куба, т.е. куб полностью владеет своей таблицей
(а не добавляет в нее поля, как обычный IMoCube куб).

## Объявление куба и пространства

todo

## Реализация

Реализация метода calc() выполняетсякак обычно, только вместо ValueSingle на каждую
координату нужно возвращать ValueList

