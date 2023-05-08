package run.game.testdata.fixture

import jandcode.core.dbm.fixture.*

/**
 *
 */
class Usr_fb extends BaseFixtureBuilder {

    protected void onBuild() {
        FixtureTable fxUsr = fx.table("Usr")

        //
        long id = 1000
        for (int n = 0; n < 100; n++) {
            fxUsr.add([
                    id  : id,
                    text: "Usr #" + id
            ])
            //
            id = id + 1
        }
    }

}