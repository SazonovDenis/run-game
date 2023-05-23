package run.game.dao.backstage


import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*

class PlanCreatorImpl extends RgmMdbUtils implements PlanCreator {


    long createPlan(String planName, Collection<Long> idItems, long dataTypeQuestion, long dataTypeAnswer, int limit) {
        //DataBox res = new DataBox()


        //
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        recPlan.setValue("text", planName)
        //
        //res.put("plan", recPlan)


        //
        Collection<DataBox> tasks = createTasks(idItems, dataTypeQuestion, dataTypeAnswer, limit)
        //
        //res.put("planTask", tasks)


        //
        long idPlan = savePlan(recPlan, tasks)

        //
        return idPlan
        //return res
    }

    Collection<DataBox> createTasks(Collection<Long> idItems, long dataTypeQuestion, long dataTypeAnswer, int limit) {
        Collection<DataBox> tasks = new ArrayList<>()

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)
        for (long idItem : idItems) {
            try {
                Collection<DataBox> tasksForItem = taskCreator.createTasks(idItem, dataTypeQuestion, dataTypeAnswer)
                tasks.addAll(tasksForItem)
            } catch (Exception e) {
                println("Task: " + idItem + ", error: " + e.message)
            }
        }

        //
        return tasks
    }


    long savePlan(StoreRecord plan, Collection<DataBox> tasks) {
        long idPlan = mdb.insertRec("Plan", plan)

        //
        Task_upd upd = mdb.create(Task_upd)
        for (DataBox task : tasks) {
            long idTask = upd.saveTask(task)
            //
            mdb.insertRec("PlanTask", [plan: idPlan, task: idTask])
        }

        //
        return idPlan
    }

    StoreRecord loadPlan(long idPlan) {
        StoreRecord recPlan = mdb.loadQueryRecord("select * from Plan where id = :id", [id: idPlan])

        //
        return recPlan
    }


    void deletePlan(long idPaln) {
        mdb.execQuery("delete from PlanFact where plan = :plan", [plan: idPaln])
        mdb.execQuery("delete from PlanTag where plan = :plan", [plan: idPaln])
        mdb.execQuery("delete from Plan where id = :plan", [plan: idPaln])
    }


}
