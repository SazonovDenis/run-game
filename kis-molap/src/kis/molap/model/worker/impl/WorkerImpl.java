package kis.molap.model.worker.impl;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.commons.error.*;
import jandcode.commons.named.*;
import jandcode.core.*;
import jandcode.core.db.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;
import kis.molap.model.conf.*;
import kis.molap.model.coord.*;
import kis.molap.model.coord.impl.*;
import kis.molap.model.cube.*;
import kis.molap.model.service.*;
import kis.molap.model.util.*;
import kis.molap.model.value.*;
import kis.molap.model.value.impl.*;
import kis.molap.model.worker.*;
import org.apache.tools.ant.util.*;
import org.slf4j.*;

import java.util.*;


public class WorkerImpl extends BaseModelMember implements Worker {

    protected static Logger log = UtLog.getLogConsole();

    CubeService cubeService;
    AgeManager ageManager;
    AuditManager auditManager;

    Mdb mdbRead;
    Mdb mdbWrite;

    UtWorkerLogDb utWorkerLogDb;


    // ---
    // region BaseModelMember
    // ---


    @Override
    protected void onConfigure(BeanConfig cfg) throws Exception {
        super.onConfigure(cfg);

        //
        cubeService = getModel().bean(CubeService.class);

        //
        Model modelRead = getModel();
        mdbRead = modelRead.createMdb();
        //
        ModelService modelService = getApp().bean(ModelService.class);
        Model modelWrite = modelService.getModel("write");
        mdbWrite = modelWrite.createMdb();

        //
        WorkerService workerService = getModel().bean(WorkerService.class);
        ageManager = workerService.createAgeManager(mdbRead);
        auditManager = workerService.createAuditManager(mdbRead);

        //
        utWorkerLogDb = mdbRead.create(UtWorkerLogDb.class);
    }


    // ---
    // endregion
    // ---


    // ---
    // region Worker
    // ---


    @Override
    public Store loadInfo(String cubeName) throws Exception {
        connect();
        try {
            // Отображем состояние всех зарегистрированных кубов, а не всех расчитанных,
            // поэтому список полный
            Store stRes = mdbRead.createStore("Molap.info");

            // Загружаем состояние расчитанных кубов
            String where_sql = "";
            if (cubeName != null) {
                where_sql = " and Molap_status.cube_name = '" + cubeName + "'";
            }
            String sql = """
                    select
                        Molap_status.cube_name,
                        Molap_status.dt_min,
                        Molap_status.dt_max,
                        Molap_status.done_id,
                        Molap_status.done_dt,
                        Molap_statistic.rec_rate,
                        Molap_statistic.one_rec_cost,
                        Molap_statistic.one_rec_durationDirty,
                        Molap_statistic.one_rec_durationCalc,
                        Molap_statistic.one_rec_durationWrite,
                        Molap_statistic.interval_rec_cost
                    from
                        Molap_status
                        left join Molap_statistic on Molap_status.cube_name = Molap_statistic.cube_name
                    where
                        (1=1) ${sqlWhere}
                    order by
                        Molap_status.cube_name
                    """;
            sql = sql.replace("${sqlWhere}", where_sql);

            //
            Store st = mdbRead.loadQuery(sql);
            StoreIndex sti = st.getIndex("cube_name");

            // Обновляем состояние
            for (CubeInfo cubeInfo : cubeService.getCubes()) {
                StoreRecord recRes = stRes.add();
                recRes.setValue("cube_name", cubeInfo.getName());

                StoreRecord rec = sti.get(cubeInfo.getName());
                if (rec != null) {
                    recRes.setValue("dt_min", rec.getValueNullable("dt_min"));
                    recRes.setValue("dt_max", rec.getValueNullable("dt_max"));
                    recRes.setValue("done_id", rec.getValueNullable("done_id"));
                    recRes.setValue("done_dt", rec.getValueNullable("done_dt"));
                    recRes.setValue("rec_rate", rec.getValue("rec_rate"));
                    recRes.setValue("one_rec_cost", rec.getValue("one_rec_cost"));
                    recRes.setValue("one_rec_durationDirty", rec.getValue("one_rec_durationDirty"));
                    recRes.setValue("one_rec_durationCalc", rec.getValue("one_rec_durationCalc"));
                    recRes.setValue("one_rec_durationWrite", rec.getValue("one_rec_durationWrite"));
                    recRes.setValue("interval_rec_cost", rec.getValue("interval_rec_cost"));
                }
            }

            //
            return stRes;
        } finally {
            disconnect();
        }
    }


