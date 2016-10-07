package de.unipaderborn.visuflow.model.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.visuflow.callgraph.CallGraphGenerator;
import de.visuflow.callgraph.GraphStructure;

public class DummyDataModel implements DataModel {
	private Map<VFMethod, GraphStructure> analysisData = new HashMap<>();

	public DummyDataModel() {
		CallGraphGenerator generator = new CallGraphGenerator();
		generator.runAnalysis(analysisData);
	}
	
	@Override
	public List<VFClass> listClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VFMethod> listMethods(VFClass vfClass) {
		List<VFMethod> methods = new ArrayList<VFMethod>();
		Iterator<Entry<VFMethod, GraphStructure>> methodIterator = analysisData.entrySet().iterator();
		while(methodIterator.hasNext())
		{
			Entry<VFMethod, GraphStructure> curr = methodIterator.next();
			VFMethod currMethod = curr.getKey();
			methods.add(currMethod);
		}
		
		return methods;
	}

	@Override
	public List<VFUnit> listUnits(VFMethod vfMethod) {
		// TODO Auto-generated method stub
		return null;
	}

}
