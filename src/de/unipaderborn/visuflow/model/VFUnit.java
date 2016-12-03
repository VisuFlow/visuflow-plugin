package de.unipaderborn.visuflow.model;

import soot.Unit;
import soot.tagkit.Tag;

public class VFUnit {

    private String fullyQualifiedName;
    protected Unit wrapped;

    private Object inSet;
    private Object outSet;
    
    private VFMethod vfMethod;

    public VFMethod getVfMethod() {
		return vfMethod;
	}

	public void setVfMethod(VFMethod vfMethod) {
		this.vfMethod = vfMethod;
	}

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

    @Override
    public int hashCode() {
        final int prime = 31;
        int result = 1;
        result = prime * result + ((fullyQualifiedName == null) ? 0 : fullyQualifiedName.hashCode());
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (obj == null)
            return false;
        if (getClass() != obj.getClass())
            return false;
        VFUnit other = (VFUnit) obj;
        if (fullyQualifiedName == null) {
            if (other.fullyQualifiedName != null)
                return false;
        } else if (!fullyQualifiedName.equals(other.fullyQualifiedName))
            return false;
        return true;
    }
}

