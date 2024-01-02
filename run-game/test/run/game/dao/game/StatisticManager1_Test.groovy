package run.game.dao.game


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
        StoreRecord recActiveGame = upd.getLastGame()
        long idGame = recActiveGame.getLong("id")
        mdb.outTable(recActiveGame)

        //
        Store st0
        println()
        st0 = compareStatisticForGames(idGame, idGame - 1)
        mdb.outTable(st0)

        idGame = idGame - 1
        println()
        st0 = compareStatisticForGames(idGame, idGame - 1)
        mdb.outTable(st0)

        idGame = idGame - 1
        println()
        st0 = compareStatisticForGames(idGame, idGame - 1)
        mdb.outTable(st0)

        idGame = idGame - 1
        println()
        st0 = compareStatisticForGames(idGame, 0)
        mdb.outTable(st0)
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

    Store compareStatisticForGames(long idGame0, long idGame1) {
        println("idGame: " + idGame1 + " -> " + idGame0)

        //
        StatisticManager1 sm = mdb.create(StatisticManager1)
        Store stRes = sm.compareStatisticForGames(idGame0, idGame1)

        // Общий рейтинг и проигранные баллы (плюсы и минусы)
        double ratingSum0 = StoreUtils.getSum(stRes, "rating0")
        double ratingSum1 = StoreUtils.getSum(stRes, "rating1")
        double ratingIncSum = StoreUtils.getSum(stRes, "ratingInc")
        double ratingDecSum = StoreUtils.getSum(stRes, "ratingDec")
        ratingSum0 = CubeUtils.discardExtraDigits(ratingSum0)
        ratingSum1 = CubeUtils.discardExtraDigits(ratingSum1)
        ratingIncSum = CubeUtils.discardExtraDigits(ratingIncSum)
        ratingDecSum = CubeUtils.discardExtraDigits(ratingDecSum)
        //
        double ratingQuicknessSum0 = StoreUtils.getSum(stRes, "ratingQuickness0")
        double ratingQuicknessSum1 = StoreUtils.getSum(stRes, "ratingQuickness1")
        double ratingQuicknessIncSum = StoreUtils.getSum(stRes, "ratingQuicknessInc")
        double ratingQuicknessDecSum = StoreUtils.getSum(stRes, "ratingQuicknessDec")
        ratingQuicknessSum0 = CubeUtils.discardExtraDigits(ratingQuicknessSum0)
        ratingQuicknessSum1 = CubeUtils.discardExtraDigits(ratingQuicknessSum1)
        ratingQuicknessIncSum = CubeUtils.discardExtraDigits(ratingQuicknessIncSum)
        ratingQuicknessDecSum = CubeUtils.discardExtraDigits(ratingQuicknessDecSum)

        //
        println("ratingSum:" + ratingSum1 + " -> " + ratingSum0)
        println("ratingIncSum:" + ratingIncSum)
        println("ratingDecSum:" + ratingDecSum)
        //
        println("ratingQuicknessSum:" + ratingQuicknessSum1 + " -> " + ratingQuicknessSum0)
        println("ratingQuicknessIncSum:" + ratingQuicknessIncSum)
        println("ratingQuicknessDecSum:" + ratingQuicknessDecSum)

        //
        return stRes
    }

}
