package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;

public class VFClass {

	private SootClass wrapped;

	private List<VFMethod> methods = new ArrayList<VFMethod>();

	public VFClass(SootClass sootClass) {
		this.wrapped = sootClass;
	}

	public SootClass getSootClass() {
		return wrapped;
	}

	public List<VFMethod> getMethods() {
		return methods;
	}
}
