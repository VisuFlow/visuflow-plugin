package de.unipaderborn.visuflow.model;

import soot.Unit;

/**
 * This class is a wrapper around {@link de.unipaderborn.visuflow.model.VFUnit} and maintains an instance of {@link de.unipaderborn.visuflow.model.VFUnit}. 
 * @author Shashank B S
 *
 */
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
	
	public VFUnit getVFUnit()
	{
		return this.vfUnit;
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
