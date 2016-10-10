package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.List;

import de.visuflow.callgraph.ControlFlowGraph;
import soot.Body;
import soot.SootMethod;
import soot.Unit;

public class VFMethod {

    protected SootMethod wrapped;
    private List<VFUnit> units = new ArrayList<>();
    private Body body;
    private ControlFlowGraph controlFlowGraph;

	public VFMethod(SootMethod wrapped) {
        this.wrapped = wrapped;
		for (Unit unit : wrapped.getActiveBody().getUnits()) {
			units.add(new VFUnit(unit));
		}
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
    
}
