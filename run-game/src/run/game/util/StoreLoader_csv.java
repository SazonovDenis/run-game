package run.game.util;

import jandcode.commons.error.*;
import jandcode.core.store.*;
import jandcode.core.store.std.*;
import org.apache.commons.csv.*;

import java.io.*;


/**
 * Загрузка из csv нашего формата.
 */
public class StoreLoader_csv extends BaseStoreLoader {

    public void doLoad(Reader reader, Store store, boolean createStruct) throws Exception {

        CSVFormat format = CSVFormat.newFormat('\t');

        CSVFormat.Builder b = format.builder();
        b.setNullString(StoreConsts.NULL_STRING_VALUE);
        b.setTrim(true);
        b.setIgnoreSurroundingSpaces(true);
        CSVFormat fmt = b.build();

        try (CSVParser prs = fmt.parse(reader)) {

            boolean firstLine = true;

            StoreField[] fields = null;

            for (CSVRecord csvRec : prs) {
                if (firstLine) {
                    firstLine = false;
                    fields = new StoreField[csvRec.size()];
                    for (int i = 0; i < fields.length; i++) {
                        if (createStruct) {
                            fields[i] = store.addField(csvRec.get(i), "string");
                        } else {
                            fields[i] = store.findField(csvRec.get(i));
                        }
                    }
                } else {
                    int cnt = csvRec.size();
                    if (cnt > fields.length) {
                        throw new XError("В строке {0} указано {1} значений, а должно быть не более {2}",
                                csvRec.getRecordNumber(), cnt, fields.length);
                    }
                    StoreRecord storeRec = store.add();
                    for (int i = 0; i < cnt; i++) {
                        StoreField f = fields[i];
                        if (f == null) {
                            continue;
                        }
                        storeRec.setValue(f.getIndex(), csvRec.get(i));
                    }
                }
            }
        }

    }

}
