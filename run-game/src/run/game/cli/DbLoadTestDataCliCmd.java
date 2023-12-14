package run.game.cli;

import jandcode.commons.cli.*;
import jandcode.core.cli.*;
import jandcode.core.dbm.std.*;

public class DbLoadTestDataCliCmd extends BaseAppCliCmd {


    @Override
    public void exec() throws Exception {
        CliDbTools dbTools = new CliDbTools(getApp(), getModelName());
        dbTools.showInfo();
        dbTools.loadTestData(getSuiteNames());
    }

    @Override
    public void cliConfigure(CliDef b) {
        b.desc("Заполнение базы данных тестовыми данными");
        b.opt("modelName")
                .names("-m").arg("MODEL")
                .desc("Для какой модели")
                .defaultValue("default");
        b.opt("suiteNames")
                .names("-s").arg("FIXTURE-SUITES")
                .desc("Имена fixture-suite через запятую, которые нужно загрузить");
    }

    private String modelName = "default";
    private String suiteNames = "";

    public String getModelName() {
        return modelName;
    }

    public void setModelName(String modelName) {
        this.modelName = modelName;
    }

    public String getSuiteNames() {
        return suiteNames;
    }

    public void setSuiteNames(String suiteNames) {
        this.suiteNames = suiteNames;
    }

}
