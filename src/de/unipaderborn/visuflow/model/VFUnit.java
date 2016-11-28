package de.unipaderborn.visuflow.model;

import soot.Unit;

public class VFUnit {
	
	Unit label;
	int id;
	
	private Object inSet;
    private Object outSet;
	
	public VFUnit(Unit label, int id)
	{
		this.label=label;
		this.id=id;
	}

	public VFUnit(Unit unit) {
		this.label = unit;
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

	public Unit getUnit() {
		return label;
	}

	public Object getInSet() {
		return inSet;
	}

	public void setInSet(Object inSet) {
		this.inSet = inSet;
	}

	public Object getOutSet() {
		return outSet;
	}

	public void setOutSet(Object outSet) {
		this.outSet = outSet;
	}

}
