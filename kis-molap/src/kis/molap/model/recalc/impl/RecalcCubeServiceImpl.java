package kis.molap.model.recalc.impl;

import jandcode.commons.*;
import jandcode.commons.conf.*;
import jandcode.commons.error.*;
import jandcode.commons.error.impl.*;
import jandcode.commons.process.*;
import jandcode.core.*;
import jandcode.core.std.*;
import kis.molap.model.recalc.*;
import org.slf4j.*;

import java.util.*;

public class RecalcCubeServiceImpl extends BaseComp implements RecalcCubeService, IAppStartup, IAppShutdown {

    protected static Logger log = LoggerFactory.getLogger(RecalcCubeService.class);

    private boolean enabled = true;
    private String exe = "recalc-cube.bat";
    private int interval = 5000;

    private Timer timer;

    protected void onConfigure(BeanConfig cfg) throws Exception {
        super.onConfigure(cfg);
        //
        Conf config = getApp().bean(CfgService.class).getConf().findConf("cube/recalc-cube");
        //
        this.enabled = config.getBoolean("enabled");
        this.exe = config.getString("exe");
        this.interval = config.getInt("interval");
        //
    }

    public String getExe() {
        return UtFile.join(getApp().getAppdir(), this.exe);
    }

    public void appStartup() throws Exception {
        if (!this.enabled) {
            return;
        }
        if (this.timer != null) {
            this.timer.cancel();
            this.timer = null;
        }
        //
        TimerTask tt = new TimerTask() {
            public void run() {
                runTask();
            }
        };
        this.timer = new Timer(this.getClass().getName());
        this.timer.schedule(tt, this.interval, this.interval);
    }

    public void appShutdown() throws Exception {
        if (!this.enabled || this.timer == null) {
            return;
        }
        this.timer.cancel();
    }

    protected void runTask() {
        try {
            doRunTask();
        } catch (Exception e) {
            String msg = new ErrorFormatterDefault(false, true, false).
                    getMessage(UtError.createErrorInfo(e));
            log.error("ERROR =>\n\n" + msg + "\n");
        }
    }

    protected void doRunTask() throws Exception {
        String exeFile = getExe();
        if (!UtFile.exists(exeFile)) {
            log.warn("not exists: {}", exeFile);
            return;
        }
        //
        log.info("task start: {}", exeFile);
        RunCmd runCmd = new RunCmd();
        runCmd.setDir(getApp().getAppdir());
        runCmd.setShowout(false);
        runCmd.setSaveout(true);
        runCmd.setCmd(exeFile);
        runCmd.run();
        log.info("task stop: {}", exeFile);
        if (runCmd.getExitCode() > 0) {
            String r = UtString.join(runCmd.getOut(), "\n");
            throw new XError("error in {0}:\n{1}", exeFile, r);
        }
    }

}
