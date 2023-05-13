package run.game.util;

import jandcode.commons.*;
import jandcode.core.dbm.mdb.*;
import jandcode.core.store.*;

import java.io.*;

/**
 * Чтение и запись из csv нашего формата
 * Разделитель - tab.
 */
public class RgmCsvUtils extends BaseMdbUtils {


    public void addFromCsv(Store store, String fileName) throws Exception {
        StoreService svcStore = getModel().getApp().bean(StoreService.class);
        StoreLoader ldr = svcStore.createStoreLoader("csv.rgm");
        ldr.setStore(store);
        ldr.load().fromFileObject(fileName);
    }


    public void addFromCsv(Store store, String fileName, String loader) throws Exception {
        StoreService svcStore = getModel().getApp().bean(StoreService.class);
        StoreLoader ldr = svcStore.createStoreLoader(loader);
        ldr.setStore(store);
        ldr.load().fromFileObject(fileName);
    }


    public void saveToCsv(Store store, File file) throws Exception {
        String delim = "\t";

        StringBuilder res = new StringBuilder();

        for (StoreField f : store.getFields()) {
            if (f.getIndex() != 0) {
                res.append(delim);
            }
            res.append(f.getName());
        }
        res.append("\r\n");

        for (StoreRecord rec : store) {
            for (StoreField f : store.getFields()) {
                if (f.getIndex() != 0) {
                    res.append(delim);
                }
                if (rec.isValueNull(f.getName())) {
                } else {
                    Object value = rec.getValue(f.getName());
                    if (value instanceof String valueStr) {
                        //res.append("\"");
                        //res.append(valueStr.replace("\"", "\\\""));
                        res.append(valueStr.replace("\t", "\\t"));
                        //res.append("\"");
                    } else if (value instanceof byte[]) {
                        res.append(UtCnv.toString(value).replace("\r", "").replace("\n", ""));
                    } else {
                        res.append(UtCnv.toString(value));
                    }
                }
            }
            res.append("\r\n");

        }

        //
        UtFile.saveString(res.toString(), file);
    }


}
