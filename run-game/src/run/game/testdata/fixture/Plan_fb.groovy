package run.game.testdata.fixture

import jandcode.commons.*
import jandcode.core.dbm.fixture.*
import jandcode.core.store.*
import run.game.dao.backstage.*
import run.game.util.*

/**
 * Планы с заранее сформированным наполнением.
 */
class Plan_fb extends BaseFixtureBuilder {

    protected void onBuild() {
        Store stPlan = mdb.createStore("Plan.list.csv")
        RgmCsvUtils utils = mdb.create(RgmCsvUtils)
        String fileName = "res:run/game/testdata/csv/Plan.list.csv"
        utils.addFromCsv(stPlan, fileName)

        //
        PlanCreator planCreator = mdb.create(PlanCreator)

        //
        for (StoreRecord recPlan : stPlan) {
            String planName = recPlan.getString("planName")
            String planText = recPlan.getString("planText")
            String planTags = recPlan.getString("planTags")
            String fileNameFactsCombinations = "res:run/game/testdata/csv/plan/" + planName + ".csv"

            //
            println(planText + " [" + planName + "]")

            // -- Plan
            mdb.startTran()
            try {
                long planId = planCreator.factsCombinations_to_Plan(planText, fileNameFactsCombinations)

                // -- PlanTag
                for (String planTag : planTags.split(",")) {
                    long tagType = UtCnv.toLong(planTag.split(":")[0])
                    String tagValue = planTag.split(":")[1]
                    mdb.insertRec("PlanTag", [plan: planId, tagType: tagType, tagValue: tagValue])
                }

                mdb.commit()

            } catch (Exception e) {
                mdb.rollback()
                throw e
            }

        }
    }


}