    @Override
    public Store loadInfoAudit() throws Exception {
        connect();
        try {
            Store st = mdbRead.loadQuery("""
                    select
                         min(id) id_min,
                         max(id) id_max,
                         make_timestamp(
                             cast(EXTRACT(YEAR from dt) as INT),
                             cast(EXTRACT(MON from dt) as INT),
                             cast(EXTRACT(DAY from dt) as INT),
                             cast(EXTRACT(HOUR from dt) as INT),
                             0, 0
                         ) dt,
                         count(*) count
                    from
                        molap_audit
                    group by
                        EXTRACT(YEAR from dt),
                        EXTRACT(MON from dt),
                        EXTRACT(DAY from dt),
                        EXTRACT(HOUR from dt)
                    order by
                        dt desc
                    limit 24
                    """);
            return st;
        } finally {
            disconnect();
        }
    }


    @Override
    public void markAuditCubeAll(long auditAgeTo) throws Exception {
        connect();
        try {
            List<CubeInfo> listToCalc = getEnabledCubesAll();

            // Если не указали явно возраст - берем по текущему возрасту в базе
            if (auditAgeTo == 0) {
                auditAgeTo = ageManager.getActualAge();
            }

            //
            for (CubeInfo cubeInfoToCalc : listToCalc) {
                ageManager.setCubeAgeDone(cubeInfoToCalc, auditAgeTo);
            }
        } finally {
            disconnect();
        }
    }


    @Override
    public void markAuditCube(String cubeName, long auditAgeTo) throws Exception {
        connect();
        try {
            CubeInfo cubeInfo = cubeService.getCubeInfo(cubeName);
            List<CubeInfo> listToCalc = new ArrayList<>();
            listToCalc.add(cubeInfo);

            // Если не указали явно возраст - берем по текущему возрасту в базе
            if (auditAgeTo == 0) {
                auditAgeTo = ageManager.getActualAge();
            }

            //
            for (CubeInfo cubeInfoToCalc : listToCalc) {
                ageManager.setCubeAgeDone(cubeInfoToCalc, auditAgeTo);
            }
        } finally {
            disconnect();
        }
    }


    @Override
    public void calcIntervalCube(String cubeNames, XDate intervalDbeg, XDate intervalDend, boolean calcCascade) throws Exception {
        connect();
        try {
            // Имена кубов можно указывать через запятую
            List<CubeInfo> listToCalc = parseSortCubeNames(cubeNames, calcCascade);

            //
            calcInterval_markRange(listToCalc, intervalDbeg, intervalDend);
        } finally {
            disconnect();
        }
    }

    @Override
    public void calcIntervalAll(XDate intervalDbeg, XDate intervalDend) throws Exception {
        log.info("Заполнение всех кубов");

        //
        connect();
        try {
            // Кубы гарантировано отсортированы по зависимостям,
            // поэтому сначала выполнится расчет самых не зависимых кубов
            List<CubeInfo> listToCalc = getEnabledCubesAll();

            //
            calcInterval_markRange(listToCalc, intervalDbeg, intervalDend);

            //
            log.info("Заполнение всех кубов выполнено");

        } finally {
            disconnect();
        }
    }


