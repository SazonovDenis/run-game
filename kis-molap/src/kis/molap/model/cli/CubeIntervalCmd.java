package kis.molap.model.cli;

import jandcode.commons.*;
import jandcode.commons.cli.*;
import jandcode.commons.datetime.*;
import jandcode.commons.error.*;
import jandcode.core.cli.*;
import jandcode.core.dbm.*;
import jandcode.core.store.*;
import kis.molap.model.worker.*;

public class CubeIntervalCmd extends BaseAppCliCmd {

    @Override
    public void exec() throws Exception {
        ModelService modelService = getApp().bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        if (!all && cubeName == null) {
            throw new XError("Нужно указывать имя куба (-cube) или обновлять ве кубы (-all)");
        }

        if (all && cubeName != null) {
            throw new XError("При расчете всех кубов (-all) не нужно указывать параметр: -cube");
        }

        if (all && noCascade) {
            throw new XError("При расчете всех кубов (-all) не нужно указывать параметр: -noCascade");
        }

        // Считаем для интервала дат
        if (all) {
            worker.calcIntervalAll(dbeg, dend);
        } else {
            worker.calcIntervalCube(cubeName, dbeg, dend, !noCascade);
        }

        // Печатаем состояние
        Store st = worker.loadInfo(cubeName);
        UtOutTable.outTable(st);
    }

    @Override
    public void cliConfigure(CliDef b) {
        b.desc("Заполнение куба");
        b.opt("all")
                .names("-all")
                .arg(false)
                .desc("Все кубы");
        b.opt("cubeName")
                .names("-cube")
                .arg("CUBE")
                .desc("Имя куба для расчета");
        b.opt("noCascade")
                .names("-noCascade")
                .arg(false)
                .desc("Не заполнять влияющие кубы")
                .defaultValue(false);
        b.opt("dbeg")
                .names("-dbeg")
                .arg("YYYY-MM-DD")
                .required(true)
                .desc("Начало периода расчета");
        b.opt("dend")
                .names("-dend")
                .arg("YYYY-MM-DD")
                .required(true)
                .desc("Конец периода расчета");
    }

    public void setAll(Boolean all) {
        this.all = all;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public void setNoCascade(Boolean noCascade) {
        this.noCascade = noCascade;
    }

    public void setDbeg(String dbeg) {
        this.dbeg = XDate.create(dbeg);
    }

    public void setDend(String dend) {
        this.dend = XDate.create(dend);
    }

    boolean all = false;
    String cubeName;
    boolean noCascade = false;
    XDate dbeg;
    XDate dend;

}
