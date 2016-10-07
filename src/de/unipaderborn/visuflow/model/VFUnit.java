package de.unipaderborn.visuflow.model;

import soot.Unit;

public class VFUnit {

    protected Unit wrapped;

    public VFUnit(Unit wrapped) {
        super();
        this.wrapped = wrapped;
    }

    public Unit getUnit() {
        return wrapped;
    }
}
