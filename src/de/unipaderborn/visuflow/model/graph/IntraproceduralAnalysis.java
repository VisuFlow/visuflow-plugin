package de.unipaderborn.visuflow.model.graph;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFEdge;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import soot.Body;
import soot.SootMethod;
import soot.Unit;
import soot.options.Options;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;

public class IntraproceduralAnalysis extends ForwardFlowAnalysis<Unit, Set<FlowAbstraction>> {
	public int flowThroughCount = 0;

	static ExceptionalUnitGraph eg;
	public static int nodeNumber;
	public static int edgeNumber;
	public static int nodeCount = 0;
	public static int edgeCount = 0;
	public static HashMap<Unit, Integer> nodesMap = new HashMap<>();
	public static HashMap<Integer, List<Integer>> edgesMap = new HashMap<>();
	public static HashMap<SootMethod, ControlFlowGraph> hashMap = new HashMap<>();
	public static VFNode[] nodes = new VFNode[20];
	public static VFEdge[] edges = new VFEdge[20];
	public static List<VFNode> listNodes;
	public static List<VFEdge> listEdges;

	public IntraproceduralAnalysis(Body b, final List<VFClass> vfClasses) {
		super(new ExceptionalUnitGraph(b));
		Options.v().set_keep_line_number(true);
		nodeNumber=0;
		edgeNumber=0;
		listNodes = new ArrayList<>();
		listEdges = new ArrayList<>();
		ControlFlowGraph g = new ControlFlowGraph();
		Unit head = null;
		eg = new ExceptionalUnitGraph(b);
		List<Unit> list = eg.getHeads();
		Iterator<Unit> it1 = list.iterator();
		while (it1.hasNext()) {
			head = it1.next();
			nodeNumber++;
			VFNode node = new VFNode(new VFUnit(head), nodeNumber);
			listNodes.add(node);
			break;
		}
		traverseUnits(head);
		g.listEdges = listEdges;
		g.listNodes = listNodes;
		VFMethod method = new VFMethod(b.getMethod());
		method.setBody(b);
		method.setControlFlowGraph(g);
		
}

	public static void traverseUnits(Unit currentNode) {
		boolean present = false;
		List<Unit> l = eg.getSuccsOf(currentNode);
		Iterator<Unit> it = l.iterator();
		while (it.hasNext()) {
			Unit temp = it.next();
			Iterator<VFNode> nodesIterator = listNodes.iterator();
			while (nodesIterator.hasNext()) {
				VFNode node = (VFNode) nodesIterator.next();
				if (node.getUnit().equals(temp)) {
					present = true;
				}
			}
			if (!present) {
				nodeNumber++;
				VFNode node = new VFNode(new VFUnit(temp), nodeNumber);
				listNodes.add(node);
			}
			VFNode source = null, destination = null;
			Iterator<VFNode> it1 = listNodes.iterator();
			while (it1.hasNext()) {
				VFNode node = (VFNode) it1.next();
				if (node.getUnit().equals(currentNode)) {
					source = node;
				}
				if (node.getUnit().equals(temp)) {
					destination = node;
				}
			}
			edgeNumber++;
			VFEdge edgeEntry = new VFEdge(edgeNumber, source, destination);
			listEdges.add(edgeEntry);
			traverseUnits(temp);
		}

	}

	@Override
	protected void flowThrough(Set<FlowAbstraction> in, Unit d, Set<FlowAbstraction> out) {

	}

	@Override
	protected Set<FlowAbstraction> newInitialFlow() {
		return new HashSet<FlowAbstraction>();
	}

	@Override
	protected Set<FlowAbstraction> entryInitialFlow() {
		return new HashSet<FlowAbstraction>();
	}

	@Override
	protected void merge(Set<FlowAbstraction> in1, Set<FlowAbstraction> in2, Set<FlowAbstraction> out) {
		out.addAll(in1);
		out.addAll(in2);
	}

	@Override
	protected void copy(Set<FlowAbstraction> source, Set<FlowAbstraction> dest) {
		dest.clear();
		dest.addAll(source);
	}

	public void doAnalyis() {
		super.doAnalysis();
	}

}
