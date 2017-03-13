package de.unipaderborn.visuflow.debug;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.runtime.IPath;

import de.unipaderborn.visuflow.model.VFUnit;

public class UnitLocation {

	public IProject project;
	public IPath jimpleFile;
	public int line;
	public int charStart;
	public int charEnd;
	public VFUnit vfUnit;
}
