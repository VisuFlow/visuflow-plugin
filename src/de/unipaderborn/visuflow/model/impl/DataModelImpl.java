package de.unipaderborn.visuflow.model.impl;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.model.graph.ICFGStructure;


public class DataModelImpl implements DataModel {
	
	private List<VFClass> classList;
	
	private VFClass selectedClass;
	private VFMethod selectedMethod;
	
	private List<VFMethod> selectedClassMethods;
	private List<VFUnit> selectedMethodUnits;
	
	private EventAdmin eventAdmin;

    @Override
    public List<VFClass> listClasses() {
        return classList;
    }

    @Override
    public List<VFMethod> listMethods(VFClass vfClass) {
    	List<VFMethod> methods = Collections.emptyList();
		for (VFClass current : classList) {
			if(current == vfClass) {
				methods = vfClass.getMethods();
			}
		}
		return methods;
    }

    @Override
    public List<VFUnit> listUnits(VFMethod vfMethod) {
    	List<VFUnit> units = Collections.emptyList();
		for (VFClass currentClass : classList) {
			for (VFMethod currentMethod : currentClass.getMethods()) {
				if(currentMethod == vfMethod) {
					units = vfMethod.getUnits();
				}
			}
		}
		return units;
    }

	@Override
	public VFClass getSelectedClass() {
		return selectedClass;
	}

	@Override
	public List<VFMethod> getSelectedClassMethods() {
		return selectedClassMethods;
	}

	@Override
	public List<VFUnit> getSelectedMethodUnits() {
		return selectedMethodUnits;
	}

	@Override
	public void setSelectedClass(VFClass selectedClass) {
		this.selectedClass = selectedClass;
		this.selectedMethod = this.selectedClass.getMethods().get(0);
		this.selectedClassMethods = this.selectedClass.getMethods();
		this.populateUnits();
	}

	@Override
	public void setSelectedMethod(VFMethod selectedMethod) {
		this.selectedMethod = selectedMethod;
		this.populateUnits();
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("selectedMethod", selectedMethod);
		properties.put("selectedMethodUnits", selectedMethodUnits);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_SELECTION, properties);
		eventAdmin.postEvent(modelChanged);
	}

	@Override
	public VFMethod getSelectedMethod() {
		return selectedMethod;
	}

	public void setClassList(List<VFClass> classList) {
		this.classList = classList;
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("model", classList);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_MODEL_CHANGED, properties);
		eventAdmin.postEvent(modelChanged);
	}
	
	private void populateUnits() {
		this.selectedMethodUnits = this.selectedMethod.getUnits();
	}
	
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	@Override
	public ICFGStructure getIcfg() {
		// TODO Auto-generated method stub
		return null;
	}

}
