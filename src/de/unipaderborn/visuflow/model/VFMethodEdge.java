package de.unipaderborn.visuflow.model;

public class VFMethodEdge {
	int id;
	VFMethod sourceMethod;
	VFMethod destMethod;
	
	public VFMethodEdge(int id, VFMethod sourceMethod, VFMethod destinationMethod) {
		this.id = id;
		this.sourceMethod = sourceMethod;
		this.destMethod = destinationMethod;
	}

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
