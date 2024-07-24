package run.game.dao.game

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import kis.molap.ntbd.model.cubes.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.dao.ocr.*
import run.game.dao.statistic.*
import run.game.model.service.*
import run.game.util.*

public class ServerImpl extends RgmMdbUtils implements Server {

    private long MAX_FACT_FOR_GAME = 8
    private long MAX_TASK_COUNT_FOR_FACT_IN_GAME = 2
    private long MAX_TASK_COUNT_FOR_FACT_IN_GAME_IF_ERROR = 4
    private long MAX_TASK_FOR_GAME = 10
    private double RATING_DECREASE_FOR_STARRED = 0.1

    private Rnd rnd = new RndImpl()


    @DaoMethod
    public DataBox getGame(long idGame) {
        long idUsr = getContextOrCurrentUsrId()

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public DataBox getLastGame() {
        long idUsr = getContextOrCurrentUsrId()

        //
        StoreRecord recGame = mdb.loadQueryRecord(sqlLastGame(), [usr: idUsr], false)
        if (recGame == null) {
            return null
        }

        //
        long idGame = recGame.getLong("id")

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public DataBox getActiveGame() {
        long idUsr = getCurrentUsrId()

        //
        StoreRecord recGame = loadActiveGameRec()
        if (recGame == null) {
            return null
        }

        //
        long idGame = recGame.getLong("id")

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public void closeActiveGame() {
        long idUsr = getCurrentUsrId()

        // Все незакрытые игры
        Store stGames = mdb.loadQuery(sqlActiveGames(), [usr: idUsr])

        //
        for (StoreRecord recGame : stGames) {
            long idGame = recGame.getLong("id")

            // Закроем игру
            XDateTime dt = XDateTime.now()
            mdb.execQuery(sqlCloseGame(), [game: idGame, dt: dt])

            // Пересчитаем кубы
            long idPlan = recGame.getLong("plan")
            RgmCubeUtils cubeUtils = mdb.create(RgmCubeUtils)
            cubeUtils.cubesRecalc(idUsr, idGame, idPlan)
        }
    }


    protected StoreRecord loadActiveGameRec() {
        long idUsr = getCurrentUsrId()
        Store stGames = mdb.loadQuery(sqlActiveGames(), [usr: idUsr])

        //
        if (stGames.size() == 0) {
            return null
        }

        //
        StoreRecord recGame = stGames.get(0)

        //
        return recGame
    }


    @DaoMethod
    public DataBox gameStart(long idPlan) {
        // Добавляем Game
        StoreRecord recGame = mdb.createStoreRecord("Game")
        XDateTime dt = XDateTime.now()
        recGame.setValue("dbeg", dt)
        recGame.setValue("plan", idPlan)


        // ---
        // Game
        long idGame = mdb.insertRec("Game", recGame)


        // ---
        // Добавим участника игры
        long idUsr = getCurrentUsrId()
        mdb.insertRec("GameUsr", [usr: idUsr, game: idGame])


        // ---
        // Отберем подходящие факты (самые плохо выученные с учетом статистики пользователя),
        // а по ним - задания на игру.


        // ---
        // Факты


        // Загрузим факты в плане
        Store stPlanFact = mdb.loadQuery(sqlPlanFact(), [plan: idPlan, usr: idUsr])

        // Слегка рандомизируем рейтинг фактов - иначе для фактов без рейтинга
        // (например, для которых никогда не выдавали заданий) рейтинг перестает быть
        // хорошим выбором порядка - от игры к игре будет выбираться список идущих подряд
        // одних тех же слов с нулевым рейтингом - по порядку в sql-запросе, а нам нужно случайно.
        for (StoreRecord recPlanFact : stPlanFact) {
            // Получаем поправку, чтобы получить изменение не более 10%
            double rndSeed = UtCubeRating.RATING_FACT_MAX * rnd.num(0, 1000) / 10000
            // Исказим рейтинг факта
            recPlanFact.setValue("ratingTask", recPlanFact.getDouble("ratingTask") + rndSeed)
        }

        // Откорректируем рейтинг - от фактов, помеченных как "любимые",
        // отнимем немного баллов, чтобы они с большей вероятностью выпадали
        for (StoreRecord recPlanFact : stPlanFact) {
            // Факт помечен как "isKnownBad"?
            if (recPlanFact.getBoolean("isKnownBad")) {
                recPlanFact.setValue("ratingTask", recPlanFact.getDouble("ratingTask") - recPlanFact.getDouble("ratingTask") * RATING_DECREASE_FOR_STARRED)
            }
        }

        // Сортируем факты по рейтингу
        stPlanFact.sort("ratingTask,ratingQuickness")


        // Отберем факты на игру.
        // Начинаем с начала (там самый плохой рейтинг) и выходим
        // при достижении максимального количества фактов.
        Set<String> factsForGame = new HashSet<>()
        for (StoreRecord recPlanFact : stPlanFact) {
            // Скрытые факты не выдаем
            if (recPlanFact.getBoolean("isHidden")) {
                continue
            }

            // Копим факты
            long factQuestion = recPlanFact.getLong("factQuestion")
            long factAnswer = recPlanFact.getLong("factAnswer")
            String key = factQuestion + "_" + factAnswer
            factsForGame.add(key)

            // Набрали фактов сколько нужно
            if (factsForGame.size() >= MAX_FACT_FOR_GAME) {
                break
            }
        }


        // ---
        // Задания


        // Загрузим задания для плана
        Store stPlanTask = mdb.loadQuery(sqlPlanTask(), [plan: idPlan, usr: idUsr])

        // Слегка рандомизируем порядок заданий - иначе всегда будут выходить
        // одни и те же задания - по порядку в sql-запросе, а нам нужно случайно.
        // Кроме того, учет рейтинга факта позволит добавить больше
        // повторных заданий на факты с самым низким рейтингом.
        for (StoreRecord recPlanTask : stPlanTask) {
            // Получаем поправку, чтобы получить изменение не более 10%
            double rndSeed = UtCubeRating.RATING_FACT_MAX * rnd.num(0, 1000) / 10000
            // Исказим рейтинг факта задания
            recPlanTask.setValue("ratingTask", recPlanTask.getDouble("ratingTask") + rndSeed)
        }

        // Сортируем задания по рейтингу факта
        stPlanTask.sort("ratingTask")


        // Для отобранных фактов выберем задания на игру
        Map<String, Integer> factsForGameSelected = new HashMap<>()
        long taskForGameCount = 0
        for (StoreRecord recTask : stPlanTask) {
            long factQuestion = recTask.getLong("factQuestion")
            long factAnswer = recTask.getLong("factAnswer")
            String key = factQuestion + "_" + factAnswer

            // Отбираем задания только среди выбранных фактов
            if (!factsForGame.contains(key)) {
                continue
            }

            // Не превышаем максимального количества заданий на каждую пару фактов в одной игре,
            // а то скучно получать задания про одно и то же.
            int tasksForFactSelectedCount = factsForGameSelected.getOrDefault(key, 0)

            // Для такой пары фактов еще можно добавить задание?
            if (tasksForFactSelectedCount >= MAX_TASK_COUNT_FOR_FACT_IN_GAME) {
                continue
            }

            // Запоминаем количество заданий на игру для каждой пары фактов.
            tasksForFactSelectedCount = tasksForFactSelectedCount + 1
            factsForGameSelected.put(key, tasksForFactSelectedCount)

            // Добавляем задание
            mdb.insertRec("GameTask", [
                    game: idGame,
                    usr : idUsr,
                    task: recTask.getLong("task"),
            ])

            // Набрали заданий на игру сколько нужно?
            taskForGameCount = taskForGameCount + 1
            if (taskForGameCount >= MAX_TASK_FOR_GAME) {
                break
            }
        }


        // ---
        return loadAndPrepareGame_Short(idGame, idUsr)
    }


    @DaoMethod
    public DataBox choiceTask(long idGame) {
        // Выбираем, что осталось спросить по плану
        StoreRecord recGameTask = choiceGameTask(idGame)

        // Закрываем игру, если нечего
        if (recGameTask == null) {
            closeActiveGame()
        }

        // Записываем факт выдачи задания для пользователя
        if (recGameTask != null) {
            long idGameTask = recGameTask.getLong("id")
            XDateTime dtTask = XDateTime.now()
            mdb.updateRec("GameTask", [id: idGameTask, dtTask: dtTask])
        }

        //
        return loadAndPrepareGameTask(idGame, recGameTask)
    }


    @DaoMethod
    public DataBox currentTask(long idGame) {
        // Выбираем последнее выданное, но не отвеченное задание
        StoreRecord recGameTask = getGameLastTask(idGame)

        //
        return loadAndPrepareGameTask(idGame, recGameTask)
    }


    /**
     * Записываем результат выполнения задания
     *
     * @param idGameTask id ранее выданного задания GameTask.id)
     * @param taskResult состояние ответа на задание (wasTrue, wasFalse, wasHint, wasSkip)
     */
    @DaoMethod
    public void postTaskAnswer(long idGameTask, Map taskResult) {
        // Загрузим выданное задание. Если задание чужое, то запись
        // загрузится пустой и будет ошибка, что нормально.
        long idUsr = getCurrentUsrId()
        StoreRecord recGameTask = mdb.loadQueryRecord(sqlGameTask(), [id: idGameTask, usr: idUsr])


        // Валидация
        if (taskResult.get("wasTrue") != true &&
                taskResult.get("wasFalse") != true &&
                taskResult.get("wasHint") != true &&
                taskResult.get("wasSkip") != true
        ) {
            mdb.validateErrors.addError("Не указан выбранный ответ на задание")
        }

        //
        if (!recGameTask.isValueNull("dtAnswer")) {
            mdb.validateErrors.addError("Ответ на задание уже дан")
        }

        //
        mdb.validateErrors.checkErrors()


        // --- Обновляем задание
        recGameTask.setValue("dtAnswer", XDateTime.now())
        recGameTask.setValue("wasTrue", taskResult.get("wasTrue"))
        recGameTask.setValue("wasFalse", taskResult.get("wasFalse"))
        recGameTask.setValue("wasHint", taskResult.get("wasHint"))
        recGameTask.setValue("wasSkip", taskResult.get("wasSkip"))
        mdb.updateRec("GameTask", recGameTask)


        // --- Игра окончена?
        // Выбираем, осталось что-нибудь спросить по плану
        long idGame = recGameTask.getLong("game")
        StoreRecord stGameTask = mdb.loadQueryRecord(sqlGameTaskRemainderCount(), [
                game: idGame,
                usr : idUsr,
        ])

        // Закрываем игру, если нечего
        if (stGameTask.getLong("cnt") == 0) {
            closeActiveGame()
            return
        }


        // --- При ошибке - попробуем добавить еще одно задание на эти же факты
        if (taskResult.get("wasFalse") == true || taskResult.get("wasHint") == true) {
            // Выясним пару фактов текущего задания
            long idTask = recGameTask.getLong("task")
            StoreRecord recTask = mdb.loadQueryRecord(sqlTask(), [id: idTask])
            long factQuestion = recTask.getLong("factQuestion")
            long factAnswer = recTask.getLong("factAnswer")

            // Есть/были в игре еще вопросы на эти факты?
            Store stOtherTask = mdb.loadQuery(
                    sqlOtherTask(),
                    [game: idGame, usr: idUsr, factQuestion: factQuestion, factAnswer: factAnswer]
            )
            //
            if (stOtherTask.size() >= MAX_TASK_COUNT_FOR_FACT_IN_GAME_IF_ERROR) {
                // Заданий с такми фактами уже достаточно, новое не нужно
                return
            }

            // Повторим задание
            mdb.insertRec("GameTask",
                    [game: idGame, usr: idUsr, task: idTask]
            )
        }

    }


    @DaoMethod
    DataBox getPlanTasks(long idPlan) {
        DataBox res = new DataBox()

        //
        long idUsr = getContextOrCurrentUsrId()

        //
        Plan_list list = mdb.create(Plan_list)
        StoreRecord recPlanRaw = list.getPlan(idPlan)


        // --- plan

        // План
        StoreRecord recPlan = mdb.createStoreRecord("Plan.server")
        recPlan.setValues(recPlanRaw.getValues())
        //
        if (recPlan.getBoolean("isOwner")) {
            recPlan.setValue("isAllowed", true)
        }


        // --- tasks

        // Факты в плане - список
        Store stPlanFact = mdb.createStore("PlanFact.list")
        mdb.loadQuery(stPlanFact, sqlPlanFactsStatistic(), [usr: idUsr, plan: idPlan])

        // Дополним факты в плане "богатыми" данными для вопроса и ответа
        // plan -> список PlanFact -> PlanFact.factQuestion -> Fact.item - > Fact.* для этого item, но нужного типа
        fillFactBodyPlan(stPlanFact, idPlan)


        // --- statistic
        StoreRecord recStatistic = loadRecStatistic(recPlanRaw, stPlanFact)


        // ---
        res.put("plan", recPlan)
        res.put("tasks", stPlanFact)
        res.put("statistic", recStatistic.getValues())

        //
        return res
    }

    StoreRecord loadRecStatistic(StoreRecord recPlanRaw, Store stPlanFact) {
        StoreRecord recStatistic = mdb.createStoreRecord("Plan.statistic.rec")

        // По всем заданиям игры - сумма баллов и т.д.
        recStatistic.setValue("ratingTask", recPlanRaw.getValue("ratingTask"))
        recStatistic.setValue("ratingQuickness", recPlanRaw.getValue("ratingQuickness"))

        // Считаем количество нескрытых и выученных фактов в плане
        int wordCount = 0
        int wordCountLearnedDiff = 0
        for (StoreRecord rec : stPlanFact) {
            if (!rec.getBoolean("isHidden")) {
                wordCount = wordCount + 1
                if (rec.getDouble("ratingTask") == UtCubeRating.RATING_FACT_MAX) {
                    wordCountLearnedDiff = wordCountLearnedDiff + 1
                }
            }
        }
        //
        recStatistic.setValue("wordCount", wordCount)
        recStatistic.setValue("wordCountLearnedDiff", wordCountLearnedDiff)
        // Максимальный балл берем на основе количества нескрытых фактов в плане
        recStatistic.setValue("ratingMax", wordCount * UtCubeRating.RATING_FACT_MAX)


        //
        return recStatistic
    }

    @DaoMethod
    DataBox getPlanStatistic(long idPlan, XDate dbeg, XDate dend) {
        DataBox res = new DataBox()

        //
        long idUsr = getContextOrCurrentUsrId()

        //
        Plan_list list = mdb.create(Plan_list)
        StoreRecord recPlanRaw = list.getPlan(idPlan)


        // --- plan

        // План
        StoreRecord recPlan = mdb.createStoreRecord("Plan.server")
        recPlan.setValues(recPlanRaw.getValues())
        //
        if (recPlan.getBoolean("isOwner")) {
            recPlan.setValue("isAllowed", true)
        }

        // --- tasks

        // Факты в плане - список
        Store stPlanFact = mdb.createStore("PlanFact.list")
        mdb.loadQuery(stPlanFact, sqlPlanFactsStatistic(), [usr: idUsr, plan: idPlan])


        // --- statistic
        StoreRecord recStatistic = loadRecStatistic(recPlanRaw, stPlanFact)


        // --- statisticByDay
        XDateTime dendParam = dend.addDays(1).toDateTime().addMSec(-1000)
        XDateTime dbegParam = dbeg.toDateTime()
        Statistic_list statisticList = mdb.create(Statistic_list)
        DataBox resStatistic = statisticList.forPlan(idPlan, dbegParam, dendParam)
        Store stSatisticByDay = resStatistic.get("statisticByDay")
        Map mapStatisticPeriod = resStatistic.get("statisticPeriod")


        // ---
        res.put("plan", recPlan)
        res.put("statistic", recStatistic.getValues())
        res.put("statisticPeriod", mapStatisticPeriod)
        res.put("statisticByDay", stSatisticByDay)

        //
        return res
    }


    @DaoMethod
    Store findItems(String text, long idPlan) {
        // --- Слова
        Map<String, Set> textItems = new LinkedHashMap<>()
        textItems.put(text, null)

        // --- Поиск слов среди введенного
        Store stItem = collectItems(textItems, null)

        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan)

        // ---
        return stFact
    }

    @DaoMethod
    DataBox findStill(String imgBase64, long idPlan) {
        // Разберем картинку
        Ocr ocr = mdb.create(Ocr)
        Store stParsedText = ocr.parseStillMarkup(imgBase64)


        // --- Раскладываем результаты OCR на части

        // Слова и их позиции
        Map<String, Set> textItems = new LinkedHashMap<>()
        // Позиции слов (общий список)
        List textPositions = new ArrayList<>()
        // Раскладываем
        splitOcrResult(stParsedText, textItems, textPositions)


        // --- Поиск слов среди введенного
        Map<String, Set> textItemsFiltered = new HashMap<>()
        Store stItem = collectItems(textItems, textItemsFiltered)


        // --- Заполнение свойств найденных слов
        Store stFact = makeFactList(stItem, idPlan)


        // --- Позиции
        List wordList = textItemsFiltered.keySet().toList()
        Map<Object, List<StoreRecord>> mapItems = StoreUtils.collectGroupBy_records(stItem, "value")
        //
        Store stItemPositions = mdb.createStore("Tesseract.items")
        for (String word : wordList) {
            List recs = mapItems.get(word)
            long item = 0
            if (recs != null) {
                item = recs.get(0).getLong("id")
            }
            StoreRecord rec = stItemPositions.add()
            rec.setValue("item", item)
            rec.setValue("itemValue", word)
            rec.setValue("positions", textItemsFiltered.get(word))
        }


        // ---
        DataBox res = new DataBox()
        res.put("facts", stFact)
        res.put("positions", stItemPositions)
        return res
    }


    Store collectItems(Map<String, Set> textItemsRaw, Map<String, Set> textItemsFiltered) {
        // --- Обработка найденного: фильтрация и чистка
        Map<String, Set> textItems = filterTextItems(textItemsRaw, (word, item) -> {
            return Item_list.filterAndSplitWord(word)
        })


        // --- Обработка найденного: стоп-слова

        // Получаем из кэша стоп-слова
        WordCacheService wordService = mdb.getModel().bean(WordCacheService)
        StoreIndex idxOcrStopWords = wordService.getIdxOcrStopWords()

        // Фильтруем по стоп-словам
        textItems = filterTextItems(textItems, (word, item) -> {
            List res = new ArrayList()
            if (idxOcrStopWords.get(word.toLowerCase()) == null) {
                res.add(word)
            }
            return res
        })


        // --- Поиск по точному совпадению

        // Ищем Items по точному совпадению с textItems
        List<String> wordList = textItems.keySet().toList()
        Item_list itemsList = mdb.create(Item_list)
        Store stItem = itemsList.findText(wordList)


        // --- Поиск с перобразованем словоформ

        // Проверим, какие слова не нашлись по непосредственному написанию
        StoreIndex idxItems = stItem.getIndex("value")
        Map<String, Set> textItemsNotFound = new LinkedHashMap<>()
        //
        for (String word : wordList) {
            if (idxItems.get(word) == null) {
                textItemsNotFound.put(word, textItems.get(word))
            }
        }

        // Если часть слов не нашлось - выполняем преобразование словоформ и делаем вторую попытку
        Map<String, Set> textItemsTranformed = filterTextItems(textItemsNotFound, (word, item) -> {
            return Item_list.transformWord(word)
        })

        if (textItemsTranformed.size() != 0) {
            List textListTranformed = textItemsTranformed.keySet().toList()

            // Ищем Items по преобразованным text
            Store stItemTransformed = itemsList.findText(textListTranformed)

            // Обновляем наши списки
            stItemTransformed.copyTo(stItem)
            //
            textItems.putAll(textItemsTranformed)
        }


        // --- Поиск по фрагменту

        // Если искали одно слово и нашили мало - то поищем ещё и по фрагменту
        if (wordList.size() == 1 && stItem.size() <= itemsList.MAX_COUNT_FOUND) {
            // Ищем Items по фрагменту
            Set itemsFound = stItem.getUniqueValues("value")
            String word = wordList.get(0)
            Store stItemWord = itemsList.findWord(word)

            // Обновляем наши списки
            for (StoreRecord recItemWord : stItemWord) {
                String value = recItemWord.getString("value")
                if (!itemsFound.contains(value)) {
                    stItem.add(recItemWord)
                    itemsFound.add(value)
                }
            }
        }


        // ---
        if (textItemsFiltered != null) {
            textItemsFiltered.putAll(textItems)
        }
        //
        return stItem
    }

    Store makeFactList(Store stItem, long idPlan) {
        // --- Превратим список Item в список пар фактов (сгенерим комбинации)
        Store stFact = loadFactList(stItem, idPlan)

        // --- Дополним факты в плане "богатыми" данными для вопроса и ответа
        fillFactBody(stFact)

        // --- Обеспечим порядок, как в исходном тексте
        Store stFactOrderd = makeOrdered(stItem, stFact)

        //
        return stFactOrderd
    }


    interface IWordAnalyzer {
        Collection<String> analyzeText(String text, Set item)
    }

    /**
     * Обрабатывает каждый элемент из textItems с помощью обработчика wordAnalyzer.
     *
     * @param textItems список вхождений текстов, где ключ - текст, а значения - набор мест расположения текста на изображении
     * @param wordAnalyzer
     *
     * @return Новый список, может быть больше или меньше исходного
     */
    Map<String, Set> filterTextItems(Map<String, Set<Map>> textItems, IWordAnalyzer wordAnalyzer) {
        Map<String, Set> textItemsRes = new LinkedHashMap<>()

        for (String itemText : textItems.keySet()) {
            Set textItemPositions = textItems.get(itemText)
            List wordsFiltered = wordAnalyzer.analyzeText(itemText, textItemPositions)
            for (String wordFiltered : wordsFiltered) {
                Set textItemRes = textItemsRes.get(wordFiltered)
                if (textItemRes == null) {
                    textItemRes = new HashSet()
                    textItemsRes.put(wordFiltered, textItemRes)
                }

                // В случае, если из одного слова в itemText получилось несколько слов в wordsFiltered -
                // также разделим исходный общий большой прямоугольник положения текста
                // на несколько меньших, для каждого нового wordFiltered, пропорционально длине нового слова.
                Set<Map> textItemPositionsSplitted = splitTextItemSinglePosition(textItemPositions, wordsFiltered, wordFiltered)
                textItemRes.addAll(textItemPositionsSplitted)
            }
        }

        return textItemsRes
    }


    Set<Map> splitTextItemSinglePosition(Set<Map> textItemPositions, List<String> words, String thisWord) {
        Set<Map> res = new HashSet<>()

        //
        int wordsCarsCount = 0
        for (String word : words) {
            wordsCarsCount = wordsCarsCount + word.size()
        }
        //
        int wordCharPos = 0
        for (String word : words) {
            if (thisWord.equals(word)) {
                break
            }
            wordCharPos = wordCharPos + word.size()
        }
        //
        int wordCarsCount = thisWord.size()

        //
        for (Map textItemSinglePosition : textItemPositions) {
            int topPx = textItemSinglePosition.get("top")
            int leftPx = textItemSinglePosition.get("left")
            int heightPx = textItemSinglePosition.get("height")
            int widthPx = textItemSinglePosition.get("width")
            //
            double pxByChar = widthPx / wordsCarsCount
            leftPx = leftPx + (pxByChar * wordCharPos)
            widthPx = (pxByChar * wordCarsCount)
            //
            Map textItemSinglePositionNew = new HashMap(textItemSinglePosition)
            textItemSinglePositionNew.put("top", topPx)
            textItemSinglePositionNew.put("left", leftPx)
            textItemSinglePositionNew.put("height", heightPx)
            textItemSinglePositionNew.put("width", widthPx)
            res.add(textItemSinglePositionNew)
        }

        return res
    }

    void splitText(String text, Map<String, Set> textItems) {
        text = text.trim()
/*
        Collection<String> words1 = Item_list. filterAndSplitWord(word)

        Collection textItems = text.split(" ")
*/

        textItems.put(text, null)
    }

    /**
     *
     * @param stText данные от Tesseract
     * @param textItems разобранные слова, ключ: слово, значение: список позиций, где встретился
     * @param textItemsPosition отдельно спискок позиций
     */
    void splitOcrResult(Store stText, Map<String, Set> textItems, List textItemsPosition) {
        for (int i = 0; i < stText.size(); i++) {
            StoreRecord rec = stText.get(i)

            //
            int conf = rec.getInt("conf")

            //
            String text = rec.getString("text")
            text = text.trim()

            //
            if (conf == 0 || text.length() == 0) {
                continue
            }


            // Слияние по переносам
            Map textPositionNext = null
            if (i < stText.size() - 2) {
                StoreRecord recNext = stText.get(i + 2)
                int line_num = rec.getInt("line_num")
                int line_numNext = recNext.getInt("line_num")
                String textNext = recNext.getString("text")
                textNext = textNext.trim()
                //
                if (text.endsWith("-") && (line_numNext - line_num) == 1) {
                    text = text.substring(0, text.length() - 1) + textNext

                    textPositionNext = [
                            text  : text,
                            left  : recNext.getInt("left"),
                            top   : recNext.getInt("top"),
                            width : recNext.getInt("width"),
                            height: recNext.getInt("height"),
                    ]

                    //
                    i = i + 2
                }
            }

            // Формируем позицию
            if (textPositionNext != null) {
                textItemsPosition.add(textPositionNext)
            }

            //
            Map textPosition = [
                    text  : text,
                    left  : rec.getInt("left"),
                    top   : rec.getInt("top"),
                    width : rec.getInt("width"),
                    height: rec.getInt("height"),
            ]
            textItemsPosition.add(textPosition)

            //
            Set textItemPositions = textItems.get(text)
            if (textItemPositions == null) {
                textItemPositions = new HashSet()
                textItems.put(text, textItemPositions)
            }
            textItemPositions.add(textPosition)
            //
            if (textPositionNext != null) {
                textItemPositions.add(textPositionNext)
            }
        }
    }

    Store makeOrdered(Store stItem, Store stFact) {
        Store stFactRes = mdb.createStore("PlanFact.list")

        //
        Map<Object, List<StoreRecord>> factsByItems = StoreUtils.collectGroupBy_records(stFact, ["item"])
        for (StoreRecord recItem : stItem) {
            String item = recItem.getString("id")
            List<StoreRecord> lstItemFacts = factsByItems.get(item)
            if (lstItemFacts != null) {
                for (StoreRecord recFact : lstItemFacts) {
                    stFactRes.add(recFact)
                }
            }
        }

        //
        return stFactRes
    }


    Store loadFactList(Store stItem, long idPlan) {
        Store stPlanFact = mdb.createStore("PlanFact.list")

        // Соберем отдельно item, отдельно конкретные факты
        Set itemIds = new HashSet()
        Set factIds = new HashSet()
        for (StoreRecord recItem : stItem) {
            if (recItem.getLong("fact") == 0) {
                itemIds.add(recItem.getLong("id"))
            } else {
                factIds.add(recItem.getLong("fact"))
            }
        }

        // Поиск Item: формируем пары фактов spelling -> translate, т.е. все варианты перевода (для списка itemIds).
        // Список с информацией пользователя, наличие/отсутствие в указанном плане, статистикой.
        long idUsr = getContextOrCurrentUsrId()
        mdb.loadQuery(stPlanFact, sqlPlanFactStatistic_Items(itemIds), [usr: idUsr, plan: idPlan])

        // Поиск Fact
        mdb.loadQuery(stPlanFact, sqlPlanFactStatistic_Facts(factIds), [usr: idUsr, plan: idPlan])

        //
        return stPlanFact
    }


    String sqlFactData_Items(Set items, long dataType) {
        String itemsStr = "0"
        if (items.size() > 0) {
            itemsStr = items.join(",")
        }

        return """ 
-- Значения фактов типа dataType, по списку сущностей
select 
    Item.id item,
    Fact.id fact,
    Fact.dataType dataType,
    Fact.value value

from    
    Item
    -- Факт типа dataType
    join Fact on (
        Item.id = Fact.item and
        Fact.dataType = ${dataType}
    )
    
where
    Item.id in (${itemsStr})    
"""
    }


    String sqlFactData_Facts(Set factIds, String factFieldKeyName) {
        String factsStr = "0"
        if (factIds.size() > 0) {
            factsStr = factIds.join(",")
        }
        if (factFieldKeyName == null) {
            factFieldKeyName = "fact"
        }

        return """ 
-- Значения фактов, по списку фактов
select 
    Fact.item item,
    Fact.id as ${factFieldKeyName},
    Fact.dataType dataType,
    Fact.value value

from    
    Fact
    
where
    Fact.id in (${factsStr})    
"""
    }


    DataBox loadAndPrepareGame_Short(long idGame, long idUsr) {
        DataBox res = new DataBox()

        // Данные по игре
        StoreRecord recGame = mdb.createStoreRecord("Game.server")
        mdb.loadQueryRecord(recGame, sqlGameStatistic(), [game: idGame, usr: idUsr])
        res.put("game", recGame)

        // Задания игры
        Store stGameTasksResult = mdb.createStore("GameTask")
        mdb.loadQuery(stGameTasksResult, sqlGameTasksStatistic(), [game: idGame, usr: idUsr])
        res.put("tasksResult", stGameTasksResult)

        //
        return res
    }


    DataBox loadAndPrepareGame(long idGame, long idUsr) {
        DataBox res = new DataBox()

        //
        StoreRecord recGameRaw = mdb.loadQueryRecord(sqlGameStatistic(), [game: idGame, usr: idUsr])

        // Игра
        StoreRecord recGame = mdb.createStoreRecord("Game.server")
        recGame.setValues(recGameRaw.getValues())

        //
        long idPlan = recGame.getLong("plan")

        // Задания игры - список + результат ответа на каждое задание
        Store stGameTasks = mdb.createStore("GameTask.list")
        mdb.loadQuery(stGameTasks, sqlGameTasksStatistic(), [game: idGame, usr: idUsr])

        // Дополним ФАКТЫ игры "богатыми" данными вопроса и ответа
        fillFactBodyPlan(stGameTasks, idPlan)

        // Дополним ЗАДАНИЯ игры "богатыми" данными вопроса и ответа.
        // Разница между ФАКТАМИ игры и ЗАДАНИЯМИ игры заключается в том, что факты игры
        // берутся из фактов плана (таблица PlanFact), а задания игры берутся из реально
        // заданных вопросов (таблица GameTask и связанные с ней через Task записи в TaskQuestion и TaskOption).
        fillFactBodyGame(stGameTasks, idGame, idUsr)


        // Дополним факты игры статистикой
        Statistic_list statisticList = mdb.create(Statistic_list)
        DataBox resStatisticList = statisticList.forGame(recGame.getDateTime("dbeg"), recGame.getDateTime("dend"))
        Map mapStatistic = resStatisticList.get("statisticPeriod")


        //
        res.put("game", recGame)
        res.put("tasks", stGameTasks)
        res.put("statistic", mapStatistic)

        //
        return res
    }


    String sqlFactData_PlanItem() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.item,
    Fact.dataType,
    Fact.value

from 
    PlanFact
    -- Факт, который привязан к PlanFact.factQuestion
    join Fact PlanFact_Fact on (
        PlanFact.factQuestion = PlanFact_Fact.id
    )
    -- Факты нужных типов для Fact.item
    join Fact on (
        PlanFact_Fact.item = Fact.item and
        Fact.dataType in (${RgmDbConst.DataType_word_spelling + "," + RgmDbConst.DataType_word_sound})
    )

where
    PlanFact.plan = :plan 
"""
    }

    String sqlFactData_PlanQuestion() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.item,
    Fact.dataType,
    Fact.value

from 
    PlanFact
    -- Факт, который привязан к PlanFact.factQuestion
    join Fact on (
        PlanFact.factQuestion = Fact.id
    )

where
    PlanFact.plan = :plan 
"""
    }


    String sqlFactData_PlanAnswer() {
        return """
select 
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Fact.dataType,
    Fact.value

from 
    PlanFact
    -- Факт, который привязан к PlanFact.factAnswer
    join Fact on (
        PlanFact.factAnswer = Fact.id
    )

where
    PlanFact.plan = :plan 
"""
    }


    String sqlFactData_GameTaskQuestion() {
        return """
select 
    GameTask.*,
    TaskQuestion.dataType,
    TaskQuestion.value

from 
    GameTask
    join TaskQuestion on (
        TaskQuestion.task = GameTask.task
    )

where
    GameTask.usr = :usr and 
    GameTask.game = :game 
"""
    }


    String sqlFactData_GameTaskAnswer() {
        return """
select 
    GameTask.*,
    TaskOption.dataType,
    TaskOption.value

from 
    GameTask
    join TaskOption on (
        TaskOption.task = GameTask.task and 
        TaskOption.isTrue = 1 
    )

where
    GameTask.usr = :usr and 
    GameTask.game = :game 
"""
    }


    protected DataBox loadAndPrepareGameTask(long idGame, StoreRecord recGameTask) {
        DataBox res = new DataBox()

        //
        long idUsr = getCurrentUsrId()


        // --- Формируем задание
        if (recGameTask != null) {
            // Грузим задание
            long idTask = recGameTask.getLong("task")
            DataBox resTask = loadTask(idTask)
            StoreRecord recTask = resTask.get("task")
            Store taskOption = resTask.get("taskOption")

            // Дополняем задание технической информацией
            long idGameTask = recGameTask.getLong("id")
            XDateTime dtTask = recGameTask.getDateTime("dtTask")
            recTask.setValue("id", idGameTask)
            recTask.setValue("dtTask", dtTask)

            //
            res.put("task", recTask)
            res.put("taskOption", taskOption)
        }


        // --- Добавим данные по игре
        DataBox resGame = loadAndPrepareGame_Short(idGame, idUsr)
        res.put("game", resGame.get("game"))
        res.put("tasksResult", resGame.get("tasksResult"))


        //
        return res
    }

    /**
     * Загружаем задание и преобразуем по требованиям frontend-api.
     */
    protected DataBox loadTask(long idTask) {
        // --- Грузим задание
        Task_upd upd = mdb.create(Task_upd)
        DataBox task = upd.loadTask(idTask)


        // --- Преобразуем задание по требованиям frontend-api
        StoreRecord resTask = mdb.createStoreRecord("Task.server")
        Store resTaskOption = mdb.createStore("TaskOption.server")

        // Основной вопрос задания
        StoreRecord recTask = task.get("task")
        resTask.setValue("task", recTask.getLong("id"))
        resTask.setValue("dataType", recTask.getLong("dataTypeQuestion"))

        // Делаем плоскую запись на основе значений задания и их типам
        // Превращаем списки stTaskQuestion и stTaskOption в пару плоских записей (см. Task.fields)
        Store stTaskQuestion = task.get("taskQuestion")
        for (StoreRecord recTaskQuestion : stTaskQuestion) {
            convertFactToFlatRecord(recTaskQuestion, resTask)
        }

        // Варианты ответа
        Store stTaskOption = task.get("taskOption")
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recTaskOption = resTaskOption.add()
            recTaskOption.setValue("id", rec.getValue("id"))
            recTaskOption.setValue("isTrue", rec.getValue("isTrue"))
            convertFactToFlatRecord(rec, recTaskOption)
        }


        // ---
        DataBox res = new DataBox()
        res.put("task", resTask)
        res.put("taskOption", resTaskOption)


        //
        return res
    }

