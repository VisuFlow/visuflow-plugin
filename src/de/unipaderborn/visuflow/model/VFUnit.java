package de.unipaderborn.visuflow.model;

import soot.Unit;

public class VFUnit {

    protected Unit wrapped;

    public VFUnit(Unit wrapped) {
        this.wrapped = wrapped;
    }

    public Unit getUnit() {
        return wrapped;
    }
}
