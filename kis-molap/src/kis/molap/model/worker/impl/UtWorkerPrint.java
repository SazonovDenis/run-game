package kis.molap.model.worker.impl;

import jandcode.commons.*;
import kis.molap.model.util.*;
import org.slf4j.*;


public class UtWorkerPrint {

    protected static Logger log = UtLog.getLogConsole();

    public static void printStatistic_audit(MolapStopWatch sw) {
        long countCalcToDo = sw.getLong("dirty", "count");
        long countCalcDone = sw.getLong("calc", "count");
        long countWriteToDo = sw.getLong("dirty", "countFull");
        long countWriteDone = sw.getLong("write", "count");
        double duration = sw.getDuration("calc") + sw.getDuration("write");
        //
        double leftMinutes = duration * (countCalcToDo - countCalcDone) / countCalcDone / 1000 / 60;
        double rate = 1000 * countCalcDone / duration;
        //
        if (rate > 0.1) {
            rate = Math.round(10 * rate) / 10.0;
        } else {
            rate = Math.round(100 * rate) / 100.0;
        }
        leftMinutes = Math.round(10 * leftMinutes) / 10.0;
        //
        log.info("Расчет и запись, координата " + countCalcDone + "/" + countCalcToDo +
                 ", создано записей " + countWriteDone + "/" + countWriteToDo +
                 ", " + rate + " координат/сек, осталось " + leftMinutes + " мин." + UtString.repeat(" ", 10)
        );
    }

    public static void printStatistic_interval(MolapStopWatch sw) {
        long countCalcToDo = sw.getLong("dirty", "count");
        long countCalcDone = sw.getLong("calc", "count");
        long countWriteDone = sw.getLong("write", "count");
        double duration = sw.getDuration("calc") + sw.getDuration("write");
        //
        double leftMinutes = duration * (countCalcToDo - countCalcDone) / countCalcDone / 1000 / 60;
        double rate = 1000 * countCalcDone / duration;
        //
        if (rate > 0.1) {
            rate = Math.round(10 * rate) / 10.0;
        } else {
            rate = Math.round(100 * rate) / 100.0;
        }
        leftMinutes = Math.round(10 * leftMinutes) / 10.0;
        //
        log.info("Расчет и запись, координата " + countCalcDone + "/" + countCalcToDo +
                 ", создано записей " + countWriteDone +
                 ", " + rate + " координат/сек, осталось " + leftMinutes + " мин." + UtString.repeat(" ", 10)
        );
    }

}
