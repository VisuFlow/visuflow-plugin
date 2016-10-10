package de.unipaderborn.visuflow.model.impl;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.List;

import org.osgi.service.component.ComponentContext;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.visuflow.callgraph.ICFGStructure;
import de.visuflow.callgraph.JimpleModelAnalysis;

public class DummyDataModel implements DataModel {
	private List<VFClass> jimpleClasses = new ArrayList<VFClass>();

	private EventAdmin eventAdmin;
	
	@Override
	public List<VFClass> listClasses() {
		return jimpleClasses;
	}

	@Override
	public List<VFMethod> listMethods(VFClass vfClass) {
		List<VFMethod> methods = Collections.emptyList();
		for (VFClass current : jimpleClasses) {
			if(current == vfClass) {
				methods = vfClass.getMethods();
			}
		}
		return methods;
	}

	@Override
	public List<VFUnit> listUnits(VFMethod vfMethod) {
		List<VFUnit> units = Collections.emptyList();
		for (VFClass currentClass : jimpleClasses) {
			for (VFMethod currentMethod : currentClass.getMethods()) {
				if(currentMethod == vfMethod) {
					units = vfMethod.getUnits();
				}
			}
		}
		return units;
	}
	
	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}
	
	protected void activate(ComponentContext context)
    {
		ICFGStructure icfg = new ICFGStructure();
		JimpleModelAnalysis analysis = new JimpleModelAnalysis();
		analysis.createICFG(icfg, jimpleClasses);
		
		Dictionary<String, Object> properties = new Hashtable<String, Object>();
		properties.put("model", jimpleClasses);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_MODEL_CHANGED, properties);
		eventAdmin.postEvent(modelChanged);
		
		new Thread() {
			public void run() {
				try {
					Thread.sleep(20000);
					System.out.println("Fire!!!!");
					Dictionary<String, Object> properties = new Hashtable<String, Object>();
					properties.put("model", jimpleClasses);
					Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_MODEL_CHANGED, properties);
					eventAdmin.postEvent(modelChanged);
				
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
			};
		}.start();
    }

    protected void deactivate(ComponentContext context)
    {
    	// noop
    }

}
