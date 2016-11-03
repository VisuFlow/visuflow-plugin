package de.unipaderborn.visuflow.model;

public class VFMethodEdge {
	int id;
	VFMethod sourceMethod;
	VFMethod destMethod;
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public VFMethod getSourceMethod() {
		return sourceMethod;
	}
	
	public void setSourceMethod(VFMethod sourceMethod) {
		this.sourceMethod = sourceMethod;
	}
	
	public VFMethod getDestMethod() {
		return destMethod;
	}
	
	public void setDestMethod(VFMethod destMethod) {
		this.destMethod = destMethod;
	}
}
