package de.unipaderborn.visuflow.model.graph;

import java.util.ArrayList;
import java.util.List;

import de.unipaderborn.visuflow.model.Method;
import de.unipaderborn.visuflow.model.VFEdge;

public class ICFGStructure {
	public List<Method> listMethods = new ArrayList<Method>();
	public List<VFEdge> listEdges = new ArrayList<VFEdge>();
}
