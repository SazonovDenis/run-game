package kis.molap.model.cube;

import kis.molap.model.conf.*;

/**
 * Пометка, что объект является кубом.
 */
public interface Cube extends ICalcData, ICalcCoords {

    CubeInfo getInfo();

}