    @Override
    public void calcAuditCube(String cubeName, long auditAgeFrom, long auditAgeTo, boolean calcCascade) throws Exception {
        connect();
        try {
            log.info("Расчет по аудиту куба [" + cubeName + "], calcCascade: " + calcCascade);

            //
            AgeManager ageManager = mdbRead.create(AgeManagerImpl.class);

            // Имена кубов можно указывать через запятую
            List<CubeInfo> listToCalc = parseSortCubeNames(cubeName, calcCascade);

            // Если не указали явно возраст - берем по текущему возрасту в базе
            if (auditAgeTo == 0) {
                auditAgeTo = ageManager.getActualAge();
            }

            //
            int n = 0;
            for (CubeInfo cubeInfoToCalc : listToCalc) {
                n = n + 1;
                log.info("Куб [" + cubeInfoToCalc.getName() + "], " + n + " / " + listToCalc.size());

                // Текущий возраст куба
                long cubeAuditAge = ageManager.getCubeAgeDone(cubeInfoToCalc);

                // Если не указали явно возраст - берем по текущему возрасту в базе
                if (auditAgeFrom == 0) {
                    auditAgeFrom = cubeAuditAge;
                }

                //
                calcAuditCube_internal(cubeInfoToCalc.getName(), auditAgeFrom, auditAgeTo);

                // Актуальность куба увеличилась - отметим возраст обработанного аудита куба
                if (auditAgeTo >= cubeAuditAge && auditAgeFrom <= cubeAuditAge) {
                    ageManager.setCubeAgeDone(cubeInfoToCalc, auditAgeTo);
                }
            }

            //
            log.info("Расчет по аудиту куба [" + cubeName + "] выполнен");

        } finally {
            disconnect();
        }
    }


    private void calcInterval_markRange(List<CubeInfo> listToCalc, XDate intervalDbeg, XDate intervalDend) throws Exception {
        int n = 0;
        for (CubeInfo cubeInfoToCalc : listToCalc) {
            n = n + 1;
            log.info("Куб [" + cubeInfoToCalc.getName() + "], " + n + " / " + listToCalc.size());

            // Текущие размеры куба
            Period cubeRange = ageManager.getMinMaxDt(cubeInfoToCalc);

            //
            calcIntervalCube_internal(cubeInfoToCalc.getName(), intervalDbeg, intervalDend);

            // Интервал считали с перекрытием - расширим пространство куба
            if (cubeRange.dend.equals(UtDateTime.EMPTY_DATE) || (intervalDend.compareTo(cubeRange.dend) > 0 && intervalDbeg.compareTo(cubeRange.dend) <= 0)) {
                ageManager.setMaxDt(cubeInfoToCalc, intervalDend);
            }
            if (cubeRange.dbeg.equals(UtDateTime.EMPTY_DATE) || (intervalDbeg.compareTo(cubeRange.dbeg) < 0 && intervalDend.compareTo(cubeRange.dbeg) >= 0)) {
                ageManager.setMinDt(cubeInfoToCalc, intervalDbeg);
            }
        }
    }


    @Override
    public void calcAuditAll(long auditAgeFrom, long auditAgeTo) throws Exception {
        log.info("Расчет по аудиту всех кубов");

        //
        connect();
        try {
            // Кубы гарантировано отсортированы по зависимостям,
            // поэтому сначала выполнится расчет самых не зависимых кубов
            List<CubeInfo> listToCalc = getEnabledCubesAll();

            // Если не указали явно возраст - берем по текущему возрасту в базе
            if (auditAgeTo == 0) {
                auditAgeTo = ageManager.getActualAge();
            }

            //
            int n = 0;
            for (CubeInfo cubeInfoToCalc : listToCalc) {
                n = n + 1;
                log.info("Куб [" + cubeInfoToCalc.getName() + "], " + n + " / " + listToCalc.size());

                // Текущий возраст куба
                long cubeAuditAgeFrom = ageManager.getCubeAgeDone(cubeInfoToCalc);

                // Если не указали явно возраст - берем по текущему возрасту в базе
                if (auditAgeFrom == 0) {
                    auditAgeFrom = cubeAuditAgeFrom;
                }

                //
                calcAuditCube_internal(cubeInfoToCalc.getName(), auditAgeFrom, auditAgeTo);

                // Актуальность куба увеличилась - отметим возраст обработанного аудита куба
                if (auditAgeTo >= cubeAuditAgeFrom && auditAgeFrom <= cubeAuditAgeFrom) {
                    ageManager.setCubeAgeDone(cubeInfoToCalc, auditAgeTo);
                }
            }

            //
            log.info("Расчет по аудиту всех кубов выполнен");

        } finally {
            disconnect();
        }
    }

    // ---
    // endregion
    // ---


    // ---
    // region Внутренние методы
    // ---


