package de.unipaderborn.visuflow.model;

import soot.Unit;

public class VFNode {
	
	VFUnit vfUnit;
	int id;
	
	public VFNode(VFUnit label, int id)
	{
		this.vfUnit=label;
		this.id=id;
	}

	public void setLabel(VFUnit label)
	{
		this.vfUnit = label;
	}
	
	public Unit getUnit()
	{
		return this.vfUnit.getUnit();
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
