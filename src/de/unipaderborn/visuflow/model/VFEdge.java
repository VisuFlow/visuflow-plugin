package de.unipaderborn.visuflow.model;

public class VFEdge {
	
	int id;
	VFNode source;
	VFNode destination;
	Method sourceMethod;
	Method destMethod;
	
	public VFEdge(int id, VFNode source, VFNode destination)
	{
		this.id=id;
		this.source=source;
		this.destination=destination;
	}
	
	public VFEdge(int id, Method sourceMethod, Method destMethod)
	{
		this.id=id;
		this.sourceMethod=sourceMethod;
		this.destMethod=destMethod;
	}
	
	public Method getSourceMethod() {
		return sourceMethod;
	}
	
	public void setSourceMethod(Method sourceMethod) {
		this.sourceMethod = sourceMethod;
	}
	
	public Method getDestMethod() {
		return destMethod;
	}
	public void setDestMethod(Method destMethod) {
		this.destMethod = destMethod;
	}
	
	public int getId() {
		return id;
	}
	
	public void setId(int id) {
		this.id = id;
	}
	
	public VFNode getSource() {
		return source;
	}
	
	public void setSource(VFNode source) {
		this.source = source;
	}
	
	public VFNode getDestination() {
		return destination;
	}
	
	public void setDestination(VFNode destination) {
		this.destination = destination;
	}	

}
