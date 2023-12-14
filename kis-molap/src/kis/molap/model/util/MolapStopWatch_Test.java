package kis.molap.model.util;

import jandcode.commons.*;
import jandcode.commons.test.*;
import org.junit.jupiter.api.*;

import java.io.*;

public class MolapStopWatch_Test extends Base_Test {

    @Test
    public void test() throws Exception {
        MolapStopWatch sw = new MolapStopWatch();

        //
        sw.start("step1");
        Thread.sleep(200);

        //
        for (int i = 0; i < 5; i++) {
            sw.start("step 1.1");
            Thread.sleep(100);
            sw.stop("step 1.1")
            ;
            sw.start("step 1.2");
            Thread.sleep(50);
            sw.stop("step 1.2");
        }

        //
        sw.stop("step1");

        //
        sw.printItems();


        //
        System.out.println(sw.getStopWatchItems());
        UtFile.saveString(sw.getStopWatchItems().toString(), new File("temp/StopWatchItems.txt"));
        System.out.println(UtJson.toJson(sw.getStopWatchItems()));
        UtFile.saveString(UtJson.toJson(sw.getStopWatchItems()), new File("temp/StopWatchItems.json"));
    }


}
