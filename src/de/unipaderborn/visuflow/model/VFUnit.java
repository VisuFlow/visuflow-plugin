package de.unipaderborn.visuflow.model;

import soot.Unit;
import soot.tagkit.Tag;

public class VFUnit {

    private String fullyQualifiedName;
    protected Unit wrapped;

    private Object inSet;
    private Object outSet;

    public VFUnit(Unit wrapped) {
        this.wrapped = wrapped;
        setFullyQualifiedName(wrapped);
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

    private void setFullyQualifiedName(Unit u) {
        Tag fqn = u.getTag("Fully Qualified Name");
        if (fqn != null) {
            String fullyQualifiedName = new String(fqn.getValue());
            this.fullyQualifiedName = fullyQualifiedName;
        } else {
            System.out.println("WARN fqn tag not found on unit " + u);
        }
    }

    public String getFullyQualifiedName() {
        return fullyQualifiedName;
    }

}
