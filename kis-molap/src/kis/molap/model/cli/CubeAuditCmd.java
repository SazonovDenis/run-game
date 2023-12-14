package kis.molap.model.cli;

import jandcode.commons.*;
import jandcode.commons.cli.*;
import jandcode.commons.error.*;
import jandcode.core.cli.*;
import jandcode.core.dbm.*;
import jandcode.core.store.*;
import kis.molap.model.worker.*;

public class CubeAuditCmd extends BaseAppCliCmd {

    @Override
    public void exec() throws Exception {
        ModelService modelService = getApp().bean(ModelService.class);
        WorkerService workerService = modelService.getModel().bean(WorkerService.class);
        Worker worker = workerService.createWorker();

        if (!all && cubeName == null) {
            throw new XError("Нужно указывать имя куба (-cube) или обновлять ве кубы (-all)");
        }

        if (all && cubeName != null) {
            throw new XError("При обновлении всех кубов (-all) не нужно указывать параметр: -cube");
        }

        if (all && noCascade) {
            throw new XError("При обновлении всех кубов (-all) не нужно указывать параметр: -noCascade");
        }

        if (markAge != null && noCascade) {
            throw new XError("При пометке возраста (-markAge) не нужно указывать параметр: -noCascade");
        }

        // Считаем изменения куба
        if (markAge != null) {
            if (all) {
                worker.markAuditCubeAll(markAge);
            } else {
                worker.markAuditCube(cubeName, markAge);
            }
        } else {
            if (all) {
                worker.calcAuditAll(0, 0);
            } else {
                worker.calcAuditCube(cubeName, 0, 0, !noCascade);
            }
        }

        // Печатаем состояние
        Store st = worker.loadInfoAudit();
        UtOutTable.outTable(st);
        //
        st = worker.loadInfo(cubeName);
        UtOutTable.outTable(st);
    }

    @Override
    public void cliConfigure(CliDef b) {
        b.desc("Обновление куба");
        b.opt("all")
                .names("-all")
                .arg(false)
                .desc("Все кубы");
        b.opt("cubeName")
                .names("-cube")
                .arg("CUBE")
                .desc("Имя куба для обновления");
        b.opt("noCascade")
                .names("-noCascade")
                .arg(false)
                .desc("Не обновлять влияющие кубы");
        b.opt("markAge")
                .names("-markAge")
                .arg("Возраст для пометки возраста")
                .desc("Не расчитывать, а только пометить возраст");
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

    public void setMarkAge(Long markAge) {
        this.markAge = markAge;
    }

    boolean all = false;
    String cubeName;
    boolean noCascade = false;
    Long markAge;

}
