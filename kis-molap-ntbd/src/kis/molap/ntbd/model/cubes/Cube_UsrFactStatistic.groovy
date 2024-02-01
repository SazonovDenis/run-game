package kis.molap.ntbd.model.cubes

import groovy.transform.*
import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.commons.variant.*
import jandcode.core.store.*
import kis.molap.model.coord.*
import kis.molap.model.cube.*
import kis.molap.model.cube.impl.*
import kis.molap.model.service.*
import kis.molap.model.value.*

/**
 * Зависимости только от GameTask, т.к. предполагаем, что
 *  - состав задания Task+TaskOption+TaskQuestion - никогда не меняется.
 *
 * Для отслеживания изменений в статистике для пары вопрос+ответ для пользователя
 * достаточно отследить только добавления в GameTask.
 */
@CompileStatic
public class Cube_UsrFactStatistic extends CubeCustom implements ICalcData {


    public CoordList getDirtyCoords(long auditAgeFrom, long auditAgeTo) throws Exception {
        CoordList coords = CoordList.create()

        //
        String table = "GameTask"
        List<IVariantMap> audit = auditManager.loadAudit(table, auditAgeFrom, auditAgeTo)

        //
        Map<String, StoreIndex> cacheTaskByGameTask = new HashMap<>()
        //
        for (Map<String, Object> dataMap : audit) {
            long usr = UtCnv.toLong(dataMap.get("usr"))
            long game = UtCnv.toLong(dataMap.get("game"))
            long task = UtCnv.toLong(dataMap.get("task"))

            StoreRecord recTask = getTaskByGameTask(usr, game, task, cacheTaskByGameTask)

            Coord coord = Coord.create()
            coord.put("usr", usr)
            coord.put("factQuestion", recTask.getLong("factQuestion"))
            coord.put("factAnswer", recTask.getLong("factAnswer"))
            coords.add(coord)
        }

        //
        return coords
    }