    /**
     * Дополняет факты "богатыми" данными вопроса и ответа (звук, перевод и т.п.),
     * т.е. заполним поля question и answer.
     *
     * Расчитываем, что в stTasks есть поля factQuestion и factAnswer.
     */
    void fillFactBody(Store stTasks) {
        // Дополним факты в плане "богатыми" данными для вопроса и ответа
        Set itemIds = stTasks.getUniqueValues("item")
        Set factQuestionIds = stTasks.getUniqueValues("factQuestion")
        Set factAnswerIds = stTasks.getUniqueValues("factAnswer")

        // Факты каждого типа для списка itemIds
        Store stFactData_question = mdb.loadQuery(sqlFactData_Facts(factQuestionIds, "factQuestion"))
        Store stFactData_answer = mdb.loadQuery(sqlFactData_Facts(factAnswerIds, "factAnswer"))
        //Store stFactData_spelling = mdb.loadQuery(sqlFactData_Items(itemIds, RgmDbConst.DataType_word_spelling))
        Store stFactData_sound = mdb.loadQuery(sqlFactData_Items(itemIds, RgmDbConst.DataType_word_sound))

        // Размажем
        convertFactsToFlatRecord(stFactData_question, ["factQuestion"], stTasks, "question")
        convertFactsToFlatRecord(stFactData_answer, ["factAnswer"], stTasks, "answer")
        //convertFactsToFlatRecord(stFactData_spelling, ["item"], stTasks, "question")
        convertFactsToFlatRecord(stFactData_sound, ["item"], stTasks, "question")
    }