    private void calcAuditCube_internal(String cubeName, long auditAgeFrom, long auditAgeTo) throws Exception {
        log.info("Расчет по аудиту куба [" + cubeName + "] с [" + auditAgeFrom + "] по [" + auditAgeTo + "]");

        //
        try {
            //
            MolapStopWatch sw = new MolapStopWatch();
            //
            LogerFiltered logCube = new LogerFiltered(log);

            // Статистика (общая)
            sw.start("total");

            // Создаем куб и пространство
            CubeInfo cubeInfo = cubeService.getCubeInfo(cubeName);
            SpaceInfo spaceInfo = cubeService.getSpaceInfo(cubeInfo.getSpace());
            Cube cube = cubeService.createCube(cubeInfo.getName(), mdbRead);


            // ---
            // Результат расчета кладем в БД
            ICalcResultFlush res = createCalcResultDb(cubeInfo);
            res.open();


            // ---
            // Узнаем что нужно пересчитать
            log.info("Анализ аудита");

            //
            CoordList dirtyCoords = getDirtyCoords(cube, auditAgeFrom, auditAgeTo, sw);


            // ---
            // Расчет и запись
            log.info("Расчет и запись по аудиту");

            // Защита от изменения данных, относящихся к диапазону дат, которые куб не содержит
            Period cubeRange = ageManager.getMinMaxDt(cubeInfo);

            // Расчет
            for (Coord coord : dirtyCoords) {
                CoordList coordsToCalc = CoordList.create();
                coordsToCalc.add(coord);

                // Есть коррдината "дата" в dirtyCoords?
                if (UtCoord.getValueDate(coord) == null) {
                    // Даты в dirtyCoords нет
                    calcCubeCoords(cube, coordsToCalc, cubeRange.dbeg, cubeRange.dend, res, sw);

                } else {
                    for (Period coordPeriod : UtCoord.getValueDateIterator(coord)) {
                        if (!cubeRange.isCross(coordPeriod)) {
                            throw new XError("Coord not inside cubeRange, cube: {0}, coord: {1}, cubeRange: {2}", cubeName, coord, cubeRange);
                        }

                        //
                        calcCubeCoords(cube, coordsToCalc, coordPeriod.dbeg, coordPeriod.dend, res, sw);
                    }
                }


                // Статистика
                sw.setValuePlus("calc", "count", 1);

                // Печать статистики
                UtWorkerPrint.printStatistic_audit(sw);
            }

            // ---
            // Запись остатков
            res.close();


            // ---
            // Статистика
            sw.stop("total");

            // Печать статистики
            logCube.printStatistic_audit(sw);

            // Запись статистики в журнал
            utWorkerLogDb.writeLog_audit(sw, cubeInfo, auditAgeFrom, auditAgeTo);

            //
            log.info("Расчет и запись по аудиту куба [" + cubeName + "] выполнены," + UtString.repeat(" ", 10) +
                    "координат: " + sw.getLong("dirty", "count") + ", " +
                    "записей: " + res.size()
            );

        } catch (Exception e) {
            // Печать ошибки
            log.error("Ошибка при расчете по аудиту куба [" + cubeName + "]: " + e.getMessage());
            log.error(StringUtils.getStackTrace(e));
            // Запись ошибки в журнал
            utWorkerLogDb.writeLog_audit_error(cubeName, auditAgeFrom, auditAgeTo, e);
            //
            throw e;
        }
    }

