package kis.molap.model.cli;

import jandcode.commons.cli.*;
import jandcode.commons.datetime.*;
import jandcode.commons.error.*;
import jandcode.core.cli.*;
import jandcode.core.dbm.*;
import kis.molap.model.worker.*;

public class CubeFillCmd extends BaseAppCliCmd {

    @Override
    public void exec() throws Exception {
        ModelService modelService = getApp().bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        // Считаем куб для интервала дат
        throw new XError("not implemented");
        //worker.calcToCsvWriteToDB(cubeName, dbeg, dend);
    }


    @Override
    public void cliConfigure(CliDef b) {
        b.desc("Первичное заполнение пустого куба");
        b.opt("all")
                .names("-all")
                .arg(false)
                .desc("Все кубы");
        b.opt("cubeName")
                .names("-cube")
                .arg("имя куба")
                .desc("Имя куба для заполнения");
        b.opt("dbeg")
                .names("-dbeg")
                .arg("дата в формате YYYY-MM-DD")
                .required(true)
                .desc("Начало периода");
        b.opt("dend")
                .names("-dend")
                .arg("дата в формате YYYY-MM-DD")
                .required(true)
                .desc("Конец периода");
    }

    public void setAll(boolean all) {
        this.all = all;
    }

    public void setCubeName(String cubeName) {
        this.cubeName = cubeName;
    }

    public void setCascade(boolean cascade) {
        this.cascade = cascade;
    }

    public void setDbeg(String dbeg) {
        this.dbeg = XDate.create(dbeg);
    }

    public void setDend(String dend) {
        this.dend = XDate.create(dend);
    }

    boolean all;
    String cubeName;
    boolean cascade;
    XDate dbeg;
    XDate dend;


}
