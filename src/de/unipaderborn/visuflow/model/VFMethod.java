package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.List;

import de.unipaderborn.visuflow.model.graph.ControlFlowGraph;
import soot.Body;
import soot.SootMethod;

public class VFMethod {

    protected SootMethod wrapped;
    private List<VFUnit> units = new ArrayList<>();
    private Body body;
    private ControlFlowGraph controlFlowGraph;

	public VFMethod(SootMethod wrapped) {
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
    
}
