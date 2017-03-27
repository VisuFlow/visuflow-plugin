package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

import de.unipaderborn.visuflow.model.graph.ControlFlowGraph;
import soot.Body;
import soot.SootMethod;

/**
 * This class is a wrapper around the {@link soot.SootMethod} and maintains all the units of that method.
 * @author Shashank B S
 *
 */
public class VFMethod {

	protected SootMethod wrapped;
	private List<VFUnit> units = new ArrayList<>();
	private List<VFUnit> incomingEdges = new ArrayList<>();
	private Body body;
	private ControlFlowGraph controlFlowGraph;
	private int id;
	private VFClass vfClass;

	public VFClass getVfClass() {
		return vfClass;
	}

	public void setVfClass(VFClass vfClass) {
		this.vfClass = vfClass;
	}

	public int getId() {
		return id;
	}

	public void setId(int id) {
		this.id = id;
	}

	public VFMethod(SootMethod wrapped) {
		this.wrapped = wrapped;
	}

	public VFMethod(int methodcount, SootMethod wrapped) {
		this.id = methodcount;
		this.wrapped = wrapped;
	}

	public SootMethod getSootMethod() {
		return wrapped;
	}

	public List<VFUnit> getUnits() {
		return units;
	}

	public Body getBody() {
		return body;
	}

	public void setBody(Body body) {
		this.body = body;
	}

	public ControlFlowGraph getControlFlowGraph() {
		return controlFlowGraph;
	}

	public void setControlFlowGraph(ControlFlowGraph controlFLowGraph) {
		this.controlFlowGraph = controlFLowGraph;
	}

	@Override
	public String toString() {
		return wrapped != null ? /*wrapped.getDeclaringClass().getName()+"."+*/ wrapped.getName() : super.toString();
	}

	public List<VFUnit> getIncomingEdges() {
		return incomingEdges;
	}

	public void setIncomingEdges(List<VFUnit> incomingEdges) {
		this.incomingEdges = incomingEdges;
	}

	public boolean addIncomingEdge(VFUnit incomingEdge){
		if(incomingEdges.contains(incomingEdge)){
			return false;
		}
		return incomingEdges.add(incomingEdge);
	}

	/**
	 * Returns a unit after the given unit. The offset determines how
	 * many units to skip. Offset 1 means the next unit (direct sibling).
	 * @param unit
	 * @param offset
	 * @return
	 */
	public VFUnit getUnitAfter(VFUnit unit, int offset) {
		boolean returnNext = false;
		int lastIndex = units.size() - 1;
		for (int i = 0; i < units.size(); i++) {
			VFUnit current = units.get(i);
			if(returnNext) {
				int requestedIndex = i - 1 + offset;
				if(requestedIndex <= lastIndex) {
					return units.get(requestedIndex);
				} else {
					throw new NoSuchElementException("There is no unit after " + unit.getFullyQualifiedName());
				}
			} else if(current.getFullyQualifiedName().equals(unit.getFullyQualifiedName())) {
				if(i == lastIndex) {
					throw new NoSuchElementException("There is no unit after " + unit.getFullyQualifiedName());
				} else {
					returnNext = true;
				}
			}
		}
		throw new UnitNotFoundException("There is no unit after " + unit.getFullyQualifiedName());
	}
}
