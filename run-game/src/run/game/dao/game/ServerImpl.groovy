package run.game.dao.game

import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.commons.process.*
import jandcode.commons.rnd.*
import jandcode.commons.rnd.impl.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*
import run.game.dao.backstage.*
import run.game.testdata.fixture.*
import run.game.util.*

public class ServerImpl extends RgmMdbUtils implements Server {

    private long MAX_TASK_FOR_GAME = 10
    private double RATING_DECREASE_FOR_STARRED = 0.25

    private Rnd rnd = new RndImpl()

    // todo: не сюда этот метод
    @DaoMethod
    public List parseStill(String imgBase64) {
        long idUsr = getCurrentUserId()

        //
        int pos = imgBase64.indexOf(",") + 1
        String imgStr = imgBase64.substring(pos)
        byte[] img = UtString.decodeBase64(imgStr)

        //
        File inFile = File.createTempFile("rgm", ".png")
        FileOutputStream strm = new FileOutputStream(inFile);
        strm.write(img)
        strm.close()

        //
        String text = tesseract(inFile.getAbsolutePath(), "eng+rus")

        //
        //inFile.delete()

        // Частота встречаемости eng
        String dirBase = "data/web-grab/"
        Map<String, Integer> wordFrequencyMap_eng = ItemFact_fb.loadWordFrequencyMap(dirBase + "eng_top-50000.txt")

        List res = new ArrayList()

        //////////////////////////
        res.add(text)
        res.add("\r\n")
        res.add("===================")
        res.add("\r\n")
        //////////////////////////

        //
        String[] textArr = text.split(" ")
        for (String word : textArr) {
            word = word.toLowerCase().trim()
            if (word.length() > 1 && wordFrequencyMap_eng.containsKey(word)) {
                res.add(word)
            }
        }

        //
        return res
    }

    String tesseract(String inFileName, String lang) {
        String outFileName = UtFile.removeExt(inFileName)

        //
        String exeFile = "tesseract ${inFileName} ${outFileName} -l ${lang} --tessdata-dir /usr/local/share/tessdata/"

        //
        RunCmd runCmd = new RunCmd()
        runCmd.setShowout(false)
        runCmd.setSaveout(true)
        runCmd.setCmd(exeFile)
        runCmd.run()
        if (runCmd.getExitCode() > 0) {
            String r = UtString.join(runCmd.getOut(), "\n")
            throw new XError("error in {0}:\n{1}", exeFile, r)
        }

        //
        outFileName = outFileName + ".txt"
        String text = UtFile.loadString(outFileName)

        //
        //new File(outFileName).delete()

        //
        return text
    }


