package de.unipaderborn.visuflow.model.impl;

import java.sql.Timestamp;

public class Event {
	
	private Timestamp time;
	
	private String unit;
	
	private String inSet;
	
	private String outSet;
	
	public Event(String unit, String inSet, String outSet){
		this.time = new Timestamp(System.currentTimeMillis());
		this.unit = unit;
		this.inSet = inSet;
		this.outSet = outSet;
	}
	
	Timestamp getTimestamp() {
		return time;
	}
	
	String getUnit() {
		return unit;
	}
	
	String getInSet() {
		return inSet;
	}
	
	String getOutSet() {
		return outSet;
	}
	
	public String toString() {
		return this.getTimestamp() + " (" + this.getUnit() + ") : " + this.getInSet() + this.getOutSet();
	}
}