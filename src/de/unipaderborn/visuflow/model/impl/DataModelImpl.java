package de.unipaderborn.visuflow.model.impl;

import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.model.graph.ICFGStructure;
import soot.SootMethod;
import soot.Unit;


public class DataModelImpl implements DataModel {

    private List<VFClass> classList;

    private VFClass selectedClass;
    private VFMethod selectedMethod;

    private List<VFMethod> selectedClassMethods;
    private List<VFUnit> selectedMethodUnits;

    private EventAdmin eventAdmin;

    private ICFGStructure icfg;

    @Override
    public List<VFClass> listClasses() {
        if(classList == null){
            return Collections.emptyList();
        }
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
        if(selectedClassMethods == null){
            return Collections.emptyList();
        }
        return selectedClassMethods;
    }

    @Override
    public List<VFUnit> getSelectedMethodUnits() {
        if(selectedMethodUnits == null){
            return Collections.emptyList();
        }
        return selectedMethodUnits;
    }

    @Override
    public void setSelectedClass(VFClass selectedClass) {
        this.selectedClass = selectedClass;
        this.selectedMethod = this.selectedClass.getMethods().get(0);
        this.selectedClassMethods = this.selectedClass.getMethods();
        this.populateUnits();
        this.setSelectedMethod(this.selectedClass.getMethods().get(1));
    }

    @Override
    public void setSelectedMethod(VFMethod selectedMethod) {
        this.selectedMethod = selectedMethod;
        this.populateUnits();
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("selectedMethod", selectedMethod);
        properties.put("selectedClassMethods", selectedClassMethods);
        properties.put("selectedMethodUnits", selectedMethodUnits);
        Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_SELECTION, properties);
        eventAdmin.postEvent(modelChanged);
    }

    @Override
    public VFMethod getSelectedMethod() {
        return selectedMethod;
    }

    @Override
    public void setClassList(List<VFClass> classList) {
        this.classList = classList;
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("model", classList);
        properties.put("icfg", icfg);
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
        return icfg;
    }

    @Override
    public void setIcfg(ICFGStructure icfg) {
        this.icfg = icfg;
        System.out.println("ICFG " + icfg);
        System.out.println("ICFG size " + icfg.listEdges.size());
    }

    @Override
    public VFMethod getVFMethodByName(SootMethod method) {
        // TODO Auto-generated method stub
        VFClass methodIncludingClass = null;
        String className = method.getDeclaringClass().getName();
        List<VFClass> classes = listClasses();
        Iterator<VFClass> classIterator = classes.iterator();
        while(classIterator.hasNext())
        {
            VFClass temp = classIterator.next();
            if(temp.getSootClass().getName().contentEquals(className))
            {
                methodIncludingClass = temp;
                break;
            }
        }
        System.out.println("inside VFMethodByName");

        Iterator<VFMethod> methodListIterator = listMethods(methodIncludingClass).iterator();
        while(methodListIterator.hasNext())
        {
            VFMethod temp = methodListIterator.next();
            if(temp.getSootMethod().getSignature().contentEquals(method.getSignature()))
            {
                System.out.println("selected method " + temp);
                System.out.println("size of cfg " + temp.getControlFlowGraph().listEdges.size());
                return temp;
            }
        }
        return null;
    }

    @Override
    public void setInSet(String unitFqn, String name, String value) {
        System.out.println("in-set " + name + " " + value);
        VFUnit vfUnit = getVFUnit(unitFqn);
        if(vfUnit != null) {
            System.out.println("Found VFUnit " + vfUnit);
            vfUnit.setInSet(value);
            fireUnitChanged(vfUnit);
        }
    }

    @Override
    public void setOutSet(Unit unit, String name, String value) {
        System.out.println("out-set " + name + " " + value);
        VFUnit vfUnit = getVFUnit("TODO"); // TODO
        if(vfUnit != null) {
            System.out.println("Found VFUnit " + vfUnit);
            vfUnit.setOutSet(value);
            fireUnitChanged(vfUnit);
        }
    }

    /*
     * This is a naive implementation, we might need a faster data structure for this
     */
    private VFUnit getVFUnit(String fqn) {
        VFUnit result = null;
        for (VFClass vfClass : classList) {
            for (VFMethod vfMethod : vfClass.getMethods()) {
                for (VFUnit vfUnit : vfMethod.getUnits()) {
                    if(vfUnit.getFullyQualifiedName().equals(fqn)) {
                        result = vfUnit;
                    }
                }
            }
        }
        return result;
    }

    private void fireUnitChanged(VFUnit unit) {
        Dictionary<String, Object> properties = new Hashtable<>();
        properties.put("unit", unit);
        Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_UNIT_CHANGED, properties);
        eventAdmin.postEvent(modelChanged);
    }

}
