package run.game.dao.backstage


import jandcode.core.dbm.std.*
import jandcode.core.store.*
import run.game.dao.*

class PlanCreatorImpl extends RgmMdbUtils implements PlanCreator {


    long createPlan(String planName, Collection<Long> idItems, Collection<Long> idItemsFalse, long dataTypeQuestion, long dataTypeAnswer, int limit) {
        StoreRecord recPlan = mdb.createStoreRecord("Plan")
        recPlan.setValue("text", planName)

        //
        Collection<DataBox> tasks = createTasks(idItems, idItemsFalse, dataTypeQuestion, dataTypeAnswer, limit)

        //
        long idPlan = savePlan(recPlan, tasks)

        //
        return idPlan
    }


    /**
     * При генерации плана по списку слов idItems
     * неправильные варианты берутся не из всего корпуса слов, а из этого списка.
     */
    Collection<DataBox> createTasks(Collection<Long> idItems, Collection<Long> idItemsFalse, long dataTypeQuestion, long dataTypeAnswer, int limit) {
        Collection<DataBox> tasks = new ArrayList<>()

        //
        TaskGenerator taskCreator = mdb.create(TaskGeneratorImpl)

        // Значения факта типа dataTypeAnswer у элементов из idItemsFalse дадут варианты выбора
        taskCreator.setItemsForChoiceFalse(idItemsFalse, dataTypeAnswer)

        //
        for (long idItem : idItems) {
            try {
                Collection<DataBox> tasksForItem = taskCreator.createTasks(idItem, dataTypeQuestion, dataTypeAnswer, limit)
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
        mdb.insertRec("PlanTag", [plan: idPlan, tag: RgmDbConst.Tag_access_public])

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
