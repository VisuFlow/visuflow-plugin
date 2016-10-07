package de.unipaderborn.visuflow.model.impl;

import java.util.Collections;
import java.util.List;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;


public class DataModelImpl implements DataModel {

    @Override
    public List<VFClass> listClasses() {
        return Collections.emptyList();
    }

    @Override
    public List<VFMethod> listMethods(VFClass vfClass) {
        return Collections.emptyList();
    }

    @Override
    public List<VFUnit> listUnits(VFMethod vfMethod) {
        return Collections.emptyList();
    }

}
