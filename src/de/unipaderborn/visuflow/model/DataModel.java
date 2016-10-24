package de.unipaderborn.visuflow.model;

import java.util.List;

public interface DataModel {
	public static final String EA_TOPIC_DATA = "de/unipaderborn/visuflow/DataModel";
	public static final String EA_TOPIC_DATA_MODEL_CHANGED = EA_TOPIC_DATA + "/ModelChanged";
	public static final String EA_TOPIC_DATA_CLASS_ADDED = EA_TOPIC_DATA + "/ClassAdded";
	public static final String EA_TOPIC_DATA_CLASS_CHANGED = EA_TOPIC_DATA + "/ClassChanged";
	public static final String EA_TOPIC_DATA_CLASS_REMOVED = EA_TOPIC_DATA + "/ClassRemoved";
	public static final String EA_TOPIC_DATA_METHOD_ADDED = EA_TOPIC_DATA + "/MethodAdded";
	public static final String EA_TOPIC_DATA_METHOD_CHANGED = EA_TOPIC_DATA + "/MethodChanged";
	public static final String EA_TOPIC_DATA_METHOD_REMOVED = EA_TOPIC_DATA + "/MethodRemoved";
	public static final String EA_TOPIC_DATA_UNIT_ADDED = EA_TOPIC_DATA + "/UnitAdded";
	public static final String EA_TOPIC_DATA_UNIT_CHANGED = EA_TOPIC_DATA + "/UnitChanged";
	public static final String EA_TOPIC_DATA_UNIT_REMOVED = EA_TOPIC_DATA + "/UnitRemoved";

	public static final String EA_TOPIC_DATA_SELECTION = EA_TOPIC_DATA + "/SeclectionChanged";

    public List<VFClass> listClasses();
    public List<VFMethod> listMethods(VFClass vfClass);
    public List<VFUnit> listUnits(VFMethod vfMethod);
    public VFClass getSelectedClass();
    public List<VFMethod> getSelectedClassMethods();
    public List<VFUnit> getSelectedMethodUnits();
    public void setSelectedClass(VFClass selectedClass);
    public void setSelectedMethod(VFMethod selectedMethod);
    public VFMethod getSelectedMethod();
}