    private void calcIntervalCube_internal(String cubeName, XDate intervalDbeg, XDate intervalDend) throws Exception {
        log.info("Заполнение куба [" + cubeName + "] с [" + intervalDbeg + "] по [" + intervalDend + "]");

        //
        try {
            MolapStopWatch sw = new MolapStopWatch();

            // Статистика
            sw.start("total");

            // Создаем куб и пространство
            CubeInfo cubeInfo = cubeService.getCubeInfo(cubeName);
            SpaceInfo spaceInfo = cubeService.getSpaceInfo(cubeInfo.getSpace());
            Cube cube = cubeService.createCube(cubeName, mdbRead);


            // ---
            // Результат расчета кладем в csv
            ICalcResultFlush res = createCalcResult(cubeInfo);
            //
            res.open();


            // ---
            // Расчет
            log.info("Расчет и запись интервала");

            //
            calcCubeCoords(cube, null, intervalDbeg, intervalDend, res, sw);


            // ---
            // Запись остатков
            log.info("Завершение записи интервала");

            // Запись csv в БД
            res.close();


            // ---
            // Статистика
            sw.stop("total");

            // Запись статистики в журнал
            utWorkerLogDb.writeLog_interval(sw, cubeInfo, intervalDbeg, intervalDend);

            //
            log.info("Заполнение куба [" + cubeName + "] выполнено, " +
                    "координат: " + sw.getLong("dirty", "count") + ", " +
                    "записей: " + res.size()
            );

        } catch (Exception e) {
            // Печать ошибки
            log.error("Ошибка при заполнении куба [" + cubeName + "]: " + e.getMessage());
            log.error(StringUtils.getStackTrace(e));
            // Запись ошибки в журнал
            utWorkerLogDb.writeLog_interval_error(cubeName, intervalDbeg, intervalDend, e);
            //
            throw e;
        }
    }


    private ICalcResultFlush createCalcResult(CubeInfo cubeInfo) throws Exception {
        if (true) {
            return createCalcResultCsvDb(cubeInfo);
        } else {
            return createCalcResultDb(cubeInfo);
        }
    }

    private CalcResultStreamDb createCalcResultDb(CubeInfo cubeInfo) throws Exception {
        CalcResultStreamDb res = new CalcResultStreamDb(mdbWrite, cubeInfo);

        return res;
    }

    private CalcResultStreamCsvDb createCalcResultCsvDb(CubeInfo cubeInfo) throws Exception {
        CalcResultStreamCsvDb res = new CalcResultStreamCsvDb(mdbWrite, cubeInfo);

        return res;
    }

    private CoordList getSpaceCoords(Space space, XDate intervalDbeg, XDate intervalDend, MolapStopWatch sw) throws Exception {
        // Статистика (анализ)
        sw.start("dirty");

        //
        CoordList spaceCoords;
        try {
            mdbRead.startTran();
            spaceCoords = space.getCoordsForInterval(intervalDbeg, intervalDend);
        } finally {
            mdbRead.commit();
        }

        // Статистика
        sw.stop("dirty");
        sw.setValuePlus("dirty", "count", spaceCoords.size());

        //
        return spaceCoords;
    }


    private CoordList getDirtyCoords(Cube cube, long auditAgeFrom, long auditAgeTo, MolapStopWatch sw) throws Exception {
        // Статистика (анализ)
        sw.start("dirty");

        //
        CoordList dirtyCoords;
        try {
            mdbRead.startTran();
            dirtyCoords = getDirtyCoordsRecursive(cube, auditAgeFrom, auditAgeTo, sw);
        } finally {
            mdbRead.commit();
        }

        // Статистика
        sw.stop("dirty");

        //
        return dirtyCoords;
    }


    void calcCubeCoords(Cube cube, Iterable<Coord> coords, XDate intervalDbeg, XDate intervalDend, ICalcResultStream res, MolapStopWatch sw) throws Exception {
        // Статистика
        sw.start("calc");

        // Расчет
        try {
            mdbRead.startTran();
            cube.calc(coords, intervalDbeg, intervalDend, res);
        } finally {
            mdbRead.commit();
        }

        // Статистика
        sw.stop("calc");
    }

    void flushCubeResult(CubeInfo cubeInfo, SpaceInfo spaceInfo, CalcResultStreamArray calcResults, MolapStopWatch sw) throws Exception {
/*
        // Статистика
        sw.start("write");

        //
        WriterDb writer = new WriterDb(mdbWrite);

        // Запись
        try {
            mdbWrite.startTran();

            //
            for (CalcResult rec : calcResults) {
                Coord coord = rec.coord;
                Value value = rec.value;

                // Проверим заполненность координат
                for (String coordName : spaceInfo.getCoords()) {
                    if (coord.get(coordName) == null) {
                        throw new XError(String.format("Для куба [%s] значение координаты [%s] == null.", cubeInfo.getName(), coordName));
                    }
                }

                //
                if (value instanceof ValueSingle valueSingle) {
                    writer.updatePoint(coord, valueSingle, cubeInfo, spaceInfo);
                } else if (value instanceof ValueList valueList) {
                    writer.updatePointList(coord, valueList, cubeInfo, spaceInfo);
                } else {
                    throw new XError(String.format("Для куба [%s] значение [%s] неправильного типа.", cubeInfo.getName(), value));
                }
            }

            //
            mdbWrite.commit();
        } catch (Exception e) {
            mdbWrite.rollback();
            throw e;


        } finally {
            sw.stop("write");
        }

        // Статистика
        sw.setValuePlus("write", "count", calcResults.size());

        // Результат копим заново
        calcResults.clear();
*/
    }


