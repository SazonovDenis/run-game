### Совпадающий spelling разных item

Слова `какао` по-казахски и по-русски пишутся одинаково, при составлении заданий система
не знает, какой item что выбрать

### Генерация поиск пары фактов по значению в generatePlanFact

Если искать по переводу "зеленый", то получим сразу два item: "жасыл" (kz) и
"green" (en), что вообще-то требует фильтрации по направлению. Когда будет много языков
романской группы, то вообще сложно будет

### Фильтр в уровнях

Для библиотеки - по языкам, типам (аудио/текст), тэгам (сложность).

Поиск убрать совсем или сделать сплошным(?), по словам.

### **done** После показа вопроса сделать паузу перед показом вариантов

Чтобы дать возможность вспомнить верный ответ

### run.game.dao.backstage.TaskGenerator

Варианты неправильных ответов подбираются с ИСКЛЮЧЕНИЕМ заведомо выученных фактов:

- в качестве вопросов
- в качестве НЕПРАВИЛЬНЫХ вариантов (т.к. это облегчит угадывание методом исключения)

### run.game.dao.game.ServerImpl.findStill

1. При выдаче слов убрать артикли, предлоги и т.п. - очень мещают
2. Порядок слов не соблюден!
3. Догрузить аудио в список предпросмотра (там надо победить разнообразие вариантов)

