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
	
	private boolean upToDate;
	
	private EventDatabase() {
		events = new ArrayList<>();
		backwardsMarker = -1;
		upToDate = true;
	}
	
	public static EventDatabase getInstance() {
		return instance;
	}
	
	public void addEvent(String unit, boolean inChanged, List<String> added, boolean outChanged, List<String> deleted) {
		if(dataModel == null) {
			dataModel = ServiceUtil.getService(DataModel.class);
		}
		backwardsMarker = events.size();
		Event event = new Event(events.size(), unit, inChanged, added, outChanged, deleted);
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
		if(upToDate) {
			return;
		}
		for(int i = backwardsMarker; i < events.size(); i++) {
			Event tmp = events.get(i);
			String fqn = tmp.getUnit();
			updateDataModel(tmp, fqn, i);
			backwardsMarker = i;
		}
	}
	
	private void updateDataModel(Event event, String fqn, int index) {
		String currentData = (String) dataModel.getCurrentUnit().getOutSet();
		if(currentData == null) {
			currentData = "[";
		} else {
			currentData = currentData.substring(0, currentData.length()-1);
		}
		dataModel.setInSet(fqn, "", currentData + "]");
		if(event.getDeletedSet() != null) {
			for(int j = 0; j < event.getDeletedSet().size(); j++) {
				currentData = currentData.replace(event.getDeletedSet().get(j), "");
			}
		}
		if(event.getAddedSet() != null) {
			for(int j = 0; j < event.getAddedSet().size(); j++) {
				currentData = currentData + ", " + event.getAddedSet().get(j);
			}
		}
		currentData = currentData.replace(", ,", ",");
		currentData = currentData.replaceFirst("\\[, ", "\\[");
		String newData = currentData + "]";
		dataModel.setOutSet(fqn, "", newData);
		dataModel.setCurrentUnit(dataModel.getVFUnit(event.getUnit()));
	}
	
	public void stepOver(VFUnit dest) {
		for(int i = backwardsMarker; i < events.size(); i++) {
			Event tmp = events.get(i);
			String fqn = tmp.getUnit();
			backwardsMarker = i;
			if(fqn.equals(dest.getFullyQualifiedName())) {
				return;
			}
			updateDataModel(tmp, fqn, i);
			if(backwardsMarker == events.size()-1) {
				upToDate = true;
			}
			//stops stepping over if there is no known event in the remaining list that belongs to the destination
			if(this.findAllFqnEvents(dest.getFullyQualifiedName(), backwardsMarker-1, events.size()-1).size() == 0) {
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
			dataModel.setCurrentUnit(dataModel.getVFUnit(events.get(i-1).getUnit()));
			String currentData = (String) dataModel.getCurrentUnit().getOutSet();
			ArrayList<Event> unitEvents = findAllFqnEvents(currentUnit, 0, backwardsMarker);
			if(unitEvents.size() > 1) {
				int tempMarker = backwardsMarker;
				List<String> tempDeleted = new ArrayList<>();
				List<String> tempAdded = new ArrayList<>();
				do {
					if(events.get(tempMarker).getValuesDeleted()) {
						for(int j = 0; j < events.get(tempMarker).getDeletedSet().size(); j++) {
							tempDeleted.add(events.get(tempMarker).getDeletedSet().get(j));
						}
					}
					if(events.get(tempMarker).getValuesAdded()) {
						for(int j = 0; j < events.get(tempMarker).getAddedSet().size(); j++) {
							tempAdded.add(events.get(tempMarker).getAddedSet().get(j));
						}
					}
					tempMarker--;
				} while(!events.get(tempMarker).getUnit().equals(currentUnit));
				
				currentData = currentData.substring(0, currentData.length()-1);
				for(int j = 0; j < tempDeleted.size(); j++) {
					currentData = currentData + ", " + tempDeleted.get(j);
				}
				for(int j = 0; j < tempAdded.size(); j++) {
					currentData = currentData.replace(tempAdded.get(j), "");
				}
				currentData = currentData.replace(", ,", ",");
				currentData = currentData.replaceFirst("\\[,", "\\[");
				dataModel.setOutSet(currentUnit, "", currentData + "]");
				if(tempMarker > 1) {
					Event lastEvent = events.get(tempMarker-1);
					if(lastEvent.getValuesAdded()) {
						for(int j = 0; j < lastEvent.getAddedSet().size(); j++) {
							currentData = currentData.replace(lastEvent.getAddedSet().get(j), "");
						}
					}
					if(lastEvent.getValuesDeleted()) {
						for(int j = 0; j < lastEvent.getDeletedSet().size(); j++) {
							currentData = currentData + ", " + lastEvent.getDeletedSet().get(j);
						}
					}
				}				
				dataModel.setInSet(currentUnit, "", currentData + "]");
			} else {
				dataModel.setInSet(currentUnit, "", null);
				dataModel.setOutSet(currentUnit, "", null);
			}
			
			if(currentUnit.equals(dest.getFullyQualifiedName())) {
				upToDate = false;
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
	
	void reset() {
		events = new ArrayList<>();
		backwardsMarker = -1;
		upToDate = true;
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
	
	public boolean getUpToDate() {
		return upToDate;
	}
}