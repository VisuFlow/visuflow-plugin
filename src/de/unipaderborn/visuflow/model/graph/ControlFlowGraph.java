package de.unipaderborn.visuflow.model.graph;

import java.util.ArrayList;
import java.util.List;

import de.unipaderborn.visuflow.model.VFEdge;
import de.unipaderborn.visuflow.model.VFNode;

/**
 * Data structure used to store information about CFG
 * @author Zafar Habeeb
 */
public class ControlFlowGraph {
	public List<VFNode> listNodes = new ArrayList<VFNode>();
	public List<VFEdge> listEdges = new ArrayList<VFEdge>();

}
