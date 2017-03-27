package de.unipaderborn.visuflow.model.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.NoSuchElementException;

import de.unipaderborn.visuflow.model.VFEdge;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import soot.Body;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;

/**
 * @author Zafar Habeeb
 *
 */
public class ControlFlowGraphGenerator {

	static ExceptionalUnitGraph eg;
	private int nodeNumber;
	private int edgeNumber;
	private List<VFNode> listNodes;
	private List<VFEdge> listEdges;
	private VFMethod method;
	
	/**
	 * @param method
	 * @return ControlFlowGraph
	 * This method generates the CFG of a method. It recursively iterates over the units and its successors
	 * and creates a graph structure with nodes as units and control-flow between units as edges.
	 */
	public ControlFlowGraph generateControlFlowGraph(VFMethod method) {
		this.method = method;
		Body b = method.getBody();
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
			VFNode node = new VFNode(getVFUnit(head), nodeNumber);
			listNodes.add(node);
			break;
		}
		traverseUnits(head);
		g.listEdges = listEdges;
		g.listNodes = listNodes;
		return g;
	}
	
	/**
	 * @param currentNode
	 * This recursive method is called from method generateControlFlowGraph() to iterate over units
	 * and its successors.
	 */
	private void traverseUnits(Unit currentNode) {
		boolean present = false;
		boolean edgeconnection = false;
		List<Unit> l = eg.getSuccsOf(currentNode);
		Iterator<Unit> it = l.iterator();
		while (it.hasNext()) {
			Unit temp = it.next();
			Iterator<VFEdge> edges = listEdges.iterator();
			while(edges.hasNext()){
				VFEdge edge = (VFEdge) edges.next();
				if (edge != null && temp != null && edge.getSource() != null && edge.getDestination() != null) {
				if(edge.getSource().getUnit().equals(currentNode) && edge.getDestination().getUnit().equals(temp))
				{
					edgeconnection = true;
					break;
				}
			}
				
			}
			if(edgeconnection)
			continue;
			Iterator<VFNode> nodesIterator = listNodes.iterator();
			while (nodesIterator.hasNext()) {
				VFNode node = (VFNode) nodesIterator.next();
				if (node.getUnit().equals(temp)) {
					present = true;
				}
			}
			if (!present) {
				nodeNumber++;
				VFNode node = new VFNode(getVFUnit(temp), nodeNumber);
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
			//System.out.println(edgeEntry.getSource().toString());
			//System.out.println(edgeEntry.getDestination().toString());
			traverseUnits(temp);
		}
	}

	private VFUnit getVFUnit(Unit unit) {
		for (VFUnit vfUnit : method.getUnits()) {
			if(vfUnit.getUnit() == unit) {
				return vfUnit;
			}
		}
		throw new NoSuchElementException("VFMethod " + method.getSootMethod().getName() + " does not contain unit " + unit.toString());
	}
}
