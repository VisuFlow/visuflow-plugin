package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import soot.Unit;
import soot.tagkit.Tag;

/**
 * This class is a wrapper around the {@link soot.Unit} and maintains an instance of {@link soot.Unit}.
 * 
 * @author Shashank B S
 *
 */
public class VFUnit {

	private String fullyQualifiedName;
	protected Unit wrapped;

	private Object inSet;
	private Object outSet;

	private VFMethod vfMethod;

	private Map<String, String> hmCustAttr = new HashMap<>();
	private List<VFUnit> outgoingEdges = new ArrayList<>();

	public List<VFUnit> getOutgoingEdges() {
		return outgoingEdges;
	}

	public void setOutgoingEdges(List<VFUnit> outgoingEdges) {
		this.outgoingEdges = outgoingEdges;
	}

	public boolean addOutgoingEdge(VFUnit outgoingEdge) {
		if (outgoingEdges.contains(outgoingEdge)) {
			return false;
		}
		return outgoingEdges.add(outgoingEdge);
	}

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
		// LALA
		if (outSet instanceof String) {
			String stringOutSet = (String) outSet;
			if (stringOutSet.length() < 3)
				return outSet;

			stringOutSet = stringOutSet.substring(1, stringOutSet.length() - 1);
			String[] parts = stringOutSet.split(", ");
			StringBuilder sb = new StringBuilder();
			sb.append("[");
			boolean first = true;
			for (String part : parts) {
				if (first)
					first = false;
				else
					sb.append(", ");

				String local = part.substring(part.indexOf("LOCAL") + 6, part.indexOf("FIELD") - 1);
				sb.append(local);
				if (part.contains("[<")) {
					String field = part.substring(part.lastIndexOf(" ") + 1, part.lastIndexOf(">"));
					sb.append(".");
					sb.append(field);
				}
			}
			sb.append("]");
			return sb.toString();
		}
		// END LALA

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

	public Map<String, String> getHmCustAttr() {
		return hmCustAttr;
	}

	public void setHmCustAttr(Map<String, String> hmCustAttr) {
		this.hmCustAttr = hmCustAttr;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((fullyQualifiedName == null) ? 0 : fullyQualifiedName.hashCode());
		return result;
	}

	@Override
	public String toString() {
		return getFullyQualifiedName();
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
