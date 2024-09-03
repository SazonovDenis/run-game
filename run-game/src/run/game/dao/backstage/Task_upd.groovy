package run.game.dao.backstage

import jandcode.commons.*
import jandcode.core.dao.*
import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*

class Task_upd extends RgmMdbUtils {


    public DataBox loadTask(long idTask) {
        DataBox res = new DataBox()

        //
        StoreRecord recTask = mdb.createStoreRecord("Task.rec")
        Store stTaskQuestion = mdb.createStore("TaskQuestion")
        Store stTaskOption = mdb.createStore("TaskOption")
        mdb.loadQueryRecord(recTask, sqlTask(), [id: idTask])
        mdb.loadQuery(stTaskQuestion, sqlTaskQuestion(), [id: idTask])
        mdb.loadQuery(stTaskOption, sqlTaskOption(), [id: idTask])

        //
        res.put("task", recTask)
        res.put("taskQuestion", stTaskQuestion)
        res.put("taskOption", stTaskOption)

        //
        return res
    }


    public long saveTask(DataBox task) {
        StoreRecord recTask = mdb.createStoreRecord("Task", task.get("task"))
        Store stTaskOption = task.get("taskOption")
        Store stTaskQuestion = task.get("taskQuestion")

        //
        long idTask = mdb.insertRec("Task", recTask)

        //
        for (StoreRecord rec : stTaskOption) {
            StoreRecord recIns = mdb.createStoreRecord("TaskOption")
            recIns.setValues(rec.getValues())
            recIns.setValue("task", idTask)
            mdb.insertRec("TaskOption", recIns)
        }

        //
        for (StoreRecord rec : stTaskQuestion) {
            StoreRecord recIns = mdb.createStoreRecord("TaskQuestion")
            recIns.setValues(rec.getValues())
            recIns.setValue("task", idTask)
            mdb.insertRec("TaskQuestion", recIns)
        }

        //
        return idTask
    }


    @DaoMethod
    public void saveUsrFacts(List<Map> usrFacts, long idPlan) {
        for (Map usrFact : usrFacts) {
            long factQuestion = usrFact.get("factQuestion")
            long factAnswer = usrFact.get("factAnswer")
            Map dataUsrFact = [
                    isHidden   : usrFact.get("isHidden"),
                    isKnownGood: usrFact.get("isKnownGood"),
            ]
            //
            saveUsrFact(factQuestion, factAnswer, dataUsrFact)
        }

        // Пересчитаем кубы
        long idUsr = getCurrentUsrId()
        RgmCubeUtils cubeUtils = mdb.create(RgmCubeUtils)
        cubeUtils.cubesRecalcUsrPlan(idUsr, idPlan)
    }

    @DaoMethod
    void saveUsrFact(long factQuestion, long factAnswer, Map dataUsrFact) {
        long idUsr = getCurrentUsrId()

        if (isEmptyUsrFact(dataUsrFact)) {
            mdb.deleteRec("UsrFact", [usr: idUsr, factQuestion: factQuestion, factAnswer: factAnswer])
            return
        }

        //
        StoreRecord recUsrFactNow = mdb.loadQueryRecord(
                "select * from UsrFact where usr = :usr and factQuestion = :factQuestion and factAnswer = :factAnswer",
                [usr: idUsr, factQuestion: factQuestion, factAnswer: factAnswer],
                false
        )

        //
        if (recUsrFactNow == null) {
            dataUsrFact.put("id", null)
            dataUsrFact.put("usr", idUsr)
            dataUsrFact.put("factQuestion", factQuestion)
            dataUsrFact.put("factAnswer", factAnswer)
            mdb.insertRec("UsrFact", dataUsrFact)
        } else {
            dataUsrFact.put("id", recUsrFactNow.getLong("id"))
            mdb.updateRec("UsrFact", dataUsrFact)
        }
    }

    boolean isEmptyUsrFact(Map dataUsrFact) {
        return dataUsrFact == null || (
                !UtCnv.toBoolean(dataUsrFact.get("isHidden")) &&
                        !UtCnv.toBoolean(dataUsrFact.get("isKnownGood")) &&
                        !UtCnv.toBoolean(dataUsrFact.get("isKnownBad"))
        )
    }


    String sqlTask() {
        return """
select 
    Task.*,
    FactQuestion.factType factTypeQuestion, 
    FactAnswer.factType factTypeAnswer 
from 
    Task
    join Fact FactQuestion on (Task.factQuestion = FactQuestion.id)
    join Fact FactAnswer on (Task.factAnswer = FactAnswer.id)
where 
    Task.id = :id 
"""
    }

    String sqlTaskQuestion() {
        return """
select 
    *
from 
    TaskQuestion
where
    TaskQuestion.task = :id 
"""
    }

    String sqlTaskOption() {
        return """
select 
    *
from 
    TaskOption
where
    TaskOption.task = :id 
"""
    }


}