    /**
     * Дополняет факты "богатыми" данными вопроса и ответа (звук, перевод и т.п.),
     * т.е. заполним поля question и answer.
     *
     * Расчитываем, что в stTasks есть поля factQuestion и factAnswer.
     */
    void fillFactBodyPlan(Store stTasks, long idPlan) {
        // Факты вопроса и ответа
        Store stFactData_Question = mdb.loadQuery(sqlFactData_PlanQuestion(), [plan: idPlan])
        Store stFactData_Answer = mdb.loadQuery(sqlFactData_PlanAnswer(), [plan: idPlan])

        // Размажем факты вопроса и ответа
        convertFactsToFlatRecord(stFactData_Question, ["factQuestion"], stTasks, "question")
        convertFactsToFlatRecord(stFactData_Answer, ["factAnswer"], stTasks, "answer")

        // Дополнительные факты Item
        Store stFactData_Item = mdb.loadQuery(sqlFactData_PlanItem(), [plan: idPlan])
        convertFactsToFlatRecord(stFactData_Item, ["factQuestion"], stTasks, "question")
    }

    void fillFactBodyGame(Store stTasks, long idGame, long idUsr) {
        // Данные вопроса и ответа
        Store sqlFactData_Question = mdb.loadQuery(sqlFactData_GameTaskQuestion(), [game: idGame, usr: idUsr])
        Store sqlFactData_Answer = mdb.loadQuery(sqlFactData_GameTaskAnswer(), [game: idGame, usr: idUsr])

        // Размажем
        convertFactsToFlatRecord(sqlFactData_Question, ["task"], stTasks, "taskQuestion")
        convertFactsToFlatRecord(sqlFactData_Answer, ["task"], stTasks, "taskAnswer")
    }


