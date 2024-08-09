package run.game.dao.backstage


import jandcode.core.dbm.std.*
import org.junit.jupiter.api.*
import run.game.dao.*

class TaskGenerator_Test extends RgmBase_Test {

    long idFact0_rus = 2354 // печенье (rus)
    long idFact0_kaz = 2353 // печенье (kaz)

    long idFact1_rus = 2370 // молоко (rus)
    long idFact1_kaz = 2369 // сүт (kaz)

    long idFact2_rus = 812  // "секрет, тайна" (kaz-rus, tranlate)
    long idFact2_kaz = 811  // "сыр"           (kaz, spelling)

    long idFact3_rus = 2399  // "сыр"      (kaz-rus, tranlate)
    long idFact3_kaz = 2398  // "ірімшік"  (kaz, spelling)

    @Test
    void createTask_forItem_rus_kaz() {
        //utils.logOn()
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)

        long idFactQuestion = idFact3_rus
        long idFactAnswer = idFact3_kaz
        DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, null)

        println()
        println("Язык вопроса: rus")
        println()
        printTask(task)
    }

    @Test
    void createTask_forItem_kaz_rus() {
        //utils.logOn()
        TaskGenerator tg = mdb.create(TaskGeneratorImpl)

        long idFactQuestion = idFact3_kaz
        long idFactAnswer = idFact3_rus
        DataBox task = tg.generateTask(idFactQuestion, idFactAnswer, null)

        println()
        println("Язык вопроса: kaz")
        println()
        printTask(task)
    }

}
