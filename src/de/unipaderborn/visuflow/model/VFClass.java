package de.unipaderborn.visuflow.model;

import soot.SootClass;

public class VFClass {

    protected SootClass wrapped;

    public VFClass(SootClass wrapped) {
        this.wrapped = wrapped;
    }

    public SootClass getSootClass() {
        return wrapped;
    }
}
