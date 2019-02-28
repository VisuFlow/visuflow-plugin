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
	
	public void addEvent(String unit, String inSet, String outSet) {
		backwardsMarker = events.size();
		Event event = new Event(backwardsMarker, unit, inSet, outSet);
		events.add(event);
	}
	
	public ArrayList<Event> findAllFqnEvents(String fqn, int from, int to){
		ArrayList<Event> hits = new ArrayList<>();
		for(int i = from; i <= to; i++) {
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
			dataModel.setInSet(fqn, "", tmp.getInSet());
			dataModel.setOutSet(fqn, "", tmp.getOutSet());
			backwardsMarker = i;			
		}
	}
	
	public void stepOver(VFUnit dest) {
		for(int i = backwardsMarker; i < events.size(); i++) {
			Event tmp = events.get(i);
			String fqn = tmp.getUnit();
			if(fqn.equals(dest.getFullyQualifiedName())) {
				return;
			}
			dataModel.setInSet(fqn, "", tmp.getInSet());
			dataModel.setOutSet(fqn, "", tmp.getOutSet());
			backwardsMarker = i;
			if(this.findAllFqnEvents(dest.getFullyQualifiedName(), backwardsMarker, events.size()-1).size() == 0) {
				backwardsMarker++;
				return;
			}
		}
	}
	
	public void stepBack(VFUnit dest) {
		ArrayList<Event> tmp = this.findAllFqnEvents(dest.getFullyQualifiedName(), 0, backwardsMarker);
		if(tmp.size() == 0) {
			return;
		}
		
		for(int i = backwardsMarker; i >= 0; i--) {
			String currentUnit = events.get(i).getUnit();
			backwardsMarker = i;
			ArrayList<Event> unitEvents = findAllFqnEvents(currentUnit, 0, backwardsMarker);
			if(unitEvents.size() > 1) {
				dataModel.setInSet(currentUnit, "", (unitEvents.get(unitEvents.size()-1).getInSet()));
				dataModel.setOutSet(currentUnit, "", (unitEvents.get(unitEvents.size()-1).getOutSet()));
			} else {
				dataModel.setInSet(currentUnit, "", null);
				dataModel.setOutSet(currentUnit, "", null);
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
	
	public List<Event> getAllEvents() {
		return events;
	}
	
	public int getBackwardsMarker() {
		return backwardsMarker;
	}
	
	public Event getCurrentEvent() {
		if(backwardsMarker < 0) {
			return null;
		}
		return events.get(backwardsMarker);
	}
	
	public Event getEvent(int index) {
		return events.get(index);
	}
	
	public boolean upToDate() {
		if(backwardsMarker == events.size()-1) {
			return true;
		} else {
			return false;
		}
	}
}