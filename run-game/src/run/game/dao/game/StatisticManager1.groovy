package run.game.dao.game

import groovy.transform.*
import jandcode.commons.*
import jandcode.commons.datetime.*
import jandcode.commons.error.*
import jandcode.core.dao.*
import jandcode.core.store.*
import kis.molap.ntbd.model.*
import kis.molap.ntbd.model.cubes.*
import run.game.dao.*
import run.game.util.*

@TypeChecked
class StatisticManager1 extends RgmMdbUtils {

    // Расчет рейтингов: вес результата текущего, предыдущего и пред-предыдущего ответа
    protected double[] ratingWeight = [0.5, 0.3, 0.2]

    // Расчет рейтинга за скорость: время ответа и баллы за него
    protected double[] ratingDurationGrade = [2, 3, 5, 8]
    protected double[] ratingDurationWeight = [1, 0.8, 0.5, 0.2]


    @DaoMethod
    public Store getStatisticForGame(long idGame) {
        // Игра
        long idUsr = getCurrentUserId()
        StoreRecord recGame = loadRecGame(idGame, idUsr)

        // План
        long idPlan = recGame.getLong("plan")

        // Период заданий для плана
        XDateTime dend = recGame.getDateTime("dend")
        XDateTime dbeg = recGame.getDateTime("dbeg")
        // Период заданий - за сутки от начала
        dbeg = dbeg.addDays(-1000)
        // Для неазаконченной игры dend
        if (UtDateTime.isEmpty(dend)) {
            dend = UtDateTime.EMPTY_DATETIME_END
        }

        // Все задания, выданные пользователю по плану за период
        Store stTaskStatistic = getStatisticForPlanInternal(idPlan, idUsr, dbeg, dend)

        //
        return stTaskStatistic
    }

    /**
     * Разница в рейтинге между текущей и предыдущей игрой (по этому плану)
     * @param idGame текущая игра
     */
    @DaoMethod
    public Store compareStatisticForGamePrior(long idGame) {
        // Предыдущая игра
        long idUsr = getCurrentUserId()
        StoreRecord recGamePrior = loadRecGamePrior(idGame, idUsr)
        long idGamePrior = 0
        if (recGamePrior) {
            idGamePrior = recGamePrior.getLong("id")
        }

        //
        return compareStatisticForGames(idGame, idGamePrior)
    }


    /**
     * Все задания, выданные пользователю по плану за период
     */
    Store getStatisticForPlanInternal(long idPlan, long idUsr, XDateTime dbeg, XDateTime dend) {
        Map params = [dbeg: dbeg, dend: dend, "usr": idUsr, plan: idPlan]
        Store stGameTask = mdb.loadQuery(sqlGameTask(), params)
        mdb.outTable(stGameTask)

        // Сгруппируем по task
        Store stTaskStatistic = mdb.createStore("GameTask.statistic")

        //
        UtCubeRating utCubeRating = new UtCubeRating()

        //
        int pos = 0
        while (pos < stGameTask.size()) {
            Map resMap = new HashMap()
            pos = utCubeRating.stepCollectRaiting(stGameTask, pos, resMap)

            //
            StoreRecord recPlanTaskStatistic = stTaskStatistic.add()
            recPlanTaskStatistic.setValue("task", resMap.get("task"))
            recPlanTaskStatistic.setValue("rating", resMap.get("rating"))
            recPlanTaskStatistic.setValue("ratingQuickness", resMap.get("ratingQuickness"))
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
                    recGameTaskNext.getLong("task") != recGameTask.getLong("task")
            ) {
                // Если не хватает последних rating (мало раз выполнено task) -
                // считаем, что предыдущие были отвечены неправильно
                while (taskAnswers.size() < ratingWeight.size()) {
                    taskAnswers.add(false)
                    taskAnswersDuration.add(null)
                }

                //
                double rating = 0
                for (int i = 0; i < ratingWeight.size(); i++) {
                    double ratingAnswer = getRatingAnswer(taskAnswers.get(i))
                    rating = rating + ratingAnswer * ratingWeight[i]
                }
                //
                double ratingQuickness = 0
                for (int i = 0; i < ratingWeight.size(); i++) {
                    double ratingQuicknessAnswer = getRatingQuickness(taskAnswersDuration.get(i))
                    ratingQuickness = ratingQuickness + ratingQuicknessAnswer * ratingWeight[i]
                }

                //
                StoreRecord recPlanTaskStatistic = stTaskStatistic.add()
                recPlanTaskStatistic.setValue("task", recGameTask.getValue("task"))
                recPlanTaskStatistic.setValue("rating", rating)
                recPlanTaskStatistic.setValue("ratingQuickness", ratingQuickness)

                //
                taskAnswers = []
                taskAnswersDuration = []
            }
        }
*/


