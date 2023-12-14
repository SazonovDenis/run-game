package kis.molap.model.value.impl;

import jandcode.commons.*;
import jandcode.commons.datetime.*;
import jandcode.commons.error.*;
import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.coord.*;
import kis.molap.model.service.*;
import kis.molap.model.util.*;
import kis.molap.model.value.*;
import kis.molap.model.worker.impl.*;

import java.util.*;

public class CalcResultStreamDb implements ICalcResultFlush {

    //
    private int size = 0;
    protected Mdb mdb;
    protected CubeInfo cubeInfo;
    protected SpaceInfo spaceInfo;

    //
    protected MolapStopWatch sw = new MolapStopWatch();

    //
    private long BUFFER_SIZE = 10000;
    //
    private long COMMIT_SIZE = 1000;
    //
    private ArrayList<CalcResult> resultBuffer = new ArrayList<>();

    public CalcResultStreamDb(Mdb mdb, CubeInfo cubeInfo) throws Exception {
        this.mdb = mdb;
        this.cubeInfo = cubeInfo;
        CubeService cubeService = mdb.getModel().bean(CubeService.class);
        this.spaceInfo = cubeService.getSpaceInfo(cubeInfo.getSpace());
    }

    @Override
    public void addValue(CalcResult value) throws Exception {
        size = size + 1;
        resultBuffer.add(value);
        if (resultBuffer.size() > BUFFER_SIZE) {
            flush();
        }
    }

    @Override
    public void open() throws Exception {
    }

    @Override
    public void close() throws Exception {
        flush();
    }

    public int size() {
        return size;
    }

    private void flush() throws Exception {
        sw.start("write");

        WriterDb writer = new WriterDb(mdb);
        try {
            writer.open(cubeInfo);

            //
            mdb.startTran();
            int commitWaitCount = 0;

            //
            for (CalcResult rec : resultBuffer) {
                Coord coord = rec.coord;
                Value value = rec.value;

                // Проверим заполненность координат
                for (String coordName : spaceInfo.getCoords()) {
                    if (coord.get(coordName) == null) {
                        throw new XError(String.format("Для куба [%s] значение координаты [%s] == null, calcResult [%s]", cubeInfo.getName(), coordName, rec));
                    }
                }

                // Проверяем заполненность значения для координаты "дата"
                String dtValueName = UtCoord.getValueDateCoordName(coord);
                if (dtValueName != null) {
                    validateDt((XDate) coord.get(dtValueName));
                }

                // Запись
                if (value instanceof ValueSingle valueSingle) {
                    writer.updatePoint(coord, valueSingle);
                } else if (value instanceof ValueList valueList) {
                    writer.updatePointList(coord, valueList);
                } else {
                    throw new XError(String.format("Для куба [%s] значение [%s] неправильного типа", cubeInfo.getName(), value));
                }

                //
                commitWaitCount = commitWaitCount + 1;
                if (commitWaitCount > COMMIT_SIZE) {
                    mdb.commit();
                    mdb.startTran();
                    commitWaitCount = 0;
                }


                // Статистика
                sw.setValuePlus("write", "count", 1);
            }

            //
            mdb.commit();


            // Результат копим заново
            resultBuffer.clear();

        } catch (Exception e) {
            mdb.rollback(e);

        } finally {
            sw.stop("write");
            writer.close();
        }

    }

    public static void validateDt(XDate dt) {
        if (dt == null) {
            throw new XError("dt == null");
        }
        if (dt.equals(UtDateTime.EMPTY_DATE_END)) {
            throw new XError("dt == EMPTY_DATE_END");
        }
        if (dt.equals(UtDateTime.EMPTY_DATE)) {
            throw new XError("dt == EMPTY_DATE");
        }
    }


}
