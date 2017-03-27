package de.unipaderborn.visuflow.model;

import java.util.List;

import de.unipaderborn.visuflow.model.graph.ICFGStructure;
import soot.SootMethod;


/**
 * This class provides an interface of the DataModel.
 * @author Shashank B S
 *
 */
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
	public static final String EA_TOPIC_DATA_VIEW_REFRESH = EA_TOPIC_DATA + "/refresh";

	public static final String EA_TOPIC_DATA_SELECTION = EA_TOPIC_DATA + "/SelectionChanged";

	/**
	 * Returns the ICFG.
	 * @return - ICFG of the target
	 */
	public ICFGStructure getIcfg();
	/**
	 * Lists all of the classes from the model.
	 * @return list of all the classes of the target code
	 */
	public List<VFClass> listClasses();
	/**
	 * Returns all the methods of a given class.
	 * @param vfClass - class
	 * @return list of all methods 
	 */
	public List<VFMethod> listMethods(VFClass vfClass);
	/**
	 * Returns all the units of a given method.
	 * @param vfMethod - method
	 * @return list of all units
	 */
	public List<VFUnit> listUnits(VFMethod vfMethod);
	public VFUnit getVFUnit(String fqn);
	/**
	 * Updates the model to hold the updated class list and triggers the {@value #EA_TOPIC_DATA_MODEL_CHANGED} event.
	 * @param classList list of updated classes
	 * @author Shashank B S
	 */
	public void setClassList(List<VFClass> classList);
	/**
	 * Returns the currently selected class.
	 * @return currently selected class
	 * @author Shashank B S
	 */
	public VFClass getSelectedClass();
	/**
	 * Returns the list of methods of the currently selected class.
	 * @return list of selected class methods
	 */
	public List<VFMethod> getSelectedClassMethods();
	/**
	 * Returns the list of units of the currently selected method.
	 * @return list of units of the currently selected method
	 * @author Shashank B S
	 */
	public List<VFUnit> getSelectedMethodUnits();
	/**
	 * Updates the currently selected class, sets the currently selected method to the first method of the class and triggers the {@link #EA_TOPIC_DATA_SELECTION} with the selected class.
	 * @param selectedClass
	 * @author Shashank B S
	 */
	public void setSelectedClass(VFClass selectedClass);
	/**
	 * Updates the currently selected method and triggers the {@link #EA_TOPIC_DATA_SELECTION} event with the selected method and panToNode flag.
	 * @param selectedMethod - the method to be selected
	 * @param panToNode flag to determine whether the graph needs to be panned
	 * @author Shashank B S
	 */
	public void setSelectedMethod(VFMethod selectedMethod, boolean panToNode);
	/**
	 * Triggers the {@link #EA_TOPIC_DATA_FILTER_GRAPH} event with the nodesToFilter, selection, panToNode, uiClassName properties.
	 * @param nodesToFilter nodes to be filtered on the graph
	 * @param selection flag to determine whether the nodes have to be highlighted
	 * @param panToNode flag to determine whether the graph has to be panned to the filtered node
	 * @param uiClassName css class to be set on the filtered nodes
	 * @author Shashank B S
	 */
	public void filterGraph(List<VFNode> nodesToFilter, boolean selection, boolean panToNode, String uiClassName);
	/**
	 * Returns the currently selected method.
	 * @return currently selected method
	 * @author Shashank B S
	 */
	public VFMethod getSelectedMethod();
	/**
	 * Returns the {@code VFMethod} of the corresponding {@code SootMethod} 
	 * @param method
	 * @return
	 * @author Shashank B S
	 */
	public VFMethod getVFMethodByName(SootMethod method);
	/**
	 * Sets the <b>ICFG</b> of the model.
	 * @param icfg
	 * @author Shashank B S
	 */
	public void setIcfg(ICFGStructure icfg);
	/**
	 * Trigger a project rebuild explicitly after changes to the model.
	 * @author Shashank B S
	 */
	public void triggerProjectRebuild();
	public void refreshView();

	public void setInSet(String unitFqn, String name, String value);
	public void setOutSet(String unitFqn, String name, String value);
	public void HighlightJimpleUnit(VFNode node);
}
