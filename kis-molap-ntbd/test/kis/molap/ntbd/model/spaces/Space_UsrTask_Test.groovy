package kis.molap.ntbd.model.spaces


import kis.molap.model.coord.*
import kis.molap.model.cube.*
import kis.molap.model.util.*
import kis.molap.ntbd.model.base.*
import org.junit.jupiter.api.*

import static org.junit.jupiter.api.Assertions.*

public class Space_UsrTask_Test extends MolapBase_Test {


    String spaceName = "Space_UsrTask"


    @Test
    public void getCoordsForInterval() throws Exception {
        // Создаем пространство
        Space space = createSpace(spaceName, mdbRead)

        // Считаем куб для интервала дат
        CoordList coords = space.getCoordsForInterval(intervalDbeg, intervalDend)

        //
        System.out.println("coords.size: " + coords.size())
        UtMolapPrint.printCoordList(coords, 5);

        //
        assertEquals(true, coords.size() > 0, "Значения есть")
    }


}

