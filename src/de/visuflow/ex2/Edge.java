package de.visuflow.ex2;
import de.visuflow.ex2.Node;

public class Edge {
	
	int id;
	Node source;
	Node destination;
	
	Edge(int id, Node source, Node destination)
	{
		this.id=id;
		this.source=source;
		this.destination=destination;
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
