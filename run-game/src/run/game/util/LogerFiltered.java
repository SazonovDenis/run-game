package run.game.util;

import org.slf4j.*;

import java.util.*;

/**
 * Умеет писать в лог не слишком часто,
 * выдает сообщение в лог, если прошло достаточно времени от предыдущей печати.
 */
public class LogerFiltered {

    //
    public long logStepDurationMs = 1000 * 10;
    public long logStepSize = 1000;

    //
    public int step;
    public int stepCountTotal;
    public long lastLogTime;
    public String info = null;

    protected Logger log;

    public LogerFiltered(Logger log) {
        this.log = log;
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
        if (step % logStepSize == 0) {
            long now = new Date().getTime();
            if (now - lastLogTime > logStepDurationMs) {
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
     * Анализирует, сделано ли достаточно шагов и прошло ли достаточно времени от предыдущей печати в лог.
     * Если пришло время печатать в лог, то сбрасывает состояние и начинается новое ожидание.
     *
     * @return true, если пришло время печатать в лог
     */
    private boolean isTimeToPrint() {
        if (step % logStepSize == 0) {
            long now = new Date().getTime();
            if (now - lastLogTime > logStepDurationMs) {
                lastLogTime = now;

                //
                return true;
            }
        }

        //
        return false;
    }

}
