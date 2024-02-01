package run.game.dao.game

import jandcode.commons.datetime.*
import jandcode.core.auth.std.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import kis.molap.ntbd.model.*
import org.junit.jupiter.api.*
import run.game.dao.*
import run.game.util.*

class StatisticManager1_Test extends RgmBase_Test {


    @Test
    void getGameStatisticByPlan() {

        //
        //utils.logOn()

        // Последняя игра
        Server upd = mdb.create(ServerImpl)
        DataBox activeGame = upd.getLastGame()
        StoreRecord recActiveGame = activeGame.get("game")
        long idGame = recActiveGame.getLong("id")
        mdb.outTable(recActiveGame)

        //
        println()
        compareStatisticForGames(idGame, idGame - 1)

        idGame = idGame - 1
        println()
        compareStatisticForGames(idGame, idGame - 1)

        idGame = idGame - 1
        println()
        compareStatisticForGames(idGame, idGame - 1)

        idGame = idGame - 1
        println()
        compareStatisticForGames(idGame, 0)
    }

    @Test
    void getGameStatisticByGame() {
        long idGame = 1004
        //
        setCurrentUser(new DefaultUserPasswdAuthToken("user1010", ""))
        println("CurrentUser: " + authSvc.getCurrentUser().attrs)

        //
        StatisticManager1 sm = mdb.create(StatisticManager1)
        Store stStatistic = sm.getStatisticForGame(idGame)

        //
        println()
        mdb.outTable(stStatistic)
    }

    @Test
    void getStatisticForPlanInternal() {
        long idPlan = 1000
        long idUsr = 1000

        // Период заданий для плана
        XDateTime dend = XDateTime.now()
        XDateTime dbeg = dend.addDays(-10)

        //
        StatisticManager1 sm = mdb.create(StatisticManager1)
        Store st = sm.getStatisticForPlanInternal(idPlan, idUsr, dbeg, dend)

        //
        println()
        println("Plan: " + idPlan)
        mdb.outTable(st)
    }


    @Test
    void getStatisticForGame() {
        //utils.logOn()

        //
        Server upd = mdb.create(ServerImpl)
        StoreRecord recActiveGame = upd.getLastGame()
        long idGame = recActiveGame.getLong("id")
        //long idGame = 1258
        println("idGame: " + idGame)

        // ---
        StatisticManager1 sm = mdb.create(StatisticManager1)
        Store st = sm.getStatisticForGame(idGame)
        mdb.outTable(st)

        // Общий рейтинг и проигранные баллы (плюсы и минусы)
        double ratingSum = StoreUtils.getSum(st, "rating")
        ratingSum = CubeUtils.discardExtraDigits(ratingSum)

        //
        println("ratingSum:" + ratingSum)

        //
        println()
        st = sm.compareStatisticForGamePrior(idGame)
        mdb.outTable(st)
    }

    void compareStatisticForGames(long idGame0, long idGame1) {
        println("idGame: " + idGame1 + " -> " + idGame0)

        //
        StatisticManager1 sm = mdb.create(StatisticManager1)
        Store stStatistic = sm.compareStatisticForGames(idGame0, idGame1)

        // Общий рейтинг и проигранные баллы (плюсы и минусы)
        Map aggretate = sm.aggregateStatistic0(stStatistic)

        // Печатаем
        println("rating:" + aggretate.get("rating1") + " -> " + aggretate.get("rating0"))
        println("ratingInc:" + aggretate.get("ratingInc"))
        println("ratingDec:" + aggretate.get("ratingDec"))
        //
        println("ratingQuickness:" + aggretate.get("ratingQuickness1") + " -> " + aggretate.get("ratingQuickness0"))
        println("ratingQuicknessInc:" + aggretate.get("ratingQuicknessInc"))
        println("ratingQuicknessDec:" + aggretate.get("ratingQuicknessDec"))
        //
        mdb.outTable(stStatistic)
    }

}
