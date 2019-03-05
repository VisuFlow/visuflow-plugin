package de.unipaderborn.visuflow.model.impl;

import java.sql.Timestamp;
import java.util.List;

public class Event {
	
	private int id;
	
	private Timestamp time;
	
	private boolean valuesAdded;
	
	private boolean valuesDeleted;
	
	private String unit;
	
	private List<String> addedSet;
	
	private List<String> deletedSet;
	
	public Event(int id, String unit, boolean valuesAdded, List<String> addedSet, boolean valuesDeleted, List<String> deletedSet) {
		this.time = new Timestamp(System.currentTimeMillis());
		this.id = id;
		this.unit = unit;
		this.valuesAdded = valuesAdded;
		if(valuesAdded) {
			this.addedSet = addedSet;
		}
		this.valuesDeleted = valuesDeleted;
		if(valuesDeleted) {
			this.deletedSet = deletedSet;
		}
	}
	
	Timestamp getTimestamp() {
		return time;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public boolean getValuesDeleted() {
		return valuesDeleted;
	}
	
	public boolean getValuesAdded() {
		return valuesAdded;
	}
	
	public List<String> getAddedSet(){
		return addedSet;
	}
	
	public List<String> getDeletedSet(){
		return deletedSet;
	}
	
	public String toString() {
		String output = this.getTimestamp() + " for " + this.unit + " - added units: ";
		if(addedSet != null) {
			for(int i = 0; i < addedSet.size(); i++) {
				output = output + addedSet.get(i) + " ";
			}
		}
		output = output + "- deleted units: ";
		if(deletedSet != null) {
			for(int i = 0; i < deletedSet.size(); i++) {
				output = output + deletedSet.get(i) + " ";
			}
		}
		
		return output;
	}
	
	public int getId() {
		return id;
	}
}