    @DaoMethod
    public DataBox getGame(long idGame) {
        long idUsr = getCurrentUserId()

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public DataBox getLastGame() {
        long idUsr = getCurrentUserId()
        StoreRecord recGame = mdb.loadQueryRecord(sqlLastGame(), [usr: idUsr], false)

        //
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
        long idUsr = getCurrentUserId()
        Store stGames = mdb.loadQuery(sqlActiveGames(), [usr: idUsr])

        //
        if (stGames.size() == 0) {
            return null
        }

        //
        StoreRecord recGame = stGames.get(0)
        long idGame = recGame.getLong("id")

        //
        return loadAndPrepareGame(idGame, idUsr)
    }

    @DaoMethod
    public void closeActiveGame() {
        long idUsr = getCurrentUserId()
        XDateTime dt = XDateTime.now()
        mdb.execQuery(sqlCloseActiveGame(), [usr: idUsr, dt: dt])
    }


    @DaoMethod
    public DataBox gameStart(long idPlan) {
        // Добавляем Game
        StoreRecord recGame = mdb.createStoreRecord("Game")
        XDateTime dt = XDateTime.now()
        recGame.setValue("dbeg", dt)
        recGame.setValue("plan", idPlan)

        //
        long idGame = mdb.insertRec("Game", recGame)


        // Добавим участника игры
        long idUsr = getCurrentUserId()
        mdb.insertRec("GameUsr", [usr: idUsr, game: idGame])


        // Отберем подходящие задания на игру.
        // Задание выбирается с учетом статистики пользователя.

        // Задания в плане
        Store stPlanTasks = mdb.loadQuery(sqlPlanTasks(), [plan: idPlan, usr: idUsr])

        // Слегка рандомизируем рейтинг -
        // иначе для для заданий без рейтинга (например, которые никогда не выдавали)
        // рейтинг перестает быть хорошим выбором порядка - получается список идущих подряд
        // одних тех же слов (потому, что задания сортируются по номеру факта,
        // а для одного факта несколько заданий)
        for (StoreRecord recTask : stPlanTasks) {
            double progressSeed = rnd.num(-1000, 1000) / 10000
            recTask.setValue("ratingTask", recTask.getDouble("ratingTask") + progressSeed)
        }

        // Откорректируем рейтинг - от помеченных как "любимые" отнимем немного баллов,
        // чтобы они с большей вероятностью выпадали
        for (StoreRecord recTask : stPlanTasks) {
            // Задание помечено как "starred"?
            if (recTask.getBoolean("starred")) {
                recTask.setValue("rating", recTask.getDouble("rating") - RATING_DECREASE_FOR_STARRED)
            }
        }

        // Сортируем задания по рейтингу
        stPlanTasks.sort("ratingTask,ratingQuickness")

        // Теперь выберем задания на игру
        long taskForGameCount = 0
        for (StoreRecord recTask : stPlanTasks) {
            // Скрытые задания (помечено как "hidden") не выдаем
            if (recTask.getBoolean("hidden")) {
                continue
            }

            //
            mdb.insertRec("GameTask", [
                    game: idGame,
                    usr : idUsr,
                    task: recTask.getLong("task"),
            ])

            //
            taskForGameCount = taskForGameCount + 1
            if (taskForGameCount >= MAX_TASK_FOR_GAME) {
                break
            }
        }

        //
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
        // Выбираем последнее выданное задание
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
        long idUsr = getCurrentUserId()
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
        // Выбираем, что осталось спросить по плану
        long idGame = recGameTask.getLong("game")
        StoreRecord recGameTaskNext = choiceGameTask(idGame)

        // Закрываем игру, если нечего
        if (recGameTaskNext == null) {
            closeActiveGame()
        }
    }


    /**
     * Список планов (уровней) пользователя.
     * С рейтингом, сортированный по некоторому критерию, например по самому отстающему
     * или по запланированному учителем.
     */
    @DaoMethod
    Store getPlansUsr() {
        Store res = mdb.createStore("Plan.list.statistic")

        long idUsr = getCurrentUserId()

        //
        mdb.loadQuery(res, sqlPlansUsr(), [usr: idUsr])

/*
        //
        StatisticManager statisticManager = mdb.create(StatisticManagerImpl)
        Store st = statisticManager.getPlansStatistic()

        //
        st.copyTo(res)
        for (StoreRecord rec : res) {
            rec.setValue("tasksStatistic", UtJson.fromJson(rec.getString("tasksStatistic")))
        }

        //
        res.sort("progress")
*/

        //
        return res
    }


    /**
     * Список общедоступных планов (уровней),
     * не еще не добавленных к списку планов пользователя.
     */
    @DaoMethod
    Store getPlansPublic() {
        Store res = mdb.createStore("Plan.list")

        //
        long idUsr = getCurrentUserId()

        //
        mdb.loadQuery(res, sqlPlansPublic(), [usr: idUsr])

        //
        return res
    }


    /**
     * Добавить план к списку планов пользователя.
     */
    @DaoMethod
    void addPlanUsr(long idPlan) {
        long idUsr = getCurrentUserId()
        StoreRecord rec = mdb.loadQueryRecord(sqlPlanUsr(), [usr: idUsr, plan: idPlan], false)

        //
        if (rec != null) {
            throw new XError("План уже добавлен к списку")
        }

        //
        mdb.insertRec("PlanUsr", [plan: idPlan, usr: idUsr])
    }


    /**
     * Исключить план из списка планов пользователя.
     */
    @DaoMethod
    void delPlan(long idPlan) {
        long idUsr = getCurrentUserId()
        StoreRecord rec = mdb.loadQueryRecord(sqlPlanUsr(), [usr: idUsr, plan: idPlan], false)

        if (rec == null) {
            throw new XError("План не был добавлен к списку")
        }

        //
        mdb.deleteRec("PlanUsr", rec.getLong("id"))
    }


    @DaoMethod
    DataBox getPlanTaskStatistic(long idPlan) {
        DataBox res = new DataBox()

        //
        long idUsr = getCurrentUserId()

        // План
        StoreRecord recPlan = loadPlanServerRecInternal(idPlan)

        // Задания в плане - список
        Store stPlanTasks = mdb.createStore("PlanTask.list.statistic")
        mdb.loadQuery(stPlanTasks, sqlPlanTasks(), [plan: idPlan, usr: idUsr])


        // Задания плана - данные вопроса и ответа
        Store stTaskQuestion = mdb.loadQuery(sqlPlanTaskQuestion(), [plan: idPlan])
        Store stTaskAnswer = mdb.loadQuery(sqlPlanTaskAnswer(), [plan: idPlan])

        // Дополним задания плана данными вопроса и ответа
        fillTaskBody(stPlanTasks, stTaskQuestion, stTaskAnswer)


        // По каждому заданию плана - последние баллы
        StatisticManager1 statisticManager = mdb.create(StatisticManager1)
        // Период заданий - за несколько дней
        XDateTime dend = XDateTime.now()
        XDateTime dbeg = dend.addDays(-7)
        // Статистика по заданиям
        Store stTasksStatistic = statisticManager.getStatisticForPlanInternal(idPlan, idUsr, dbeg, dend)

        // Дополним задания игры статистикой
        StoreUtils.join(stPlanTasks, stTasksStatistic, "task", [
                "rating",
                "ratingQuickness"
        ])

        // По всем заданиям игры - сумма баллов и т.д.
        Map statisticAggretated = statisticManager.aggregateStatistic(stTasksStatistic)

        // todo: максимальный балл надо брать из запроса к плану и возвращать в recPlan (а точнее - из будущего куба). Пока так
        long countHidden = StoreUtils.getCount(stPlanTasks, "hidden", true)
        statisticAggretated.put("ratingMax", stPlanTasks.size() - countHidden)


        //
        res.put("plan", recPlan)
        res.put("planTasks", stPlanTasks)
        res.put("statistic", statisticAggretated)

        //
        return res
    }


    @DaoMethod
    void saveTaskUsr(long idTask, Map taskUsr) {
        Task_upd upd = mdb.create(Task_upd)
        upd.saveTaskUsr(idTask, taskUsr)
    }

    DataBox loadAndPrepareGame_Short(long idGame, long idUsr) {
        DataBox res = new DataBox()

        // Данные по игре
        StoreRecord recGame = loadGameServerRecInternal(idGame, idUsr)
        res.put("game", recGame)

        // Задания игры - список + результат ответа на каждое задание
        Store stGameTasksResult = mdb.createStore("GameTask.list")
        mdb.loadQuery(stGameTasksResult, sqlGameTasks(), [game: idGame, usr: idUsr])
        res.put("tasksResult", stGameTasksResult)

        //
        return res
    }


    DataBox loadAndPrepareGame(long idGame, long idUsr) {
        DataBox res = new DataBox()

        //
        StoreRecord recGame = loadGameServerRecInternal(idGame, idUsr)
        if (recGame == null) {
            return null
        }


        // Задания игры - список + результат ответа на каждое задание
        Store stGameTasks = mdb.createStore("GameTask.list.statistic")
        mdb.loadQuery(stGameTasks, sqlGameTasks(), [game: idGame, usr: idUsr])


        // Задания игры - данные вопроса и ответа
        long idPlan = recGame.getLong("plan")
        Store stTaskQuestion = mdb.loadQuery(sqlPlanTaskQuestion(), [plan: idPlan])
        Store stTaskAnswer = mdb.loadQuery(sqlPlanTaskAnswer(), [plan: idPlan])

        // Дополним задания игры данными вопроса и ответа
        fillTaskBody(stGameTasks, stTaskQuestion, stTaskAnswer)


        // По каждому заданию игры - баллы по текущей и прошлой игре, заработанные и проигранные баллы
        StatisticManager1 statisticManager = mdb.create(StatisticManager1)
        Store stTasksStatistic = statisticManager.compareStatisticForGamePrior(idGame)

        // Дополним задания игры статистикой
        StoreUtils.join(stGameTasks, stTasksStatistic, "task", [
                rating0  : "rating",
                ratingInc: "ratingInc",
                ratingDec: "ratingDec"
        ], false)


        // По всем заданиям игры - сумма баллов и т.д.
        Map statisticAggretated = statisticManager.aggregateStatistic0(stTasksStatistic)

        // todo: максимальный балл надо брать из запроса к плану и возвращать в recPlan (а точнее - из будущего куба). Пока так
        long countHidden = StoreUtils.getCount(stGameTasks, "hidden", true)
        statisticAggretated.put("ratingMax", stGameTasks.size() - countHidden)


        //
        res.put("game", recGame)
        res.put("gameTasks", stGameTasks)
        res.put("statistic", statisticAggretated)

        //
        return res
    }


    StoreRecord loadPlanServerRecInternal(long idPlan) {
        StoreRecord recPlan = mdb.createStoreRecord("Plan.server")
        //
        mdb.loadQueryRecord(recPlan, sqlPlan(), [plan: idPlan])
        if (recPlan.getLong("id") == 0) {
            return null
        }
        //
        return recPlan
    }

    StoreRecord loadGameServerRecInternal(long idGame, long idUsr) {
        StoreRecord recGame = mdb.createStoreRecord("Game.server")
        //
        mdb.loadQueryRecord(recGame, sqlGame(), [game: idGame, usr: idUsr])
        if (recGame.getLong("id") == 0) {
            return null
        }
        //
        return recGame
    }


    String sqlPlanTaskQuestion() {
        return """
select 
    PlanTask.*,
    TaskQuestion.dataType,
    TaskQuestion.value

from 
    PlanTask
    join TaskQuestion on (
        TaskQuestion.task = PlanTask.task
    )

where
    PlanTask.plan = :plan 
"""
    }


    String sqlPlanTaskAnswer() {
        return """
select 
    PlanTask.*,
    TaskOption.dataType,
    TaskOption.value

from 
    PlanTask
    join TaskOption on (
        TaskOption.task = PlanTask.task and 
        TaskOption.isTrue = 1 
    )

where
    PlanTask.plan = :plan 
"""
    }


    protected DataBox loadAndPrepareGameTask(long idGame, StoreRecord recGameTask) {
        DataBox res = new DataBox()

        //
        long idUsr = getCurrentUserId()


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
     * Преобразуем задание по требованиям frontend-api
     * Превращает списки stTaskQuestion и stTaskOption в пару плоских записей,
     * где имеем valueText, valueSound, valueImage и т.д.
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
        Store stTaskQuestion = task.get("taskQuestion")
        for (StoreRecord recTaskQuestion : stTaskQuestion) {
            convert(recTaskQuestion, resTask)
        }

        // Варианты ответа
        Store stTaskOption = task.get("taskOption")
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recTaskOption = resTaskOption.add()
            recTaskOption.setValue("id", rec.getValue("id"))
            recTaskOption.setValue("isTrue", rec.getValue("isTrue"))
            convert(rec, recTaskOption)
        }


        // ---
        DataBox res = new DataBox()
        res.put("task", resTask)
        res.put("taskOption", resTaskOption)


        //
        return res
    }


