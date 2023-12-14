package kis.molap.model.conf.impl;

import jandcode.commons.conf.*;
import jandcode.core.*;
import jandcode.core.dbm.*;
import kis.molap.model.conf.*;

import java.util.*;

public class CubeInfoImpl extends BaseModelMember implements CubeInfo, CubeConf {


    private Conf conf;

    String className;
    String title;
    String space;
    boolean isBunch;
    List<String> fields = new ArrayList<>();
    List<String> depends = new ArrayList<>();
    List<String> dependCubes = new ArrayList<>();

    boolean enabled;


    @Override
    protected void onConfigure(BeanConfig cfg) throws Exception {
        super.onConfigure(cfg);

        //
        conf = cfg.getConf();

        //
        className = conf.getString("class");
        //
        name = conf.getName();
        //
        title = conf.getString("title");
        //
        space = conf.getString("space");
        //
        isBunch = conf.getBoolean("isBunch");
        //
        Conf fieldsConf = conf.findConf("field");
        for (Conf conf : fieldsConf.getConfs()) {
            fields.add(conf.getName());
        }
        //
        Conf dependsConf = conf.findConf("depend");
        if (dependsConf != null) {
            for (Conf conf : dependsConf.getConfs()) {
                depends.add(conf.getName());
            }
        }
        //
        Conf dependsCubeConf = conf.findConf("dependCube");
        if (dependsCubeConf != null) {
            for (Conf conf : dependsCubeConf.getConfs()) {
                dependCubes.add(conf.getName());
            }
        }


        //
        enabled = conf.getBoolean("enabled", true);
    }


    // ---
    // ICubeInfo
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
    public String getSpace() {
        return space;
    }

    @Override
    public boolean isBunch() {
        return isBunch;
    }

    @Override
    public List<String> getFields() {
        return fields;
    }

    @Override
    public List<String> getDepends() {
        return depends;
    }

    @Override
    public List<String> getDependCubes() {
        return dependCubes;
    }


    // ---
    // ICubeConf
    // ---

    @Override
    public boolean getEnabled() {
        return enabled;
    }


}
