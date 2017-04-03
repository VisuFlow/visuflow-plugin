package de.unipaderborn.visuflow.model;

/**
 * This class maintains the edge between instances of two {@link de.unipaderborn.visuflow.model.VFMethod} nodes.
 * @author Shashank B S
 *
 */
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
	
	public String toString(){
		return "Source: " + sourceMethod + " | Destination: " + destMethod;
	}
}
