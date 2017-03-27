package de.unipaderborn.visuflow.model.graph;

import java.util.ArrayList;
import java.util.List;

import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFMethodEdge;

/**
 * Data structure used to information on ICFG
 *
 */
public class ICFGStructure {
	public List<VFMethod> listMethods = new ArrayList<VFMethod>();
	public List<VFMethodEdge> listEdges = new ArrayList<VFMethodEdge>();
}