    /**
     * Собираем, что нужно пересчитать как непосредственно по кубу cube, так и по влияющим таблицам кубам
     */
    CoordList getDirtyCoordsRecursive(Cube cube, long auditAgeFrom, long auditAgeTo, MolapStopWatch sw) throws Exception {
        CubeInfo cubeInfo = cube.getInfo();

        // Узнаем что нужно пересчитать непосредственно по кубу cube (по влияющим таблицам)
        sw.start("dirty");

        //
        log.info(cubeInfo.getName() + ".getDirtyCoords");
        CoordList dirtyCoords = cube.getDirtyCoords(auditAgeFrom, auditAgeTo);
        log.info(cubeInfo.getName() + ".getDirtyCoords.count = " + dirtyCoords.size());

        //
        sw.setValuePlus("dirty", "count.coords", dirtyCoords.size());
        sw.setValuePlus("dirty", "count.points", UtCoord.sizeFull(dirtyCoords));
        sw.stop("dirty");


        // Узнаем что нужно пересчитать по влияющим кубам (рекурсивно)
        for (String dependsCubeName : cubeInfo.getDependCubes()) {
            // Что считает нужным пересчитать влияющий куб dependsCube
            sw.start("dirty_depends");

            //
            Cube dependsCube = cubeService.createCube(dependsCubeName, mdbRead);
            CoordList dependsDirtyCoords = getDirtyCoordsRecursive(dependsCube, auditAgeFrom, auditAgeTo, new MolapStopWatch());

            //
            sw.setValuePlus("dirty_depends", "count", dependsDirtyCoords.size());
            sw.setValuePlus("dirty_depends", "countFull", UtCoord.sizeFull(dependsDirtyCoords));
            sw.stop("dirty_depends");


            // Сконвертим координаты влияющего куба в координаты нашего куба cube
            sw.start("convert");

            //
            if (dependsDirtyCoords.size() > 0) {
                cube.convertCoords(dependsCubeName, dependsDirtyCoords, dirtyCoords);
            }

            //
            sw.setValuePlus("convert", "count.coords", dirtyCoords.size() - sw.getLong("dirty", "count.coords"));
            sw.setValuePlus("convert", "count.points", UtCoord.sizeFull(dirtyCoords) - sw.getLong("dirty", "count.points"));
            sw.stop("convert");
        }


        //
        return dirtyCoords;
    }

    /**
     * Возвращает список всех кубов в порядке зависимостей.
     * Следит, чтобы все влияющие кубы не были отключены.
     *
     * @return список всех кубов
     * @throws Exception Если обнаружится, что влияющий куб отключен
     */
    List<CubeInfo> getEnabledCubesAll() throws Exception {
        NamedList<CubeInfo> res = new DefaultNamedList<>();

        // Все кубы
        List<CubeInfo> cubesAll = cubeService.getCubes();

        // Формируем результат путем фильтрации отключенных
        for (CubeInfo cubeInfo : cubesAll) {
            if (cubeInfo instanceof CubeConf cubeConf) {
                if (cubeConf.getEnabled()) {
                    // Проверим, что все влияющие не отключены
                    NamedList<CubeInfo> dummy = new DefaultNamedList<>();
                    fillDependCubes(cubeInfo, dummy);

                    // Добавляем в результат
                    res.add(cubeInfo);
                }
            }
        }

        //
        return res;
    }

