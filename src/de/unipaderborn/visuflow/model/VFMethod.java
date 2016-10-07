package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.List;

import soot.SootMethod;
import soot.Unit;

public class VFMethod {

    protected SootMethod wrapped;
    private List<VFUnit> units = new ArrayList<>();

    public VFMethod(SootMethod wrapped) {
        this.wrapped = wrapped;
		for (Unit unit : wrapped.getActiveBody().getUnits()) {
			units.add(new VFUnit(unit));
		}
    }

    public SootMethod getSootMethod() {
        return wrapped;
    }
    
    public List<VFUnit> getUnits() {
		return units;
	}    
}
