package de.unipaderborn.visuflow.model.impl;

import java.sql.Timestamp;

public class Event {
	
	private int id;
	
	private Timestamp time;
	
	private String unit;
	
	private String inSet;
	
	private String outSet;
	
	public Event(int id, String unit, String inSet, String outSet){
		this.time = new Timestamp(System.currentTimeMillis());
		this.id = id;
		this.unit = unit;
		this.inSet = inSet;
		this.outSet = outSet;
	}
	
	Timestamp getTimestamp() {
		return time;
	}
	
	public String getUnit() {
		return unit;
	}
	
	public String getInSet() {
		return inSet;
	}
	
	public String getOutSet() {
		return outSet;
	}
	
	public String toString() {
		return this.getTimestamp() + " (" + this.getUnit() + ") : " + this.getInSet() + this.getOutSet();
	}
	
	public int getId() {
		return id;
	}
}