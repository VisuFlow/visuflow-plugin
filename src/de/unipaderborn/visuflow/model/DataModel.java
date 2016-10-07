package de.unipaderborn.visuflow.model;

import java.util.List;

public interface DataModel {

    public List<VFClass> listClasses();
    public List<VFMethod> listMethods(VFClass vfClass);
    public List<VFUnit> listUnits(VFMethod vfMethod);
}
