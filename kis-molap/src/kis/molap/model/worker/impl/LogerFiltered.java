package kis.molap.model.worker.impl;

import kis.molap.model.util.*;
import org.slf4j.*;

import java.util.*;

/**
 * Умеет писать в лог не слишком часто,
 * выдает сообщение в лог, если прошло достаточно времени от предыдущей печати.
 */
public class LogerFiltered {

    //
    public long LOG_STEP_DURATION_MS = 1000 * 10;
    public long LOG_STEP_SIZE = 1;

    //
    public int step;
    public int stepCountTotal;
    public long lastLogTime;
    public String info = null;

    protected Logger log;

    public LogerFiltered(Logger log) {
        this.log = log;
    }

    public Logger getLog() {
        return log;
    }

    /**
     * Начать
     */
    public void logStepStart() {
        this.step = 0;
        this.lastLogTime = new Date().getTime();
        this.stepCountTotal = 0;
    }

    /**
     * Начать с указанием общего количества шагов
     *
     * @param count общее количество шагов
     */
    public void logStepStart(int count) {
        this.step = 0;
        this.lastLogTime = new Date().getTime();
        this.stepCountTotal = count;
    }

    /**
     * Начать с указанием общего количества шагов и подсказки
     *
     * @param count общее количество шагов
     * @param info  подсказка для вывода в лог
     */
    public void logStepStart(int count, String info) {
        this.step = 0;
        this.lastLogTime = new Date().getTime();
        this.stepCountTotal = count;
        this.info = info;
    }

    /**
     * Делает очередной шаг.
     * Если сделано достаточно шагов и прошло достаточно времени от предыдущей печати в лог,
     * то выдает сообщение в лог.
     */
    public void logStepStep() {
        step = step + 1;

        //
        if (step % LOG_STEP_SIZE == 0) {
            long now = new Date().getTime();
            if (now - lastLogTime > LOG_STEP_DURATION_MS) {
                lastLogTime = now;

                //
                String infoStr;
                if (info == null) {
                    infoStr = "";
                } else {
                    infoStr = info + ", ";
                }
                if (stepCountTotal > 0) {
                    log.info("  " + infoStr + "step: " + step + "/" + stepCountTotal);
                } else {
                    log.info("  " + infoStr + "step: " + step);
                }
            }
        }
    }

    /**
     * Делает очередной шаг.
     * Если сделано достаточно шагов и прошло достаточно времени от предыдущей печати в лог, то
     * вызывает UtWorkerPrint#printStatistic_interval()
     *
     * @param sw что печатаем
     */
    @Deprecated // Метод содержит ссылку на Molap, а без этого метода - автономен
    public void logStep_printStatistic_interval(MolapStopWatch sw) {
        step = step + 1;

        //
        if (isTimeToPrint()) {
            UtWorkerPrint.printStatistic_interval(sw);
        }
    }

    /**
     * Делает очередной шаг.
     * Если сделано достаточно шагов и прошло достаточно времени от предыдущей печати в лог,
     * то вызывает UtWorkerPrint#printStatistic_audit()
     *
     * @param sw что печатаем
     */
    @Deprecated // Метод содержит ссылку на Molap, а без этого метода - автономен
    public void printStatistic_audit(MolapStopWatch sw) {
        step = step + 1;

        //
        if (isTimeToPrint()) {
            UtWorkerPrint.printStatistic_audit(sw);
        }

    }

    /**
     * Анализирует, сделано ли достаточно шагов и прошло ли достаточно времени от предыдущей печати в лог.
     * Если пришло время печатать в лог, то сбрасывает состояние и начинается новое ожидание.
     *
     * @return true, если пришло время печатать в лог
     */
    private boolean isTimeToPrint() {
        if (step % LOG_STEP_SIZE == 0) {
            long now = new Date().getTime();
            if (now - lastLogTime > LOG_STEP_DURATION_MS) {
                lastLogTime = now;

                //
                return true;
            }
        }

        //
        return false;
    }

}
