package de.unipaderborn.visuflow.model;

import soot.Unit;

public class VFUnit {

    protected Unit wrapped;
    
    private Object inSet;
    private Object outSet;

    public VFUnit(Unit wrapped) {
        this.wrapped = wrapped;
    }

    public Unit getUnit() {
        return wrapped;
    }

	public Object getInSet() {
		return inSet;
	}

	public void setInSet(Object inSet) {
		this.inSet = inSet;
	}

	public Object getOutSet() {
		return outSet;
	}

	public void setOutSet(Object outSet) {
		this.outSet = outSet;
	}
    
}