    Map<Long, String> dataTypeFieldNames = [
            (RgmDbConst.DataType_word_spelling) : "valueSpelling",
            (RgmDbConst.DataType_word_translate): "valueTranslate",
            (RgmDbConst.DataType_word_sound)    : "valueSound",
            (RgmDbConst.DataType_word_picture)  : "valuePicture",
    ]

    /**
     * Запись типа Fact раскладываем в "плоскую" запись.
     * Берем пару полей recFact.dataType+recFact.value и заполняем
     * соответствующее поле (valueSound, valueTranslate, valueSpelling или valuePicture),
     * в зависимости от recFact.dataType.
     */
    void convertFactToFlatRecord(StoreRecord recFact, StoreRecord recTask) {
        long sourceDatatype = recFact.getLong("dataType")
        String destFieldName = dataTypeFieldNames.get(sourceDatatype)
        if (UtCnv.isEmpty(destFieldName)) {
            return
        }

        // Если поле уже заполнено - не заполняем
        if (!recTask.isValueNull(destFieldName)) {
            return
        }

        // Если поле "valueSpelling" уже заполнено - не заполняем "valueTranslate"
        if (sourceDatatype == RgmDbConst.DataType_word_spelling && !recTask.isValueNull("valueTranslate")) {
            return
        }

        // Если поле "valueSpelling" уже заполнено - не заполняем "valueTranslate"
        if (sourceDatatype == RgmDbConst.DataType_word_translate && !recTask.isValueNull("valueSpelling")) {
            return
        }

        //
        recTask.setValue(destFieldName, recFact.getValue("value"))
    }

