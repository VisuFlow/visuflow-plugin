package de.unipaderborn.visuflow.model;

import soot.SootMethod;

public class Method {

	int ID;
	SootMethod method;
	
	public Method(int ID, SootMethod method)
	{
		this.ID = ID;
		this.method = method;
	}
	public int getID() {
		return ID;
	}
	public void setID(int iD) {
		ID = iD;
	}
	public SootMethod getMethod() {
		return method;
	}
	public void setMethod(SootMethod method) {
		this.method = method;
	}
}
