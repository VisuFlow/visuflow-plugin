package de.unipaderborn.visuflow.model.graph;

import java.util.ArrayList;
import java.util.List;

import de.unipaderborn.visuflow.model.VFEdge;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;

/**
 * Data structure used to store information about CFG
 * @author Zafar Habeeb
 */
public class ControlFlowGraph {
	public List<VFNode> listNodes = new ArrayList<VFNode>();
	public List<VFEdge> listEdges = new ArrayList<VFEdge>();
	
	private int temporaryNodes = 0;
	
	public List<VFNode> getIncomingEdges(VFUnit node){
		List<VFNode> results = new ArrayList<>();
		for(int i = 0; i < listEdges.size(); i++) {
			if(listEdges.get(i).getDestination().getVFUnit().equals(node)) {
				results.add(listEdges.get(i).getSource());
			}
		}
		return results;
	}
	
	public VFNode getNodeByVFUnit(VFUnit unit) {
		VFNode result = null;
		for(VFNode node: listNodes) {
			if(node.getVFUnit().equals(unit)) {
				result = node;
			}
		}
		return result;
	}
	
	public void addTemporaryNodes(List<VFNode> nodes) {
		temporaryNodes = nodes.size();
		int counter = listEdges.size() + 10;
		VFNode start = listNodes.get(0);
		for(int i = 0; i < temporaryNodes; i++) {
			listNodes.add(0, nodes.get(i));
			VFEdge edge = new VFEdge(counter, nodes.get(i), start);
			listEdges.add(0, edge);
			counter++;
		}
	}
	
	public void removeTemporaryNodes() {
		for(int i = 0; i < temporaryNodes; i++) {
			listNodes.remove(0);
		}
		temporaryNodes = 0;
	}

}
