package kis.molap.model.conf.impl;

import jandcode.commons.conf.*;
import jandcode.core.*;
import jandcode.core.dbm.*;
import kis.molap.model.conf.*;

import java.util.*;

public class SpaceInfoImpl extends BaseModelMember implements SpaceInfo {


    private Conf conf;

    String className;
    String title;
    String tableName;
    List<String> coords = new ArrayList<>();
    List<String> depends = new ArrayList<>();


    @Override
    protected void onConfigure(BeanConfig cfg) throws Exception {
        super.onConfigure(cfg);

        //
        conf = cfg.getConf();

        //
        name = conf.getName();
        className = conf.getString("class");
        title = conf.getString("title");
        tableName = conf.getString("table");

        //
        Conf fieldsConf = conf.findConf("coord");
        for (Conf conf : fieldsConf.getConfs()) {
            coords.add(conf.getName());
        }

        //
        Conf dependsConf = conf.findConf("depend");
        if (dependsConf != null) {
            for (Conf conf : fieldsConf.getConfs()) {
                depends.add(conf.getName());
            }
        }
    }


    // ---
    // IMoSpaceInfo
    // ---

    @Override
    public String getClassName() {
        return className;
    }

    @Override
    public String getTitle() {
        return title;
    }

    @Override
    public String getTable() {
        return tableName;
    }

    @Override
    public List<String> getCoords() {
        return coords;
    }

    @Override
    public List<String> getDepends() {
        return depends;
    }


}
