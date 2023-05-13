package run.game.dao.backstage

import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*

class Task_upd extends RgmMdbUtils {


    public DataBox loadTask(long idTask) {
        DataBox res = new DataBox()

        //
        StoreRecord recTask = mdb.createStoreRecord("Task.info")
        Store stTaskOption = mdb.createStore("TaskOption")
        Store stTaskQuestion = mdb.createStore("TaskQuestion")
        mdb.loadQueryRecord(recTask, sqlFactForTask(), [id: idTask])
        mdb.loadQuery(stTaskOption, sqlTaskOption(), [id: idTask])
        mdb.loadQuery(stTaskQuestion, sqlTaskQuestion(), [id: idTask])

        //
        res.put("task", recTask)
        res.put("taskOption", stTaskOption)
        res.put("taskQuestion", stTaskQuestion)

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


    String sqlFactForTask() {
        return """
select 
    Task.*,
    FactQuestion.dataType dataTypeQuestion, 
    FactQuestion.value valueQuestion,
    FactAnswer.dataType dataTypeAnswer, 
    FactAnswer.value valueAnswer 
from 
    Task
    join Fact FactQuestion on (Task.factQuestion = FactQuestion.id)
    join Fact FactAnswer on (Task.factAnswer = FactAnswer.id)
where 
    Task.id = :id 
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


}
