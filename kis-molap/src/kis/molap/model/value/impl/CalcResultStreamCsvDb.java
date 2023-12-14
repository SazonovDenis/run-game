package kis.molap.model.value.impl;

import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.service.*;
import kis.molap.model.util.*;
import kis.molap.model.value.*;
import kis.molap.model.worker.impl.*;

import java.io.*;

public class CalcResultStreamCsvDb implements ICalcResultFlush {


    //
    protected Mdb mdb;
    protected CubeInfo cubeInfo;
    protected SpaceInfo spaceInfo;

    CalcResultStreamFileCsv calcResultStreamFileCsv;


    public CalcResultStreamCsvDb(Mdb mdb, CubeInfo cubeInfo) throws Exception {
        this.mdb = mdb;
        this.cubeInfo = cubeInfo;
        CubeService cubeService = mdb.getModel().bean(CubeService.class);
        this.spaceInfo = cubeService.getSpaceInfo(cubeInfo.getSpace());
        //
        this.calcResultStreamFileCsv = new CalcResultStreamFileCsv(cubeInfo, spaceInfo);
        //
        this.calcResultStreamFileCsv.delimiter = "\t";
        this.calcResultStreamFileCsv.doHeaders = false;
        String tempDir = System.getProperty("java.io.tmpdir");
        this.calcResultStreamFileCsv.file = new File(tempDir + "/" + cubeInfo.getName() + ".csv");
    }

    @Override
    public void addValue(CalcResult value) throws Exception {
        calcResultStreamFileCsv.addValue(value);
    }

    @Override
    public void open() throws Exception {
        calcResultStreamFileCsv.open();
    }


    @Override
    public void close() throws Exception {
        calcResultStreamFileCsv.close();
        //
        System.out.println(calcResultStreamFileCsv);

        // Запись csv в БД
        // Для пероначального заполнения можно использовать insertFromCsv,
        // а при обновлении - insertOrUpdateFromCsv.
        WriterDb writer = new WriterDb(mdb);
        //writer.insertFromCsv(cubeInfo, calcResultStreamFileCsv.file);
        writer.insertOrUpdateFromCsv(cubeInfo, calcResultStreamFileCsv.file);
    }


    @Override
    public int size() {
        return calcResultStreamFileCsv.size();
    }


}