    public void calc(Iterable<Coord> coords, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res) throws Exception {
        // Период анализа ответов - за некоторое количество дней от начала
        XDateTime dend = XDateTime.today().addDays(1)
        XDateTime dbeg = dend.addDays(-10)
        Map params = UtCnv.toMap(
                "dbeg", dbeg,
                "dend", dend
        )

        //
        if (coords == null) {
            CubeService cubeService = mdb.getModel().bean(CubeService.class)
            Space space = cubeService.createSpace(cubeInfo.getSpace(), mdb)
            coords = space.getCoordsForInterval(intervalDbeg, intervalDend)
        }

        //
        if (coords instanceof Collection) {
            logCube.logStepStart(coords.size())
        } else if (coords instanceof Map) {
            logCube.logStepStart(coords.size())
        } else {
            logCube.logStepStart()
        }

        //
        UtCubeRating utCubeRating = new UtCubeRating()

        //
        for (Coord coord : coords) {
            long usr = UtCnv.toLong(coord.get("usr"))
            long factQuestion = UtCnv.toLong(coord.get("factQuestion"))
            long factAnswer = UtCnv.toLong(coord.get("factAnswer"))

            //
            params.put("usr", usr)
            params.put("factQuestion", factQuestion)
            params.put("factAnswer", factAnswer)
            Store stGameTask = mdb.loadQuery(sqlGameTaskByFact(), params)
            //mdb.outTable(stGameTask)

            int pos = 0
            while (pos < stGameTask.size()) {
                Map resMap = new HashMap()
                pos = utCubeRating.stepCollectRaiting(stGameTask, pos, resMap)

                // ---
                // Возвращаем calcResult
                CalcResult calcResult = new CalcResult()

                //
                Coord newCoord = Coord.create()
                newCoord.put("usr", usr)
                newCoord.put("factQuestion", factQuestion)
                newCoord.put("factAnswer", factAnswer)
                calcResult.coord = newCoord

                //
                ValueSingle valueSingle = ValueSingle.create()
                valueSingle.put("ratingTask", resMap.get("ratingTask"))
                valueSingle.put("ratingQuickness", resMap.get("ratingQuickness"))
                calcResult.value = valueSingle

                //
                res.addValue(calcResult)


                // ---
                logCube.logStepStep()
            }

/*
            //
            List<Boolean> taskAnswers = []
            List<Double> taskAnswersDuration = []
            //
            for (int n = 0; n < stGameTask.size(); n++) {
                // Текущая и следующая запись
                StoreRecord recGameTask = stGameTask.get(n)
                StoreRecord recGameTaskNext = null
                if (n < stGameTask.size() - 1) {
                    recGameTaskNext = stGameTask.get(n + 1)
                }

                // Накопление rating по очередному GameTask
                if (taskAnswers.size() < ratingWeight.size()) {
                    if (recGameTask.getLong("wasTrue")) {
                        taskAnswers.add(true)
                    } else {
                        taskAnswers.add(false)
                    }
                    //
                    Double duration = null
                    if (!UtDateTime.isEmpty(recGameTask.getDateTime("dtAnswer"))) {
                        double durationMsec = recGameTask.getDateTime("dtAnswer").diffMSec(recGameTask.getDateTime("dtTask"))
                        duration = durationMsec / 1000
                    }
                    taskAnswersDuration.add(duration)
                }

                // Накопление по очередному task закончено
                if (recGameTaskNext == null ||
                        recGameTaskNext.getLong("factQuestion") != recGameTask.getLong("factQuestion") ||
                        recGameTaskNext.getLong("factAnswer") != recGameTask.getLong("factAnswer")
                ) {
                    // ---
                    // Считаем рейтинг

                    // Если не хватает последних ответов (мало раз выполнено task) -
                    // считаем, что предыдущие были отвечены неправильно
                    while (taskAnswers.size() < ratingWeight.size()) {
                        taskAnswers.add(false)
                        taskAnswersDuration.add(null)
                    }

                    //
                    double ratingTask = 0
                    for (int i = 0; i < ratingWeight.size(); i++) {
                        double ratingAnswer = getRatingAnswer(taskAnswers.get(i))
                        ratingTask = ratingTask + ratingAnswer * ratingWeight[i]
                    }
                    //
                    double ratingQuickness = 0
                    for (int i = 0; i < ratingWeight.size(); i++) {
                        double ratingQuicknessAnswer = getRatingQuickness(taskAnswersDuration.get(i))
                        ratingQuickness = ratingQuickness + ratingQuicknessAnswer * ratingWeight[i]
                    }

                    //
                    taskAnswers = []
                    taskAnswersDuration = []


                    // ---
                    // Возвращаем calcResult
                    CalcResult calcResult = new CalcResult()

                    //
                    Coord newCoord = Coord.create()
                    newCoord.put("usr", usr)
                    newCoord.put("factQuestion", factQuestion)
                    newCoord.put("factAnswer", factAnswer)
                    calcResult.coord = newCoord

                    //
                    ValueSingle valueSingle = ValueSingle.create()
                    valueSingle.put("ratingTask", ratingTask)
                    valueSingle.put("ratingQuickness", ratingQuickness)
                    calcResult.value = valueSingle

                    //
                    res.addValue(calcResult)

                    //
                    logCube.logStepStep()
                }
            }
*/

        }
    }

    public void convertCoords(String sourceCubeName, CoordList coords, CoordList coordsRes) throws Exception {
    }


    StoreRecord getTaskByGameTask(long usr, long game, long task, HashMap<String, StoreIndex> cacheTaskByGame) {
        String key = usr + "_" + game
        if (!cacheTaskByGame.containsKey(key)) {
            Store stTasks = mdb.loadQuery(sqlGameTaskByGame(), [usr: usr, game: game])
            cacheTaskByGame.put(key, stTasks.getIndex("id"))
        }

        //
        StoreRecord recTask = cacheTaskByGame.get(key).get(task)

        // Защита от дурака
        if (recTask == null) {
            throw new XError("getTaskByGame: recTask == null")
        }

        //
        return recTask
    }

    String sqlGameTaskByGame() {
        """
select
    Task.* 
from 
    GameTask 
    join Task on (GameTask.task = Task.id)
where
    GameTask.usr = :usr and
    GameTask.game = :game
"""
    }


    String sqlGameTaskByFact() {
        """
select
    GameTask.*,
    Task.factQuestion,
    Task.factAnswer

from
    GameTask
    join Task on (GameTask.task = Task.id)

where
    GameTask.usr = :usr and
    GameTask.dtTask >= :dbeg and
    GameTask.dtTask < :dend and
    Task.factQuestion = :factQuestion and
    Task.factAnswer = :factAnswer

order by
    Task.factQuestion,
    Task.factAnswer,
    GameTask.dtTask desc,
    GameTask.id desc

"""
    }

}
