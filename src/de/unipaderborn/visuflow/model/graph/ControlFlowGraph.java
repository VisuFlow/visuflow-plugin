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
	
	public List<VFNode> getIncomingEdges(VFUnit node){
		List<VFNode> results = new ArrayList<>();
		for(int i = 0; i < listEdges.size(); i++) {
			if(listEdges.get(i).getDestination().getVFUnit().equals(node)) {
				results.add(listEdges.get(i).getSource());
			}
		}
		return results;
	}

}
