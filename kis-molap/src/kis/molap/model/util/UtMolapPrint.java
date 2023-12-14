package kis.molap.model.util;

import kis.molap.model.coord.*;
import kis.molap.model.value.*;
import kis.molap.model.value.impl.*;

import java.util.*;

public class UtMolapPrint {


    // ---------------------------------------
    // Утилиты
    // ---------------------------------------

    public static void printCoordList(Iterable<Coord> coords, int maxToPrint) {
        if (coords == null) {
            System.out.println("<null>");
        } else {
            int count = 0;
            for (Coord coord : coords) {
                System.out.println(coord);

                //
                count = count + 1;
                if (count > maxToPrint) {
                    System.out.println("...");
                    break;
                }
            }

            //
            if (count == 0) {
                System.out.println("<empty>");
            }

            //
            if (coords instanceof CoordList coordsList) {
                System.out.println("items count: " + coordsList.size());
            }
        }
    }

    public static void printValuesList(Collection<CalcResult> valuesList, int printCount) {
        if (valuesList.size() == 0) {
            System.out.println("<empty>");
        } else {
            int n = 0;
            for (CalcResult rec : valuesList) {
                Coord coord = rec.coord;
                Value value = rec.value;

                //
                if (value instanceof ValueList) {
                    ValueListImpl valueList = (ValueListImpl) value;
                    System.out.println(coord + ", count: " + valueList.size());
                    printValueList(valueList);
                } else {
                    ValueSingleImpl valueSingle = (ValueSingleImpl) value;
                    System.out.println(coord + " = " + valueSingle.getValues());
                }

                //
                n = n + 1;
                if (n > printCount) {
                    System.out.println("...");
                    break;
                }
            }
            System.out.println("items count: " + valuesList.size());
        }
    }


    public static void printValueList(ValueList valueList) {
        if (valueList == null) {
            System.out.println("valueList == null");
            return;
        }

        for (int i = 0; i < valueList.size(); i++) {
            ValueSingle valueRec = valueList.get(i);
            System.out.println("  " + valueRec.getValues().toString());
        }

        System.out.println("valueList.size: " + valueList.size());
    }

    public static void printMapList(List<Map> valuesList) {
        if (valuesList == null) {
            System.out.println("valuesList == null");
            return;
        }

        for (int i = 0; i < valuesList.size(); i++) {
            Map valueRec = valuesList.get(i);
            System.out.println("  " + valueRec.toString());
        }

        System.out.println("items count: " + valuesList.size());
    }

    public static String fileSizeStr(long size) {
        if (size < 1024) {
            return size + " bytes";
        } else if (size < 1024 * 1024) {
            return (10 * Math.round(size / 1024) / 10) + " Kb";
        } else if (size < 1024 * 1024 * 1024) {
            return (10 * Math.round(size / 1024 / 1024) / 10) + " Mb";
        } else {
            return (10 * Math.round(size / 1024 / 1024 / 1024) / 10) + " Gb";
        }
    }

}