    void convertFactsToFlatRecord(Store stFactData, Collection<String> keyFields, Store stDest, String fieldDest) {
        Map<Object, List<StoreRecord>> mapFacts = StoreUtils.collectGroupBy_records(stFactData, keyFields)

        //
        for (StoreRecord recDest : stDest) {
            String key = StoreUtils.concatenateFields(recDest, keyFields)

            //
            StoreRecord recTaskQuestion = mdb.createStoreRecord("Task.fields")
            recTaskQuestion.setValues(recDest.getValue(fieldDest))

            //
            List<StoreRecord> lstFact = mapFacts.get(key)
            for (StoreRecord recFact : lstFact) {
                convertFactToFlatRecord(recFact, recTaskQuestion)
            }

            //
            recDest.setValue(fieldDest, recTaskQuestion.getValues())
        }

    }


    protected StoreRecord choiceGameTask(long idGame) {
        long idUsr = getCurrentUsrId()

        // Извлечем не выданные задания
        Store stGameTask = mdb.loadQuery(sqlChoiceTask(), [
                game: idGame,
                usr : idUsr,
        ])
        // Первое выданное будет впереди, а не отвеченные с конца
        stGameTask.sort("*dtTask")


        // Запомним factQuestion последнего задания (
        long factQuestionLast = stGameTask.get(0).getLong("factQuestion")


        // Узнаем, с какой позиции начинаются не отвеченные задания
        int posTask = Integer.MAX_VALUE
        for (int pos = 0; pos < stGameTask.size(); pos++) {
            StoreRecord recGameTask = stGameTask.get(pos)
            if (recGameTask.isValueNull("dtTask")) {
                posTask = pos
                break
            }
        }

        // Все задания уже отвечены?
        if (posTask > stGameTask.size() - 1) {
            return null
        }

        // Узнаем, есль ли возможность выдать задание, где factQuestion не совпадает
        // с factQuestion последнего задания (factQuestionLast).
        // Такая возможность будет, если в наборе setFactQuestion будет
        // больше одного элемента, что говорит о том, что кроме факта factQuestionLast
        // в невыданных заданиях есть и другие factQuestion
        Set<Long> setFactQuestion = new HashSet<>()
        for (int pos = posTask; pos < stGameTask.size(); pos++) {
            StoreRecord recGameTask = stGameTask.get(pos)
            setFactQuestion.add(recGameTask.getLong("factQuestion"))
        }

        // Выберем случайное
        StoreRecord rec
        while (true) {
            int n = rnd.num(posTask, stGameTask.size() - 1)
            rec = stGameTask.get(n)
            if (rec.getLong("factQuestion") != factQuestionLast || setFactQuestion.size() <= 1) {
                break
            }
        }

        //
        return rec
    }


