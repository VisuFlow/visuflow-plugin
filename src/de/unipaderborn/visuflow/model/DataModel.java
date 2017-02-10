package de.unipaderborn.visuflow.model;

import java.util.List;

import de.unipaderborn.visuflow.model.graph.ICFGStructure;
import soot.SootMethod;


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
	public static final String EA_TOPIC_DATA_FILTER_GRAPH = EA_TOPIC_DATA + "/UnitFiltered";

	public static final String EA_TOPIC_DATA_SELECTION = EA_TOPIC_DATA + "/SelectionChanged";

	public ICFGStructure getIcfg();
	public List<VFClass> listClasses();
	public List<VFMethod> listMethods(VFClass vfClass);
	public List<VFUnit> listUnits(VFMethod vfMethod);
	public void setClassList(List<VFClass> classList);
	public VFClass getSelectedClass();
	public List<VFMethod> getSelectedClassMethods();
	public List<VFUnit> getSelectedMethodUnits();
	public void setSelectedClass(VFClass selectedClass);
	public void setSelectedMethod(VFMethod selectedMethod, boolean panToNode);
	public void filterGraph(List<VFNode> nodesToFilter, boolean selection) throws Exception;
	public VFMethod getSelectedMethod();
	public VFMethod getVFMethodByName(SootMethod method);
	public void setIcfg(ICFGStructure icfg);

	public void setInSet(String unitFqn, String name, String value);
	public void setOutSet(String unitFqn, String name, String value);
	public void HighlightJimpleUnit(VFNode node);
}
