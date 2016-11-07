package de.unipaderborn.visuflow.model.graph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import de.unipaderborn.visuflow.model.VFEdge;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.graph.ControlFlowGraph;
import soot.Body;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class ControlFlowGraphGenerator {

	static ExceptionalUnitGraph eg;
	private int nodeNumber;
	private int edgeNumber;
	private List<VFNode> listNodes;
	private List<VFEdge> listEdges;
	
	public ControlFlowGraph generateControlFlowGraph(Body b) {
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
			VFNode node = new VFNode(head, nodeNumber);
			listNodes.add(node);
			break;
		}
		traverseUnits(head);
		g.listEdges = listEdges;
		g.listNodes = listNodes;
		return g;
	}
	
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
				if(edge.getSource().getLabel().equals(currentNode) && edge.getDestination().getLabel().equals(temp))
				{
					System.out.println("Here");
					edgeconnection = true;
					break;
				}
			}
			if(edgeconnection)
			continue;
			Iterator<VFNode> nodesIterator = listNodes.iterator();
			while (nodesIterator.hasNext()) {
				VFNode node = (VFNode) nodesIterator.next();
				if (node.getLabel().equals(temp)) {
					present = true;
				}
			}
			if (!present) {
				nodeNumber++;
				VFNode node = new VFNode(temp, nodeNumber);
				listNodes.add(node);
			}
			VFNode source = null, destination = null;
			Iterator<VFNode> it1 = listNodes.iterator();
			while (it1.hasNext()) {
				VFNode node = (VFNode) it1.next();
				if (node.getLabel().equals(currentNode)) {
					source = node;
				}
				if (node.getLabel().equals(temp)) {
					destination = node;
				}
			}
			edgeNumber++;
			VFEdge edgeEntry = new VFEdge(edgeNumber, source, destination);
			listEdges.add(edgeEntry);
			System.out.println(edgeEntry.getSource().toString());
			System.out.println(edgeEntry.getDestination().toString());
			traverseUnits(temp);
		}

	}
}
