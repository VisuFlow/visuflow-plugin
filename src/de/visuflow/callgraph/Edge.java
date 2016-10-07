package de.visuflow.callgraph;

public class Edge {
	
	int id;
	Node source;
	Node destination;
	Method sourceMethod;
	Method destMethod;
	
	public Edge(int id, Node source, Node destination)
	{
		this.id=id;
		this.source=source;
		this.destination=destination;
	}
	
	public Edge(int id, Method sourceMethod, Method destMethod)
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
	public Node getSource() {
		return source;
	}
	public void setSource(Node source) {
		this.source = source;
	}
	public Node getDestination() {
		return destination;
	}
	public void setDestination(Node destination) {
		this.destination = destination;
	}	

}
