### run.game.dao.backstage.Item_list.find

это костыль, с кучей внутренних настроек, наружу надо белее полезный метод, и именно туда
все гораничения

### run.game.dao.backstage.Item_list

методы поиска возвращает Store то структуры "Item", то "Fact.list"

### run.game.dao.backstage.TaskGenerator

Варианты неправильных ответов подбираются с ИСКЛЮЧЕНИЕМ заведомо выученных фактов:

- **done** в качестве вопросов
- **todo**: в качестве НЕПРАВИЛЬНЫХ вариантов (т.к. это облегчит угадывание методом
  исключения)

### Задания при создании и редактировании плана

генерация недостающих заданий при создании (run.game.dao.backstage.Plan_upd.ins)
и редактировании плана (run.game.dao.backstage.Plan_upd.***)
вынести из создания плана (checkPlanTasks) и перенести в код более верхненго уровня

Учесть, что при генерации плана не всегда есть нужные Task

### Рейтинг при показе игры

Рейтинг при показе резкльтатов игры, в случае если в одной игре было несколько заданий на
одну пару фактов. В этом случае в суммарном заработанном за игру рейтинге дублируется
рейтинг по каждому Task, и показаны лишние рейтинги по каждому Task, а нужно чтобы рейтинг
был только для фактов.

### run.game.dao.backstage.TaskGeneratorImpl.generateItemFactsCombinations

не использованы factTagQuestion, factTagAnswer

### run.game.dao.backstage.TaskGeneratorImpl.prepareValuesFalse

язык слова надо передавать, а не угадывать

~~~
String lang
if (UtWord.isAlphasEng(valueTrue)) {
    lang = "eng"
} else 
...
~~~