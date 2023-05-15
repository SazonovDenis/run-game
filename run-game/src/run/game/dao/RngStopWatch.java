package run.game.dao;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.commons.error.*;
import jandcode.commons.variant.*;

import java.util.*;


/**
 * Набор поименованых таймеров.
 * Умеет не только измерять время, но и хранить связанные с этим переменные (в Map).
 */
public class RngStopWatch {

    private String DEFAULT_TIMER_NAME = "__default_timer__";

    // Набор таймеров
    private Map<String, IVariantMap> stopWatchItems = null;


    public RngStopWatch() {
        stopWatchItems = new HashMap<String, IVariantMap>();
    }

    /**
     * Таймер по имени
     *
     * @param timerName имя таймера
     * @return данные таймера или null, если таймера нет
     */
    public IVariantMap get_timer(String timerName) {
        IVariantMap swItem = stopWatchItems.get(timerName);
        if (swItem == null) {
            throw new XError(String.format("Таймер %s не найден", timerName));
        }
        return swItem;
    }

    public IVariantMap start() {
        return start(DEFAULT_TIMER_NAME);
    }

    public IVariantMap stop() {
        return stop(DEFAULT_TIMER_NAME);
    }

    public long getDuration() {
        return getDuration(DEFAULT_TIMER_NAME);
    }


    /**
     * Создать и запустить таймер.
     * Новый или существующий стоящий таймер - запускается.
     * Существующий запущенный таймер - продолжает работу, вызов метода игнорируется.
     *
     * @param timerName имя таймера
     * @return данные нового или уже сущесвующего таймера
     */
    public IVariantMap start(String timerName) {
        IVariantMap metric = stopWatchItems.get(timerName);

        // Таймер сущестует?
        if (metric == null) {
            // Таймер не сущестует

            // Создаем
            metric = new VariantMap();
            stopWatchItems.put(timerName, metric);

            // Запускаем
            metric.put("sw_start", XDateTime.now());
            metric.put("sw_stop", null);
            metric.put("sw_steps", 1);
        } else {
            // Таймер сущестует

            // Таймер запущен?
            if (metric.get("sw_stop") != null) {
                // Таймер не запущен - запускаем
                metric.put("sw_start", XDateTime.now());
                metric.put("sw_stop", null);
                metric.put("sw_steps", metric.getInt("sw_steps") + 1);
            }
        }

        //
        return metric;
    }

    /**
     * Остановить таймер.
     * Существующий запущенный таймер - остановится.
     * Существующий стоящий таймер - продолжит стоять, вызов метода игнорируется.
     * Таймер не существует - вызов метода игнорируется.
     *
     * @param timerName имя таймера
     * @return данные таймера или null, если таймера нет
     */
    public IVariantMap stop(String timerName) {
        IVariantMap metric = stopWatchItems.get(timerName);

        // Таймер есть и он запущен - останавливаем
        if (metric != null && metric.get("sw_stop") == null) {
            XDateTime sw_stop = XDateTime.now();
            long sw_duration = getDuration(timerName);

            //
            metric.put("sw_stop", sw_stop);
            metric.put("sw_duration", sw_duration);
        }

        //
        return metric;
    }

    /**
     * Остановить таймер.
     * Существующий запущенный таймер - продолжит работать, но сбросится.
     * Существующий стоящий таймер - продолжит стоять.
     * Таймер не существует - вызов метода игнорируется.
     *
     * @param timerName имя таймера
     * @return данные таймера или null, если таймера нет
     */
    public IVariantMap clear(String timerName) {
        IVariantMap metric = stopWatchItems.get(timerName);

        // Таймер есть и он запущен - останавливаем
        if (metric != null && metric.get("sw_stop") == null) {
            metric.put("sw_duration", 0);
        }

        //
        return metric;
    }

    /**
     * Общее проработанное время таймера, миллисекунд
     *
     * @param timerName имя таймера
     */
    public long getDuration(String timerName) {
        IVariantMap metric = stopWatchItems.get(timerName);

        // Таймера вообще нет
        if (metric == null) {
            return 0L;
        }

        // Накопленное время
        long sw_duration = metric.getLong("sw_duration");

        // Для работающего таймера прибавим время с последнего запуска
        if (metric.get("sw_stop") == null) {
            XDateTime sw_start = metric.getDateTime("sw_start");
            sw_duration = sw_duration + XDateTime.now().diffMSec(sw_start);
        }

        //
        return sw_duration;
    }

