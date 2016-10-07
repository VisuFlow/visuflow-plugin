package de.unipaderborn.visuflow.model;

import soot.SootMethod;

public class VFMethod {

    protected SootMethod wrapped;

    public VFMethod(SootMethod wrapped) {
        this.wrapped = wrapped;
    }

    public SootMethod getSootMethod() {
        return wrapped;
    }
}
