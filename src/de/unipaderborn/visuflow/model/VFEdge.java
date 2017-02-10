package de.unipaderborn.visuflow.model;

public class VFEdge {

	int id;
	VFNode source;
	VFNode destination;

	public VFEdge(int id, VFNode source, VFNode destination)
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
