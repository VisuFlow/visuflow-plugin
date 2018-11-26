package de.unipaderborn.visuflow.model.impl;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class EventDatabase {
	
	private static EventDatabase instance = new EventDatabase();
	
	private Logger logger = Visuflow.getDefault().getLogger();

	private List<Event> events;
	
	private int backwardsMarker;
	
	private DataModel dataModel;
	
	private EventDatabase() {
		events = new ArrayList<>();
		backwardsMarker = -1;
		dataModel = ServiceUtil.getService(DataModel.class);
	}
	
	public static EventDatabase getInstance() {
		return instance;
	}
	
	public void addEvent(Event event) {
		events.add(event);
		backwardsMarker = events.size()-1;
		logger.info("Event added: " + event.toString());
	}
	
	public ArrayList<Event> findAllFqnEvents(String fqn){
		ArrayList<Event> hits = new ArrayList<>();
		for(int i = 0; i <= backwardsMarker; i++) {
			Event tmp = events.get(i);
			if(tmp.getUnit().equals(fqn)) {
				hits.add(tmp);
			}
		}
		return hits;
	}
	
	public Event searchTimestamp(Timestamp time) {
		for(int i = 0; i <= backwardsMarker; i++) {
			if(events.get(i).getTimestamp().equals(time)) {
				return events.get(i);
			}
		}
		return null;
	}
	
	public void resume() {
		for(int i = backwardsMarker; i < events.size(); i++) {
			Event tmp = events.get(i);
			String fqn = tmp.getUnit();
			if(dataModel.getVFUnit(fqn) == null) {
				logger.info("In stepOver (DB) " + fqn + "is not found");
				backwardsMarker++;
				return;
			}
			dataModel.setInSet(fqn, "", tmp.getInSet());
			dataModel.setOutSet(fqn, "", tmp.getOutSet());
			backwardsMarker = i;			
		}
	}
	
	public void stepOver(VFUnit dest) {
		for(int i = backwardsMarker; i < events.size(); i++) {
			Event tmp = events.get(i);
			String fqn = tmp.getUnit();
			if(dataModel.getVFUnit(fqn) == null) {
				logger.info("In stepOver (DB) " + fqn + "is not found");
				backwardsMarker++;
				return;
			}
			if(fqn.equals(dest.getFullyQualifiedName())) {
				return;
			}
			dataModel.setInSet(fqn, "", tmp.getInSet());
			dataModel.setOutSet(fqn, "", tmp.getOutSet());
			backwardsMarker = i;			
		}
	}
	
	public void stepBack(VFUnit dest) {
		for(int i = backwardsMarker; i >= 0; i--) {
			String currentUnit = events.get(i).getUnit();
			backwardsMarker = i;
			if(currentUnit == null) {
				logger.info("In stepBack (DB) " + currentUnit + "is not found");
				backwardsMarker--;
				return;
			}
			ArrayList<Event> unitEvents = findAllFqnEvents(dest.getFullyQualifiedName());
			if(unitEvents.size() > 1) {
				dataModel.setInSet(dest.getFullyQualifiedName(), "", (unitEvents.get(unitEvents.size()-1).getInSet()));
				dataModel.setOutSet(dest.getFullyQualifiedName(), "", (unitEvents.get(unitEvents.size()-1).getOutSet()));
			} else {
				dataModel.setInSet(dest.getFullyQualifiedName(), "", null);
				dataModel.setOutSet(dest.getFullyQualifiedName(), "", null);
			}
			if(currentUnit.equals(dest.getFullyQualifiedName())) {
					break;
			}
		}
	}
	
	public void printFullDatabase() {
		logger.info("Start printing full database...");
		logger.info("Printing " + events.size() + " database entries.");
		for(int i = 0; i < events.size(); i++) {
			logger.info(events.get(i).toString());
		}
	}
}