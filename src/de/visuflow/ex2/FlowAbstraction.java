package de.visuflow.ex2;

import soot.Local;
import soot.SootField;
import soot.Unit;

public class FlowAbstraction {

	private final Unit source;
	private final Local local;
	private final SootField field;

	public FlowAbstraction(Unit source, Local local) {
		this(source, local, null);
	}

	public FlowAbstraction(Unit source, SootField field) {
		this(source, null, field);
	}

	public FlowAbstraction(Unit source, Local local, SootField field) {
		this.source = source;
		this.local = local;
		this.field = field;
	}

	public Unit getSource() {
		return this.source;
	}

	public Local getLocal() {
		return this.local;
	}

	public SootField getField() {
		return this.field;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((local == null) ? 0 : local.hashCode());
		result = prime * result + ((field == null) ? 0 : field.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null || !(obj instanceof FlowAbstraction))
			return false;
		FlowAbstraction other = (FlowAbstraction) obj;
		if (local == null) {
			if (other.local != null)
				return false;
		} else if (!local.equals(other.local))
			return false;
		if (field == null) {
			if (other.field != null)
				return false;
		} else if (!field.equals(other.field))
			return false;
		return true;
	}

	@Override
	public String toString() {
		if (local != null)
			return "LOCAL " + local;
		if (field != null)
			return "FIELD " + field;
		return "";
	}

	public FlowAbstraction deriveWithNewSource(Unit newSource) {
		return new FlowAbstraction(newSource, local, field);
	}

}