    /**
     * Возвращает список кубов, перечисленных в строке через запятую.
     * Список кубов отстортирован в порядке зависимостей и дополнен влияющими (при необходимости).
     *
     * @param cubeNames имена кубов через запятую
     * @param cascade   =true - нужно ли собирать зависимые кубы
     * @return отсортированный список кубов
     */
    public List<CubeInfo> parseSortCubeNames(String cubeNames, boolean cascade) throws Exception {
        NamedList<CubeInfo> cubes = new DefaultNamedList<>();

        // Разбираем имена через запятую
        List<String> cubeNamesList = Arrays.stream(cubeNames.split(",")).toList();
        for (String name : cubeNamesList) {
            CubeInfo cubeInfo = cubeService.getCubeInfo(name);
            cubes.add(cubeInfo);
        }

        // Набираем зависимости
        if (cascade) {
            for (String name : cubeNamesList) {
                CubeInfo cubeInfo = cubeService.getCubeInfo(name);
                fillDependCubes(cubeInfo, cubes);
            }
        }

        // Обеспечим порядок (и убираем заодно дубли, если были)
        List<CubeInfo> cubesRes = getSorted(cubes);

        //
        return cubesRes;
    }

    private List<CubeInfo> getSorted(NamedList<CubeInfo> cubes) {
        List<CubeInfo> res = new ArrayList<>();

        for (CubeInfo cube : cubeService.getCubes()) {
            if (cubes.find(cube.getName()) != null) {
                res.add(cube);
            }
        }

        return res;
    }

    /**
     * Получаем список влияющих кубов: каскадно, но в беспорядке
     *
     * @throws Exception Если обнаружится, что влияющий куб отключен
     */
    private void fillDependCubes(CubeInfo cubeInfo, NamedList<CubeInfo> res) throws Exception {
        for (String cubeName_depend : cubeInfo.getDependCubes()) {
            CubeInfo cubeInfo_depend = cubeService.getCubeInfo(cubeName_depend);

            // Куб уже есть в списке?
            if (res.find(cubeInfo_depend.getName()) != null) {
                continue;
            }

            // Куб отключен?
            if (cubeInfo_depend instanceof CubeConf cubeConf) {
                if (!cubeConf.getEnabled()) {
                    throw new XError("Для куба %s отключен зависимый куб: %s", cubeInfo.getName(), cubeInfo_depend.getName());
                }
            }

            // Добавляем в список
            res.add(cubeInfo_depend);

            // Зависимости
            fillDependCubes(cubeInfo_depend, res);
        }
    }

    // ---
    // endregion
    // ---


