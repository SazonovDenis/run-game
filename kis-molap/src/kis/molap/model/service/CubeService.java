package kis.molap.model.service;

import jandcode.core.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.cube.*;

import java.util.*;

/**
 * Хранилище алгоритмов расчета (кубов).
 * Фабрика кубов, знает про все кубы и их свойства.
 */
public interface CubeService extends Comp, IModelMember {

    CubeInfo getCubeInfo(String name) throws Exception;

    SpaceInfo getSpaceInfo(String name) throws Exception;

    Cube createCube(String name, Mdb mdb) throws Exception;

    Space createSpace(String name, Mdb mdb) throws Exception;

    /**
     * Список зарегистрированных кубов.
     * Список сортирован: первые элементы - кубы без зависимостей, потом зависимые от них и т.д.
     */
    List<CubeInfo> getCubes();

    /**
     * Список зарегистрированных пространств
     */
    List<SpaceInfo> getSpaces();

}
