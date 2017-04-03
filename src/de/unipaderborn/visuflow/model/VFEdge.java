package de.unipaderborn.visuflow.model;

/**
 * This class maintains the edge between instances of two {@link de.unipaderborn.visuflow.model.VFNode} nodes.
 * @author Shashank B S
 *
 */
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
