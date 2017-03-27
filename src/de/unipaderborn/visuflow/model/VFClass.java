package de.unipaderborn.visuflow.model;

import java.util.ArrayList;
import java.util.List;

import soot.SootClass;

/**
 * This class is a wrapper around the {@link soot.SootClass} which maintains the list of all the methods.
 * @author Shashank B S
 *
 */
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