        //
        return stTaskStatistic
    }

    /**
     * Разница в рейтинге между играми
     * @param idGame0 текущая игра
     * @param idGame1 предыдущая игра
     * @return
     */
    protected Store compareStatisticForGames(long idGame0, long idGame1) {
        Store st0 = getStatisticForGame(idGame0)
        Store st1 = null
        if (idGame1 != 0) {
            st1 = getStatisticForGame(idGame1)
        }

        // До и после
        Store stRes = mdb.createStore("GameGameTask.statistic.compare")

        //
        for (int n = 0; n < st0.size(); n++) {
            StoreRecord recRes = stRes.add()

            //
            recRes.setValue("task", st0.get(n).getValue("task"))

            // Рейтинг до
            recRes.setValue("rating0", st0.get(n).getValue("rating"))
            recRes.setValue("ratingQuickness0", st0.get(n).getValue("ratingQuickness"))
        }

        // Рейтинг после
        if (st1 != null) {
            for (int n = 0; n < st0.size(); n++) {
                StoreRecord recRes = stRes.get(n)

                // Защита от дурака
                if (stRes.get(n).getValue("task") != st1.get(n).getValue("task")) {
                    throw new XError("st0.task != st1.task")
                }

                // Рейтинг
                recRes.setValue("rating1", st1.get(n).getValue("rating"))
                recRes.setValue("ratingQuickness1", st1.get(n).getValue("ratingQuickness"))
            }
        }

        // Заработанные и проигранные баллы (плюсы и минусы)
        for (int n = 0; n < st0.size(); n++) {
            StoreRecord recRes = stRes.get(n)

            //
            double ratingDiff = recRes.getDouble("rating0") - recRes.getDouble("rating1")
            // Увеличение рейтинга
            if (ratingDiff > 0) {
                recRes.setValue("ratingInc", ratingDiff)
            }
            // Уменьшение рейтинга
            if (ratingDiff < 0) {
                recRes.setValue("ratingDec", ratingDiff)
            }

            //
            double ratingQuicknessDiff = recRes.getDouble("ratingQuickness0") - recRes.getDouble("ratingQuickness1")
            // Увеличение рейтинга
            if (ratingQuicknessDiff > 0) {
                recRes.setValue("ratingQuicknessInc", ratingQuicknessDiff)
            }
            // Уменьшение рейтинга
            if (ratingQuicknessDiff < 0) {
                recRes.setValue("ratingQuicknessDec", ratingQuicknessDiff)
            }
        }

        //
        return stRes
    }

    /**
     * Бал за ответ
     * @param taskAnswer результат ответа
     * @return Значение от 0 до 1
     */
    private double getRatingAnswer(boolean taskAnswer) {
        if (taskAnswer == true) {
            return 1
        } else {
            return 0
        }
    }

    /**
     * Бал за скорость
     * @param taskAnswerDuration время ответа
     * @return Значение от 0 до 1
     */
    private double getRatingQuickness(Double taskAnswerDuration) {
        double ratingQuickness = 0

        if (taskAnswerDuration == null) {
            return ratingQuickness
        }

        for (int i = 0; i < ratingDurationGrade.size(); i++) {
            if (taskAnswerDuration <= ratingDurationGrade[i]) {
                ratingQuickness = ratingDurationWeight[i]
                break
            }
        }

        return ratingQuickness
    }

    protected StoreRecord loadRecGame(long idGame, long idUsr) {
        Map params = [game: idGame, usr: idUsr]
        StoreRecord recGame = mdb.loadQueryRecord(sqlRecGame(), params)
        return recGame
    }

    protected StoreRecord loadRecGamePrior(long idGame, long idUsr) {
        StoreRecord recGame = loadRecGame(idGame, idUsr)
        XDateTime dbeg = recGame.getDateTime("dbeg")
        long plan = recGame.getLong("plan")

        Map params = [plan: plan, usr: idUsr, dbeg: dbeg]
        StoreRecord recGamePrior = mdb.loadQueryRecord(sqlGamePrior(), params, false)

        return recGamePrior
    }


    protected String sqlRecGame() {
        return """
select 
    Game.* 
from 
    Game
    join GameUsr on (GameUsr.game = Game.id) 
where
    GameUsr.game = :game and 
    GameUsr.usr = :usr 
"""
    }

    protected String sqlGamePrior() {
        return """
select 
    * 
from
    Game
    join GameUsr on (GameUsr.game = Game.id)
where
    GameUsr.usr = :usr and
    Game.plan = :plan and
    Game.dbeg < :dbeg
order by
    Game.dbeg desc,
    Game.id desc
limit 1
"""
    }

    protected String sqlGameTask() {
        return """
select
    GameTask.*,
    Task.factQuestion,
    Task.factAnswer
     
from 
    Game
    join GameTask on (
        GameTask.game = Game.id and
        GameTask.usr = :usr and
        GameTask.dtTask >= :dbeg and 
        GameTask.dtTask < :dend
    )
    join Task on (
        GameTask.task = Task.id
    ) 
     
where
    Game.plan = :plan
     
order by
    Task.factQuestion,
    Task.factAnswer,
    GameTask.dtTask desc,
    GameTask.game desc
"""
    }

    /**
     * Общий рейтинг и проигранные баллы (плюсы и минусы)
     * @param stStatistic статистика по каждому заданию
     * @return сумма по всем заданиям
     */
    Map aggregateStatistic0(Store stStatistic) {
        double rating0 = StoreUtils.getSum(stStatistic, "rating0")
        double rating1 = StoreUtils.getSum(stStatistic, "rating1")
        double ratingInc = StoreUtils.getSum(stStatistic, "ratingInc")
        double ratingDec = StoreUtils.getSum(stStatistic, "ratingDec")
        rating0 = CubeUtils.discardExtraDigits(rating0)
        rating1 = CubeUtils.discardExtraDigits(rating1)
        ratingInc = CubeUtils.discardExtraDigits(ratingInc)
        ratingDec = CubeUtils.discardExtraDigits(ratingDec)
        //
        double ratingQuickness0 = StoreUtils.getSum(stStatistic, "ratingQuickness0")
        double ratingQuickness1 = StoreUtils.getSum(stStatistic, "ratingQuickness1")
        double ratingQuicknessInc = StoreUtils.getSum(stStatistic, "ratingQuicknessInc")
        double ratingQuicknessDec = StoreUtils.getSum(stStatistic, "ratingQuicknessDec")
        ratingQuickness0 = CubeUtils.discardExtraDigits(ratingQuickness0)
        ratingQuickness1 = CubeUtils.discardExtraDigits(ratingQuickness1)
        ratingQuicknessInc = CubeUtils.discardExtraDigits(ratingQuicknessInc)
        ratingQuicknessDec = CubeUtils.discardExtraDigits(ratingQuicknessDec)

        //
        Map aggretate = [
                rating0           : rating0,
                rating1           : rating1,
                ratingInc         : ratingInc,
                ratingDec         : ratingDec,
                ratingQuickness0  : ratingQuickness0,
                ratingQuickness1  : ratingQuickness1,
                ratingQuicknessInc: ratingQuicknessInc,
                ratingQuicknessDec: ratingQuicknessDec,
        ]

        //
        return aggretate
    }

    /**
     * Общий рейтинг и проигранные баллы (плюсы и минусы)
     * @param stStatistic статистика по каждому заданию
     * @return сумма по всем заданиям
     */
    Map aggregateStatistic(Store stStatistic) {
        double rating = StoreUtils.getSum(stStatistic, "rating")
        rating = CubeUtils.discardExtraDigits(rating)
        //
        double ratingQuickness = StoreUtils.getSum(stStatistic, "ratingQuickness")
        ratingQuickness = CubeUtils.discardExtraDigits(ratingQuickness)

        //
        Map aggretate = [
                rating         : rating,
                ratingQuickness: ratingQuickness,
        ]

        //
        return aggretate
    }


}