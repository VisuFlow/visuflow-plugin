package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;
import soot.SootMethod;

public class VFClass {

	private SootClass wrapped;

	private List<VFMethod> methods = new ArrayList<VFMethod>();

	public VFClass(SootClass sootClass) {
		this.wrapped = sootClass;
		for (SootMethod sootMethod : sootClass.getMethods()) {
			methods.add(new VFMethod(sootMethod));
		}
	}

	public SootClass getSootClass() {
		return wrapped;
	}

	public List<VFMethod> getMethods() {
		return methods;
	}
}
