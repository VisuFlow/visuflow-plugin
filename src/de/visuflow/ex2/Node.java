package de.visuflow.ex2;

import soot.Unit;

public class Node {
	
	String label;
	int id;
	
	Node(String label, int id)
	{
		this.label=label;
		this.id=id;
	}
	public void setLabel(String label)
	{
		this.label = label;
	}
	
	public String getLabel()
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
