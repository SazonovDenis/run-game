package run.game.main;

import jandcode.commons.cli.*;
import jandcode.core.apx.cli.*;
import jandcode.core.cli.*;
import jandcode.core.web.cli.*;
import kis.molap.model.cli.*;
import run.game.cli.*;

public class Main {

    public static void main(String[] args) {
        new Main().run(args);
    }

    public void run(String[] args) {
        CliLauncher z = new CliLauncher(args);
        z.addExtension(new AppCliExtension());

        z.addCmd("serve", new ServeCliCmd());

        z.addCmd("db-check", new DbCheckCliCmd());
        z.addCmd("db-create", new DbCreateCliCmd());
        z.addCmd("db-loadtestdata", new DbLoadTestDataCliCmd());

        z.addCmd("cube-status", new CubeStatusCmd());
        z.addCmd("cube-fill", new CubeFillCmd());
        z.addCmd("cube-interval", new CubeIntervalCmd());
        z.addCmd("cube-audit", new CubeAuditCmd());
        z.addCmd("cube-db-init", new DbInitAuditCmd());

        z.exec();
    }

}
