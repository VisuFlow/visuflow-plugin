package de.unipaderborn.visuflow.model.impl;

import java.util.Collections;
import java.util.List;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.model.graph.ICFGStructure;


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

	@Override
	public VFClass getSelectedClass() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VFMethod> getSelectedClassMethods() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VFUnit> getSelectedMethodUnits() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public void setSelectedClass(VFClass selectedClass) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void setSelectedMethod(VFMethod selectedMethod) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public VFMethod getSelectedMethod() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public ICFGStructure getIcfg() {
		// TODO Auto-generated method stub
		return null;
	}

}
