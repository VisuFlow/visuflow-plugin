package de.unipaderborn.visuflow.model.impl;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.visuflow.callgraph.CallGraphGenerator;
import de.visuflow.callgraph.ControlFlowGraph;

public class DummyDataModel implements DataModel {
	private List<VFClass> analysisData = new ArrayList<VFClass>();

	private EventAdmin eventAdmin;
	
	@Override
	public List<VFClass> listClasses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public List<VFMethod> listMethods(VFClass vfClass) {
		List<VFMethod> methods = new ArrayList<VFMethod>();
//		Iterator<Entry<VFMethod, ControlFlowGraph>> methodIterator = analysisData.entrySet().iterator();
//		while(methodIterator.hasNext())
//		{
//			Entry<VFMethod, ControlFlowGraph> curr = methodIterator.next();
//			VFMethod currMethod = curr.getKey();
//			methods.add(currMethod);
//		}
		
		return methods;
	}

	@Override
	public List<VFUnit> listUnits(VFMethod vfMethod) {
		// TODO Auto-generated method stub
		return null;
	}
	
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	protected void activate(ComponentContext context)
    {
		CallGraphGenerator generator = new CallGraphGenerator();
		generator.runAnalysis(analysisData);
		
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("model", analysisData);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_MODEL_CHANGED, properties);
		eventAdmin.postEvent(modelChanged);
    }

    protected void deactivate(ComponentContext context)
    {
    	// noop
    }

}