    protected StoreRecord getGameLastTask(long idGame) {
        long idUsr = getCurrentUsrId()

        // Извлечем выданное, но не отвеченное задание
        Store stGameTask = mdb.loadQuery(sqlGetTask(), [
                game: idGame,
                usr : idUsr,
        ])

        if (stGameTask.size() == 0) {
            return null
        }

        // Выберем последнее
        StoreRecord rec = stGameTask.get(0)

        //
        return rec
    }


    String sqlPlanFact() {
        return """ 
-- Все пары фактов в плане
select 
    PlanFact.id, 
    
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    
    Cube_UsrFact.ratingTask,
    Cube_UsrFact.ratingQuickness

from
    PlanFact
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = PlanFact.factQuestion and 
        UsrFact.factAnswer = PlanFact.factAnswer 
    )
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = PlanFact.factQuestion and 
        Cube_UsrFact.factAnswer = PlanFact.factAnswer
    )
    
where
    PlanFact.plan = :plan    
"""
    }

    String sqlPlanTask() {
        return """ 
-- Подбор всех заданий для каждой пары фактов в плане
select 
    PlanFact.id, 
    
    PlanFact.factQuestion, 
    PlanFact.factAnswer, 
    
    Task.id task,
    
    Cube_UsrFact.ratingTask,
    Cube_UsrFact.ratingQuickness

from
    PlanFact
    left join Task on (
        Task.factQuestion = PlanFact.factQuestion and 
        Task.factAnswer = PlanFact.factAnswer 
    )
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = PlanFact.factQuestion and 
        Cube_UsrFact.factAnswer = PlanFact.factAnswer
    )
    
where
    PlanFact.plan = :plan    
"""
    }


