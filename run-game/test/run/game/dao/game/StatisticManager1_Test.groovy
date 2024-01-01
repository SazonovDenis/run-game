package run.game.dao.game

import jandcode.commons.error.*
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
        mdb.outTable(recActiveGame)

        //
        Store st0
        long idGame = recActiveGame.getLong("id")
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

        //
        //utils.logOn()

        long idGame = 1242
        println("idGame: " + idGame)

        //
        StatisticManager1 sm = mdb.create(StatisticManager1)
        Store st = sm.getStatisticForGame(idGame)
        mdb.outTable(st)

        //

        // Общий рейтинг и проигранные баллы (плюсы и минусы)
        double ratingSum = StoreUtils.getSum(st, "rating")
        ratingSum = CubeUtils.discardExtraDigits(ratingSum)

        //
        println("ratingSum:" + ratingSum)

    }

    /**
     * Разница в рейтинге между играми
     * @param idGame0 текущая игра
     * @param idGame1 предыдущая игра
     * @return
     */
    Store compareStatisticForGames(long idGame0, long idGame1) {
        println("idGame: " + idGame1 + " -> " + idGame0)

        //
        StatisticManager1 sm = mdb.create(StatisticManager1)
        Store st0 = sm.getStatisticForGame(idGame0)
        Store st1 = null
        if (idGame1 != 0) {
            st1 = sm.getStatisticForGame(idGame1)
        }

        // До и после
        Store stRes = mdb.createStore("StatisticManager1.task.statistic1")

        //
        for (int n = 0; n < st0.size(); n++) {
            StoreRecord recRes = stRes.add()

            //
            recRes.setValue("task", st0.get(n).getValue("task"))

            // Рейтинг до
            recRes.setValue("rating0", st0.get(n).getValue("rating"))
        }

        // Рейтинг после
        if (st1 != null) {
            for (int n = 0; n < st0.size(); n++) {
                StoreRecord recRes = stRes.get(n)

                //
                if (stRes.get(n).getValue("task") != st1.get(n).getValue("task")) {
                    throw new XError("st0.task != st1.task")
                }

                // Рейтинг
                recRes.setValue("rating1", st1.get(n).getValue("rating"))
            }
        }

        // Заработанные и проигранные баллы (плюсы и минусы)
        for (int n = 0; n < st0.size(); n++) {
            StoreRecord recRes = stRes.get(n)

            //
            double diff = recRes.getDouble("rating0") - recRes.getDouble("rating1")

            // Увеличение рейтинга
            if (diff > 0) {
                recRes.setValue("incSum", diff)
            }
            // Уменьшение рейтинга
            if (diff < 0) {
                recRes.setValue("decSum", diff)
            }
        }

        //
        //mdb.outTable(stRes)


        // Общий рейтинг и проигранные баллы (плюсы и минусы)
        double ratingSum0 = StoreUtils.getSum(stRes, "rating0")
        double ratingSum1 = StoreUtils.getSum(stRes, "rating1")
        double incSum = StoreUtils.getSum(stRes, "incSum")
        double decSum = StoreUtils.getSum(stRes, "decSum")
        ratingSum0 = CubeUtils.discardExtraDigits(ratingSum0)
        ratingSum1 = CubeUtils.discardExtraDigits(ratingSum1)
        incSum = CubeUtils.discardExtraDigits(incSum)
        decSum = CubeUtils.discardExtraDigits(decSum)

        //
        println("ratingSum:" + ratingSum1 + " -> " + ratingSum0)
        println("incSum:" + incSum)
        println("decSum:" + decSum)

        //
        return stRes
    }

}