    void convert(StoreRecord recTaskSource, StoreRecord resTask) {
        //resTask.setValue("dataType", recTaskSource.getValue("dataType"))

        // Звук
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_sound) {
            resTask.setValue("valueSound", recTaskSource.getValue("value"))
        }
        // Текст
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_translate) {
            resTask.setValue("valueTranslate", recTaskSource.getValue("value"))
        }
        // Текст
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_spelling) {
            resTask.setValue("valueSpelling", recTaskSource.getValue("value"))
        }
        // Картинка
        if (recTaskSource.getLong("dataType") == RgmDbConst.DataType_word_picture) {
            resTask.setValue("valuePicture", recTaskSource.getValue("value"))
        }
    }

    /**
     * Дополним задания игры данными вопроса и ответа,
     * т.е. заполним поля question и answer
     */
    void fillTaskBody(Store stGameTasks, Store stTaskQuestion, Store stTaskAnswer) {
        Map<Object, List<StoreRecord>> mapTaskQuestion = StoreUtils.collectGroupBy_records(stTaskQuestion, "task")
        Map<Object, List<StoreRecord>> mapTaskAnswer = StoreUtils.collectGroupBy_records(stTaskAnswer, "task")
        //
        for (StoreRecord recGameTask : stGameTasks) {
            StoreRecord recQuestion = mdb.createStoreRecord("Task.fields")
            List<StoreRecord> lstTaskQuestion = mapTaskQuestion.get(recGameTask.getLong("task"))
            for (StoreRecord recTaskQuestion : lstTaskQuestion) {
                convert(recTaskQuestion, recQuestion)
            }
            recGameTask.setValue("question", recQuestion.getValues())

            //
            StoreRecord recAnswer = mdb.createStoreRecord("Task.fields")
            List<StoreRecord> lstTaskAnswer = mapTaskAnswer.get(recGameTask.getLong("task"))
            for (StoreRecord recTaskAnswer : lstTaskAnswer) {
                convert(recTaskAnswer, recAnswer)
            }
            recGameTask.setValue("answer", recAnswer.getValues())
        }
    }


    protected StoreRecord choiceGameTask(long idGame) {
        long idUsr = getCurrentUserId()

        // Извлечем не выданные задания
        Store stGameTask = mdb.loadQuery(sqlChoiceTask(), [
                game: idGame,
                usr : idUsr,
        ])

        if (stGameTask.size() == 0) {
            return null
        }

        // Выберем случайное
        int n = rnd.num(0, stGameTask.size() - 1)
        StoreRecord rec = stGameTask.get(n)

        //
        return rec
    }


    protected StoreRecord getGameLastTask(long idGame) {
        long idUsr = getCurrentUserId()

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


    private String sqlChoiceTask() {
        return """
select
    GameTask.*
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


    private String sqlGameTaskOption() {
        return """
select
    TaskOption.*
from
    GameTask
    join Task on (GameTask.task = Task.id)
    join TaskOption on (Task.id = TaskOption.task)
where
    GameTask.id = :id and
    TaskOption.id = :taskOption
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

    private String sqlPlanTasks() {
        return """
select
    PlanTask.*,
    TaskUsr.hidden,
    TaskUsr.starred,
    Cube_UsrFact.ratingTask,
    Cube_UsrFact.ratingQuickness

from
    PlanTask
    join Task on (PlanTask.task = Task.id)
    left join TaskUsr on (
        PlanTask.id = TaskUsr.task and 
        TaskUsr.usr = :usr
    )
    left join Cube_UsrFact on (
        Cube_UsrFact.factQuestion = Task.factQuestion and
        Cube_UsrFact.factAnswer = Task.factAnswer and
        Cube_UsrFact.usr = :usr
    )

where
    PlanTask.plan = :plan
"""
    }

    private String sqlGameTasks() {
        return """
select
    GameTask.task,

    GameTask.dtTask,
    GameTask.dtAnswer,
    GameTask.wasTrue,
    GameTask.wasFalse,
    GameTask.wasHint,
    GameTask.wasSkip

from
    GameTask
    left join TaskUsr on (GameTask.task = TaskUsr.task and TaskUsr.usr = :usr)

where
    GameTask.usr = :usr and
    GameTask.game = :game

order by
    GameTask.dtTask,
    GameTask.id
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
    Game.dbeg,
    Game.id
"""
    }

    private String sqlPlansUsr() {
        return """
with Tab_UsrPlanStatistic as (

select 
    PlanTask.plan,
    count(*) count,
    sum(case when TaskUsr.hidden = 1 then 0 else 1 end) countFull
  
from
    PlanTask
    left join TaskUsr on (PlanTask.id = TaskUsr.task and TaskUsr.usr = :usr)
  
group by
    PlanTask.plan 
)


select
    Plan.id,
    Plan.id plan,
    Plan.text planText,
    --PlanTag.tag public,
    (case when PlanTag.tag is null then 0 else 1 end) isPublic,
    (case when PlanUsr.usr is null then 0 else 1 end) isUsr,
    Tab_UsrPlanStatistic.count,
    Tab_UsrPlanStatistic.countFull
    
from
    Plan
    left join PlanUsr on (Plan.id = PlanUsr.plan and PlanUsr.usr = :usr)
    left join PlanTag on (Plan.id = PlanTag.plan and PlanTag.tag = ${RgmDbConst.Tag_access_public})
    join Tab_UsrPlanStatistic on (Plan.id = Tab_UsrPlanStatistic.plan)

where
    PlanUsr.usr = :usr
"""
    }

    private String sqlPlansPublic() {
        return """
with Tab_UsrPlanStatistic as (

select 
    PlanTask.plan,
    count(*) count
  
from
    PlanTask
  
group by
    PlanTask.plan 
)


select
    Plan.*,
    (case when PlanTag.tag is null then 0 else 1 end) isPublic,
    (case when PlanUsr.usr is null then 0 else 1 end) isUsr,
    Tab_UsrPlanStatistic.count
    
from
    Plan
    left join PlanUsr on (Plan.id = PlanUsr.plan and PlanUsr.usr = :usr)
    join PlanTag on (Plan.id = PlanTag.plan and PlanTag.tag = ${RgmDbConst.Tag_access_public})
    join Tab_UsrPlanStatistic on (Plan.id = Tab_UsrPlanStatistic.plan)

where
    PlanUsr.usr is null
"""
    }

    private String sqlPlanUsr() {
        return """
select
    PlanUsr.*
from
    PlanUsr
where
    PlanUsr.plan = :plan and
    PlanUsr.usr = :usr
"""
    }

    private String sqlPlan() {
        return """
select
    Plan.id,
    Plan.id plan,
    Plan.text planText
from
    Plan
where
    Plan.id = :plan
"""
    }

    private String sqlGame() {
        return """
select
    Game.*,
    Plan.text planText
from
    Game
    join Plan on (Game.plan = Plan.id)
    join GameUsr on (Game.id = GameUsr.game)
where
    Game.id = :game and
    GameUsr.usr = :usr
"""
    }

    private String sqlCloseActiveGame() {
        return """
update
    Game
set 
    dend = :dt
where
    Game.dend is null and
    Game.id in (
        select
            GameUsr.game 
        from 
            GameUsr
        where
            GameUsr.usr = :usr
    )
"""
    }


}
