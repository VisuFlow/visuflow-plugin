package de.unipaderborn.visuflow.model;

public class VFEdge {
	
	int id;
	VFUnit source;
	VFUnit destination;
	Method sourceMethod;
	Method destMethod;
	
	public VFEdge(int id, VFUnit source, VFUnit destination)
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
	
	public VFUnit getSource() {
		return source;
	}
	
	public void setSource(VFUnit source) {
		this.source = source;
	}
	
	public VFUnit getDestination() {
		return destination;
	}
	
	public void setDestination(VFUnit destination) {
		this.destination = destination;
	}	

}