    String sqlPlanFactsStatistic() {
        return """ 
-- Пары фактов в плане
select 
    PlanFact.id,
    PlanFact.plan,
    PlanFact.factQuestion,
    PlanFact.factAnswer, 
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from
    -- Пара фактов
    PlanFact
    -- Информация пользователя о паре фактов
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = PlanFact.factQuestion and 
        UsrFact.factAnswer = PlanFact.factAnswer 
    )
    -- Статистическая информация о паре фактов
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = PlanFact.factQuestion and 
        Cube_UsrFact.factAnswer = PlanFact.factAnswer
    )
    
where
    PlanFact.plan = :plan    
"""
    }


    String sqlPlanFactStatistic_Items(Collection items) {
        String itemsStr = "0"
        if (items.size() > 0) {
            itemsStr = items.join(",")
        }

        return """ 
-- Формируем пары фактов spelling -> translate, т.е. все варианты перевода, для сущностей по списку
select 
    Item.id item,
    
    Fact_Spelling.id factQuestion,
    Fact_Spelling.dataType dataTypeSpelling,
    Fact_Spelling.value valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.dataType dataTypeTranslate,
    Fact_Translate.value valueTranslate,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    (case when PlanFact.id is null then 0 else 1 end) isInPlan,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from    
    Item
    -- Факт типа DataType_word_spelling
    join Fact Fact_Spelling on (
        Item.id = Fact_Spelling.item and
        Fact_Spelling.dataType = ${RgmDbConst.DataType_word_spelling}
    )
    -- Факт типа DataType_word_translate 
    join Fact Fact_Translate on (
        Item.id = Fact_Translate.item and
        Fact_Translate.dataType = ${RgmDbConst.DataType_word_translate}
    )
    -- Информация о наличии пары фактов в указанном плане
    left join PlanFact on (
        PlanFact.plan = :plan and
        PlanFact.factQuestion = Fact_Spelling.id and 
        PlanFact.factAnswer = Fact_Translate.id 
    )
    -- Информация пользователя о паре фактов
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = Fact_Spelling.id and 
        UsrFact.factAnswer = Fact_Translate.id 
    )
    -- Статистическая информация о паре фактов
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = Fact_Spelling.id and 
        Cube_UsrFact.factAnswer = Fact_Translate.id
    )
    
where
    Item.id in (${itemsStr})   

order by
    Item.id,
    Fact_Spelling.id,    
    Fact_Translate.id    
"""
    }