    /**
     * Обновлятель кубов по расписанию.
     * Отвечает за:
     * - расписание (периодичность запуска каждого куба)
     * - своевременное расширение кубов
     */
/*
    public void run() throws Exception {
        log.info("############################################");
        log.info("Запуск задачи");

        //
        try {
            Model model = getApp().service(ModelService.class).getModel();
            MoService cubeService = model.service(MoService.class);

            //
            Db dbRead = getApp().service(ModelService.class).getModel("default").getDb();
            Db dbWrite = getApp().service(ModelService.class).getModel("write").getDb();
            //
            DbUtils utRead = new DbUtils(dbRead);
            DbUtils utWrite = new DbUtils(dbWrite);
            //
            dbRead.connect();
            dbWrite.connect();

            //
            MoWorker worker = new MoWorker(utRead, utWrite);

            //
            try {
                // -----------------------------------------------
                // Расчет расширения по расписанию
                // -----------------------------------------------
                log.info("Проверка расширения всех кубов");

                XDateTime dateNow = UtDate.clearTime(worker.readDatabaseDt());

                // Кубы в cubeDefs гарантировано отсортированы по зависимостям,
                // поэтому сначала запускаем расчет самых не зависимых кубов
                for (CubeInfo cd : cubeService.getCubeDefs()) {

                    // Создаем нужный куб
                    ICube cube = cubeService.createCube(cd.getName(), utRead);

                    // Конфиг куба
                    CubeInfo cubeDef = cubeService.getCubeDefs().find(cubeInfo.getName());

                    // Период расширения куба
                    int cubeExpandStepMin = 0;
                    int cubeExpandStepMax = 1;
                    if (cubeDef != null) {
                        cubeExpandStepMin = cubeDef.conf.getExpandStepMin();
                        cubeExpandStepMax = cubeDef.conf.getExpandStepMax();
                    }
                    if (cubeExpandStepMin < 0) {
                        cubeExpandStepMin = 0;
                    }
                    if (cubeExpandStepMax < 1) {
                        cubeExpandStepMax = 1;
                    }
                    // Куб включен?
                    boolean cubeEnabled = true;
                    if (cubeDef != null) {
                        cubeEnabled = cubeDef.conf.getEnabled();
                    }

                    // Текущее значение координаты "Дата"
                    XDateTime cubeDtExpand = worker.getExpandDt(cube);

                    // Куб включен и cubeDtExpand некорректная?
                    if (cubeEnabled && cubeDtExpand.compareTo(MoService.cubeDtValid) <= 0) {
                        throw new XError("cubeDtExpand <= " + MoService.cubeDtValid + ", куб: " + cubeInfo.getName());
                    }

                    // Куб включен и пришло время расширять куб?
                    if (cubeEnabled && cubeDtExpand.diffDays(dateNow) <= cubeExpandStepMin) {
                        log.info("Расширяем куб [" + cubeInfo.getName() + "]");

                        // Будущее значение координаты "Дата" - минимум + запас
                        XDateTime dtExpandTo = dateNow.plusDays(cubeExpandStepMin + cubeExpandStepMax);

                        // -----------------------------------------------
                        // Расширяем влияющие кубы
                        // -----------------------------------------------

                        // Получим список влияющих кубов (каскадно)
                        List<CubeInfo> dependCubes = worker.getDependCubesAll(cube);

                        //
                        for (CubeInfo cd_depend : dependCubes) {
                            // Создаем влияющий куб
                            ICube dependsCube = cubeService.createCube(cd_depend.getName(), utRead);

                            // Возраст влияющего куба
                            XDateTime dependsCubeExpandDt = worker.getExpandDt(dependsCube);

                            // Возраст влияющего куба меньше чем будущий возраст нашего куба (dtCalcTo)?
                            if (dependsCubeExpandDt.compareTo(dtExpandTo) < 0) {
                                log.info("Расширяем влияющий куб [" + dependscubeInfo.getName() + "]");

                                // Принудительно расширяем влияющий куб dependsCube до dtExpandTo
                                worker.refreshCubeInterval(dependsCube, dependsCubeExpandDt, dtExpandTo);

                                // Ставим метку о расширении влияющего куба
                                worker.setDtExpand(dependsCube, dtExpandTo);
                            }
                        }

                        // Расширяем сам куб
                        log.info("Расширяем куб [" + cubeInfo.getName() + "]");
                        worker.refreshCubeInterval(cube, cubeDtExpand, dtExpandTo);

                        // Ставим метку о расширении куба
                        worker.setDtExpand(cube, dtExpandTo);
                    }

                }


                // -----------------------------------------------
                // Расчет всех кубов по аудиту
                // -----------------------------------------------
                log.info("Расчет всех кубов по аудиту");

                // Период аудита для расчета - до текущей даты (с сервера)
                XDateTime dtCalcTo = worker.readDatabaseDt();

                //
                worker.refreshCubeAudit_all(dtCalcTo);


            } finally {
                dbRead.disconnect();
                dbWrite.disconnect();
            }


            // -----------------------------------------------
            log.info("Задача завершена");

        } catch (Exception e) {
            log.error("Ошибка при выполнении задачи: " + e.getMessage());
            e.printStackTrace();
            throw e;
        }
    }
*/


    // ---
    // region Утилиты
    // ---
    public void connect() throws Exception {
        //System.out.println("dbRead: " + getDbString(mdbRead));
        //System.out.println("dbWrite: " + getDbString(mdbWrite));

        //
        mdbRead.connect();
        //System.out.println("dbRead.connect - ok");

        mdbWrite.connect();
        //System.out.println("dbWrite.connect - ok");
    }

    public void disconnect() throws Exception {
        mdbRead.disconnect();
        mdbWrite.disconnect();
    }

    String getDbString(Mdb mdb) {
        DbSource dbSource = mdb.getDbSource();
        return mdb.getDbSource().getName() + " [" + dbSource.getDbType() + ":" + dbSource.getProps().get("username") + "@" + dbSource.getProps().get("database") + "]";
    }

    // ---
    // endregion
    // ---


}