    /**
     * Удобная обертка для задания числовых переменных в данных таймера.
     *
     * @param timerName таймер
     * @param key       имя числового поля
     * @param value     новое значение
     */
    public void setValue(String timerName, String key, long value) {
        IVariantMap swItem = get_timer(timerName);
        swItem.setValue(key, value);
    }

    /**
     * Удобная обертка для задания числовых переменных в данных таймера.
     *
     * @param timerName таймер
     * @param key       имя числового поля
     * @param value     новое значение
     */
    public void setValue(String timerName, String key, Double value) {
        IVariantMap swItem = get_timer(timerName);
        swItem.setValue(key, value);
    }

    /**
     * Инкрементация числовых переменных в данных таймера.
     * Удобная обертка вместо вызова пары методов getValue()+setValue()
     *
     * @param timerName таймер
     * @param key       имя числового поля
     * @param value     сколько прибавим
     */
    public void setValuePlus(String timerName, String key, long value) {
        IVariantMap swItem = get_timer(timerName);
        swItem.setValue(key, swItem.getDouble(key) + value);
    }

    /**
     * Инкрементация числовых переменных в данных таймера.
     * Удобная обертка вместо вызова пары методов getValue()+setValue()
     *
     * @param timerName таймер
     * @param key       имя числового поля
     * @param value     сколько прибавим
     */
    public void setValuePlus(String timerName, String key, Double value) {
        IVariantMap swItem = get_timer(timerName);
        swItem.setValue(key, swItem.getDouble(key) + value);
    }


    public long getValueLong(String timerName, String key) {
        IVariantMap swItem = stopWatchItems.get(timerName);
        if (swItem == null) {
            return 0L;
        }
        return swItem.getLong(key);
    }

    public double getValueDouble(String timerName, String key) {
        IVariantMap swItem = stopWatchItems.get(timerName);
        if (swItem == null) {
            return 0D;
        }
        return swItem.getDouble(key);
    }

    public XDateTime getStart(String timerName) {
        IVariantMap swItem = get_timer(timerName);
        return swItem.getDateTime("sw_start");
    }

    public XDateTime getStop(String timerName) {
        IVariantMap swItem = get_timer(timerName);
        return swItem.getDateTime("sw_stop");
    }

    /**
     * Печатает данные таймеров.
     */
    public void printItems() {
        printItems(false);
    }

    /**
     * Печатает данные таймеров с датами запуска и остановки.
     */
    public void printItems(boolean printStartStop) {
        for (Map.Entry e : stopWatchItems.entrySet()) {
            String timerName = (String) e.getKey();
            IVariantMap metric = (IVariantMap) e.getValue();

            XDateTime sw_start = metric.getDateTime("sw_start");
            XDateTime sw_stop = metric.getDateTime("sw_stop");
            int sw_steps = metric.getInt("sw_steps");
            long sw_duration = getDuration(timerName);

            //
            System.out.println(timerName);
            System.out.println("  " + UtString.padRight("sw_duration", 16) + ": " + durationToStr(sw_duration));

            //
            if (printStartStop) {
                if (metric.get("sw_stop") == null) {
                    System.out.println("  " + UtString.padRight("sw_start", 16) + ": " + sw_start);
                    System.out.println("  " + UtString.padRight("sw_stop", 16) + ": " + "running");
                    System.out.println("  " + UtString.padRight("sw_steps", 16) + ": " + sw_steps);
                } else {
                    System.out.println("  " + UtString.padRight("sw_start", 16) + ": " + sw_start);
                    System.out.println("  " + UtString.padRight("sw_stop", 16) + ": " + sw_stop);
                    System.out.println("  " + UtString.padRight("sw_steps", 16) + ": " + sw_steps);
                }
            }

            //
            for (Map.Entry x : metric.entrySet()) {
                String key = (String) x.getKey();
                if (key.compareTo("sw_start") != 0 &&
                        key.compareTo("sw_stop") != 0 &&
                        key.compareTo("sw_duration") != 0 &&
                        key.compareTo("sw_steps") != 0
                ) {
                    System.out.println("  " + UtString.padRight(key, 16) + ": " + String.valueOf(x.getValue()));
                }
            }
        }

    }

    private String durationToStr(long sw_duration) {
        if (sw_duration > 1000) {
            return sw_duration / 1000 + " sec " + sw_duration % 1000 + " msec";
        } else {
            return sw_duration + " msec";
        }
    }


}
