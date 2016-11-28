package de.unipaderborn.visuflow.model;

import soot.Unit;

public class VFNode {
	
	Unit label;
	int id;	
	
	public VFNode(Unit label, int id)
	{
		this.label=label;
		this.id=id;
	}

	public void setLabel(Unit label)
	{
		this.label = label;
	}
	
	public Unit getLabel()
	{
		return this.label;
	}
	
	public void setId(int id)
	{
		this.id=id;
	}
	
	public int getId()
	{
		return this.id;
	}

}
