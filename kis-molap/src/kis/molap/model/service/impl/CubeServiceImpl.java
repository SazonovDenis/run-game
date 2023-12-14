package kis.molap.model.service.impl;

import jandcode.commons.*;
import jandcode.commons.conf.*;
import jandcode.commons.error.*;
import jandcode.commons.named.*;
import jandcode.core.*;
import jandcode.core.dbm.*;
import jandcode.core.dbm.mdb.*;
import kis.molap.model.conf.*;
import kis.molap.model.conf.impl.*;
import kis.molap.model.cube.*;
import kis.molap.model.cube.impl.*;
import kis.molap.model.service.*;

import java.util.*;


/**
 * Менеджер/фабрика кубов
 */
public class CubeServiceImpl extends BaseModelMember implements CubeService {


    // Храним список зарегистрированных кубов
    // Список сортирован: первые элементы - кубы без зависимостей, потом зависимые от них и т.д.
    private List<CubeInfo> cubesInfo = new ArrayList<>();

    // Храним список зарегистрированных Space-ов
    private List<SpaceInfo> spacesInfo = new ArrayList<>();


    @Override
    protected void onConfigure(BeanConfig cfg) throws Exception {
        super.onConfigure(cfg);

        //
        loadCubes();
    }


    /**
     * {@link CubeService}
     */

    @Override
    public CubeInfo getCubeInfo(String name) {
        for (CubeInfo cubeInfo : cubesInfo) {
            if (cubeInfo.getName().equals(name)) {
                return cubeInfo;
            }
        }
        throw new XError("Куб [{0}] не найден", name);
    }

    @Override
    public SpaceInfo getSpaceInfo(String name) {
        for (SpaceInfo spaceInfo : spacesInfo) {
            if (spaceInfo.getName().equals(name)) {
                return spaceInfo;
            }
        }
        throw new XError("Пространство [{0}] не найдено", name);
    }

    @Override
    public Cube createCube(String name, Mdb mdb) {
        CubeInfo cubeInfo = getCubeInfo(name);
        if (cubeInfo == null) {
            throw new XError("Куб [{0}] не найден", name);
        }

        //
        CubeCustom cube = (CubeCustom) UtClass.createInst(cubeInfo.getClassName());

        //
        cube.setInfo(cubeInfo);

        //
        cube.setMdb(mdb);

        //
        return cube;
    }

    @Override
    public Space createSpace(String name, Mdb mdb) {
        SpaceInfo spaceInfo = getSpaceInfo(name);
        if (spaceInfo == null) {
            throw new XError("Пространство [{0}] не найдено", name);
        }

        //
        SpaceCustom space = (SpaceCustom) UtClass.createInst(spaceInfo.getClassName());

        //
        space.setInfo(spaceInfo);

        //
        space.setMdb(mdb);

        //
        return space;
    }

    @Override
    public List<CubeInfo> getCubes() {
        return cubesInfo;
    }

    @Override
    public List<SpaceInfo> getSpaces() {
        return spacesInfo;
    }


    /**
     * Загружает список кубов и готовит его к работе.
     */
    private void loadCubes() {
        Conf conf = getModel().getConf();

        //
        Conf confCubes = Conf.create();
        //
        Conf cConf = conf.findConf("cube");
        if (cConf != null) {
            Conf xConf = Conf.create();
            xConf.setValue("cube", cConf);
            ConfExpander expCube = UtConf.createExpander(xConf);
            confCubes = expCube.expand("cube");
        }

        Conf confSpaces = Conf.create();
        Conf sConf = conf.findConf("space");
        if (sConf != null) {
            Conf xConf = Conf.create();
            xConf.setValue("space", sConf);
            ConfExpander expSpace = UtConf.createExpander(xConf);
            confSpaces = expSpace.expand("space");
        }

        // ---
        // Список зарегистрированных Space-ов
        for (Conf confSpace : confSpaces.getConfs()) {
            SpaceInfo spaceInfo = null;
            try {
                spaceInfo = getModel().create(confSpace, SpaceInfoImpl.class, false);
            } catch (Exception e) {
                throw new XErrorMark(e, "confSpace: " + confSpace.origin());
            }
            spacesInfo.add(spaceInfo);
        }

        // ---
        // Список зарегистрированных кубов
        for (Conf confCube : confCubes.getConfs()) {
            CubeInfo cubeInfo = null;
            try {
                cubeInfo = getModel().create(confCube, CubeInfoImpl.class, false);
            } catch (Exception e) {
                throw new XErrorMark(e, "confCube: " + confCube.origin());
            }
            cubesInfo.add(cubeInfo);
        }

        // Отсортируем список кубов по иерархии зависимостей
        sortByDepends(cubesInfo);
    }


    /**
     * Сортирует список по зависимостям.
     * Список кубов должен быть полным, разрывы в зависимостях не допускаются.
     *
     * @param cubeInfos сортируемый список
     */
    private void sortByDepends(List<CubeInfo> cubeInfos) {
        NamedList<CubeInfo> cubeDefsSorted = new DefaultNamedList<>();

        boolean wasAdded;
        while (true) {
            // если после очередного прохода цикла элементов не будет добавлено, значит ловить больше нечего
            wasAdded = false;

            // Пытаемся добавить в отсортированный список (cubeDefsSorted) элементы из исходного
            for (CubeInfo cube : cubeInfos) {

                // Элемент еще не добавлен?
                if (cubeDefsSorted.find(cube.getName()) == null) {
                    // Пытаемся добавить, но не добавляем элемент,
                    // имеющий ссылки на элементы, еще не добавленные в список
                    boolean canAdd = true;
                    for (String dependCube : cube.getDependCubes()) {
                        if (cubeDefsSorted.find(dependCube) == null) {
                            canAdd = false;
                            break;
                        }
                    }
                    if (canAdd) {
                        cubeDefsSorted.add(cube);
                        // отмечаем факт добавления
                        wasAdded = true;
                    }
                }
            }

            if (!wasAdded) {
                break;
            }
        }

        // Если не все отсортировалось, то найдем пример косячника
        if (cubeDefsSorted.size() < cubeInfos.size()) {
            String badCubeName = "";
            String badCubeDepend = "";
            boolean badFound = true;
            for (CubeInfo def : cubeInfos) {

                // Элемент еще не добавлен?
                if (cubeDefsSorted.find(def.getName()) == null) {
                    // Пытаемся найти ссылки на элементы, еще не добавленные в список
                    for (String cubeDepend : def.getDependCubes()) {
                        if (cubeDefsSorted.find(cubeDepend) == null) {
                            badCubeName = def.getName();
                            badCubeDepend = cubeDepend;
                            badFound = false;
                            break;
                        }
                    }
                }
                //
                if (!badFound) {
                    break;
                }
            }

            throw new XError("Невозможно построить зависимости: куб {0} ссылается на {1}", badCubeName, badCubeDepend);
        }

        //
        cubeInfos.clear();
        cubeInfos.addAll(cubeDefsSorted);
    }


}
