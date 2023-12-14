package kis.molap.model.cube.impl;

import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.cube.*;


/**
 * Отвечает за подготовку пространства в целевой таблице куба (в БД)
 */
public abstract class SpaceCustom implements Space, IMdbLinkSet {


    protected Mdb mdb;

    protected SpaceInfo spaceInfo;


    // ---
    // IMdbLinkSet
    // ---

    public void setMdb(Mdb mdb) {
        this.mdb = mdb;
    }


    // ---
    //
    // ---

    public void setInfo(SpaceInfo spaceInfo) {
        this.spaceInfo = spaceInfo;
    }


}
