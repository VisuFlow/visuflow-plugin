package de.visuflow.callgraph;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import soot.Body;
import soot.Unit;
import soot.toolkits.graph.ExceptionalUnitGraph;

public class ControlFlowGraphGenerator {

	static ExceptionalUnitGraph eg;
	private int nodeNumber;
	private int edgeNumber;
	private List<Node> listNodes;
	private List<Edge> listEdges;
	
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
			Node node = new Node(head, nodeNumber);
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
		List<Unit> l = eg.getSuccsOf(currentNode);
		Iterator<Unit> it = l.iterator();
		while (it.hasNext()) {
			Unit temp = it.next();
			Iterator<Node> nodesIterator = listNodes.iterator();
			while (nodesIterator.hasNext()) {
				Node node = (Node) nodesIterator.next();
				if (node.getLabel().equals(temp)) {
					present = true;
				}
			}
			if (!present) {
				nodeNumber++;
				Node node = new Node(temp, nodeNumber);
				listNodes.add(node);
			}
			Node source = null, destination = null;
			Iterator<Node> it1 = listNodes.iterator();
			while (it1.hasNext()) {
				Node node = (Node) it1.next();
				if (node.getLabel().equals(currentNode)) {
					source = node;
				}
				if (node.getLabel().equals(temp)) {
					destination = node;
				}
			}
			edgeNumber++;
			Edge edgeEntry = new Edge(edgeNumber, source, destination);
			listEdges.add(edgeEntry);
			traverseUnits(temp);
		}

	}
}