    String sqlPlanFactStatistic_Facts(Collection facts) {
        String factsStr = "0"
        if (facts.size() > 0) {
            factsStr = facts.join(",")
        }

        return """ 
-- Формируем пары фактов spelling -> translate, т.е. все варианты перевода, для сущностей по списку
select 
    Item.id item,
    
    Fact_Spelling.id factQuestion,
    Fact_Spelling.dataType dataTypeSpelling,
    Fact_Spelling.value valueSpelling,
    
    Fact_Translate.id factAnswer, 
    Fact_Translate.dataType dataTypeTranslate,
    Fact_Translate.value valueTranslate,
    
    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad,
    (case when PlanFact.id is null then 0 else 1 end) isInPlan,
    
    coalesce(Cube_UsrFact.ratingTask, 0) ratingTask,
    coalesce(Cube_UsrFact.ratingQuickness, 0) ratingQuickness

from    
    Item
    -- Факт типа DataType_word_spelling
    join Fact Fact_Spelling on (
        Item.id = Fact_Spelling.item and
        Fact_Spelling.dataType = ${RgmDbConst.DataType_word_spelling}
    )
    -- Факт типа DataType_word_translate 
    join Fact Fact_Translate on (
        Item.id = Fact_Translate.item and
        Fact_Translate.dataType = ${RgmDbConst.DataType_word_translate}
    )
    -- Информация о наличии пары фактов в указанном плане
    left join PlanFact on (
        PlanFact.plan = :plan and
        PlanFact.factQuestion = Fact_Spelling.id and 
        PlanFact.factAnswer = Fact_Translate.id 
    )
    -- Информация пользователя о паре фактов
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = Fact_Spelling.id and 
        UsrFact.factAnswer = Fact_Translate.id 
    )
    -- Статистическая информация о паре фактов
    left join Cube_UsrFact on (
        Cube_UsrFact.usr = :usr and 
        Cube_UsrFact.factQuestion = Fact_Spelling.id and 
        Cube_UsrFact.factAnswer = Fact_Translate.id
    )
    
where
    Fact_Translate.id in (${factsStr})   

order by
    Item.id,
    Fact_Spelling.id,    
    Fact_Translate.id    
"""
    }


    private String sqlUsrGameStatistic() {
        return """
select 
    Cube_UsrGame.*

from
    Cube_UsrGame
    
where
    Cube_UsrGame.usr = :usr and
    Cube_UsrGame.game = :game
"""
    }

    private String sqlGameTasksStatistic() {
        return """
select
    GameTask.*,
    
    Task.factQuestion, 
    Task.factAnswer, 

    UsrFact.isHidden,
    UsrFact.isKnownGood,
    UsrFact.isKnownBad--,

    --Cube_UsrFact.ratingTask ratingTaskNow,
    --Cube_UsrFact.ratingQuickness ratingQuicknessNow

from
    GameTask
    join Task on (
        GameTask.task = Task.id
    )
    left join UsrFact on (
        UsrFact.usr = :usr and
        UsrFact.factQuestion = Task.factQuestion and 
        UsrFact.factAnswer = Task.factAnswer 
    )
    --left join Cube_UsrFact on (
    --    Cube_UsrFact.usr = :usr and 
    --    Cube_UsrFact.factQuestion = Task.factQuestion and 
    --    Cube_UsrFact.factAnswer = Task.factAnswer
    --)

where
    GameTask.usr = :usr and
    GameTask.game = :game

order by
    GameTask.dtTask,
    GameTask.id
"""
    }


    private String sqlChoiceTask() {
        return """
select
    GameTask.*,
    Task.factQuestion,
    Task.factAnswer
from
    GameTask 
    join Task on (GameTask.task = Task.id)
where
    GameTask.game = :game and
    GameTask.usr = :usr
"""
    }

    private String sqlGameTaskRemainderCount() {
        return """
select
    count(*) cnt
from
    GameTask 
where
    game = :game and
    usr = :usr and
    -- не выданное
    dtTask is null 
"""
    }


    private String sqlGetTask() {
        return """
select
    GameTask.*
from
    GameTask 
where
    game = :game and
    usr = :usr and
    -- выданное, но ...
    dtTask is not null and 
    -- ... но неотвеченные
    dtAnswer is null 
order by
    dtTask desc 
"""
    }


    private String sqlGameTask() {
        return """
select 
    * 
from 
    GameTask
where
    GameTask.id = :id and 
    GameTask.usr = :usr 
"""
    }


    private String sqlTask() {
        return """
select 
    Task.* 
from 
    Task
where
    Task.id = :id 
"""
    }


    String sqlOtherTask() {
        """
select 
    GameTask.*
from
    GameTask
    join Task on (GameTask.task = Task.id)
where
    GameTask.usr = :usr and
    GameTask.game = :game and
    --GameTask.dtTask is null and
    Task.factQuestion = :factQuestion and
    Task.factAnswer = :factAnswer
"""
    }


    private String sqlLastGame() {
        return """
select
    Game.*
from
    Game
    join GameUsr on (Game.id = GameUsr.game)
where
    GameUsr.usr = :usr
order by    
    Game.dbeg desc,
    Game.id desc
limit 1
"""
    }


    private String sqlActiveGames() {
        return """
select
    Game.*
from
    Game
    join GameUsr on (Game.id = GameUsr.game)
where
    Game.dend is null and
    GameUsr.usr = :usr
order by    
    Game.dbeg desc,
    Game.id
"""
    }

    private String sqlGameStatistic() {
        return """
select
    Game.*,
    Plan.text planText,
    
    Cube_UsrPlan.count wordCount,
    Cube_UsrPlan.countFull wordCountFull,
    Cube_UsrPlan.countLearned wordCountLearned,
    Cube_UsrPlan.ratingTask,
    Cube_UsrPlan.ratingQuickness

from
    Game
    join Plan on (Game.plan = Plan.id)
    join GameUsr on (
        Game.id = GameUsr.game and
        GameUsr.usr = :usr 
    )
    left join Cube_UsrPlan on (
        Cube_UsrPlan.usr = :usr and 
        Cube_UsrPlan.plan = Plan.id 
    )

where
    Game.id = :game
"""
    }

    private String sqlCloseGame() {
        return """
update
    Game
set 
    dend = :dt
where
    Game.dend is null and
    Game.id = :game
"""
    }


}
