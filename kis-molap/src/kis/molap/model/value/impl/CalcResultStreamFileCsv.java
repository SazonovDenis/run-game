package kis.molap.model.value.impl;

import jandcode.commons.datetime.*;
import jandcode.commons.datetime.impl.*;
import kis.molap.model.conf.*;
import kis.molap.model.coord.*;
import kis.molap.model.util.*;
import kis.molap.model.value.*;

import java.io.*;
import java.nio.charset.*;
import java.util.*;

public class CalcResultStreamFileCsv implements ICalcResultFlush {


    //
    public File file;
    protected CubeInfo cubeInfo;
    protected SpaceInfo spaceInfo;

    //
    private int size = 0;
    private Writer writer;
    private FileOutputStream stream;

    //
    protected MolapStopWatch sw = new MolapStopWatch();

    //
    private long BUFFER_SIZE = 10000;
    //
    private List<CalcResult> resultBuffer = new ArrayList<>();

    //
    public String delimiter = ",";
    //
    public boolean doHeaders = true;


    public CalcResultStreamFileCsv(CubeInfo cubeInfo, SpaceInfo spaceInfo) {
        this.cubeInfo = cubeInfo;
        this.spaceInfo = spaceInfo;
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
        if (file == null) {
            file = File.createTempFile(cubeInfo.getName() + "-", ".csv");
            //file.deleteOnExit();
        }
        //
        stream = new FileOutputStream(file);
        writer = new OutputStreamWriter(stream, StandardCharsets.UTF_8);
        //
        if (doHeaders) {
            initCsvHeader();
        }
    }


    @Override
    public void close() throws Exception {
        flush();
        writer.close();
        stream.close();
    }


    @Override
    public int size() {
        return size;
    }


    public List<CalcResult> getBuffer() {
        return resultBuffer;
    }


    public String toString() {
        try {
            return "recs: " + size() + ", file: " + file.getCanonicalPath() + ", fileSize: " + UtMolapPrint.fileSizeStr(file.length());
        } catch (IOException e) {
            return "file: <not found>" + file.getAbsolutePath();
        }
    }

    private void flush() throws Exception {
        sw.start("write");

        //
        for (CalcResult rec : resultBuffer) {
            if (rec.value instanceof ValueList valueList) {
                for (int i = 0; i < valueList.size(); i++) {
                    ValueSingle valueSingle = valueList.get(i);
                    flushCoord(rec.coord);
                    flushValueSingle(valueSingle);
                    writer.write("\r");
                }
            } else {
                ValueSingle valueSingle = (ValueSingle) rec.value;
                flushCoord(rec.coord);
                flushValueSingle(valueSingle);
                writer.write("\r");
            }

            //
            sw.setValuePlus("write", "count", 1);
        }

        //
        resultBuffer.clear();

        //
        sw.stop("write");
    }

    private void flushCoord(Coord coord) throws IOException {
        boolean isFirstCoord = true;
        for (String coordName : spaceInfo.getCoords()) {
            if (!isFirstCoord) {
                writer.write(delimiter);
            }
            isFirstCoord = false;
            //
            String text = coord.get(coordName).toString();
            writer.write(text);
        }
    }

    private void flushValueSingle(ValueSingle valueSingle) throws IOException {
        for (String fieldName : cubeInfo.getFields()) {
            if (spaceInfo.getCoords().contains(fieldName)) {
                continue;
            }
            //
            writer.write(delimiter);
            //
            String text = getText(valueSingle.get(fieldName));
            text = text.replace(",", "\\,");
            writer.write(text);
        }
    }


    /**
     * Сериализация значения
     */
    private String getText(Object value) {
        if (value == null) {
            return "\\N";
        } else if (value instanceof XDate valueDate) {
            return valueDate.toString(new XDateTimeFormatterImpl("YYYY-MM-DD")) + " 00:00:00";
        } else if (value instanceof XDateTime valueDateTime) {
            return valueDateTime.toString(new XDateTimeFormatterImpl("YYYY-MM-DD hh:mm:ss"));
        } else {
            return value.toString();
        }
    }


    private void initCsvHeader() throws IOException {
        boolean isFirst = true;
        for (String coordName : spaceInfo.getCoords()) {
            if (!isFirst) {
                writer.write(delimiter);
            }
            isFirst = false;
            //
            writer.write(coordName);
        }

        //
        for (String fieldName : cubeInfo.getFields()) {
            if (spaceInfo.getCoords().contains(fieldName)) {
                continue;
            }
            //
            writer.write(delimiter);
            //
            writer.write(fieldName);
        }

        //
        writer.write("\r");
    }


}
