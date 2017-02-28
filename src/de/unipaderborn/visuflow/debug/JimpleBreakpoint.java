package de.unipaderborn.visuflow.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

public class JimpleBreakpoint extends LineBreakpoint {

	private List<IBreakpoint> javaBreakpoints = new ArrayList<>();

	public JimpleBreakpoint() {
		super();
	}

	public JimpleBreakpoint(IMarker marker) throws CoreException {
		super();
		setMarker(marker);
		setAttribute(ENABLED, true);
	}

	@Override
	public String getModelIdentifier() {
		return "JimpleModel";
	}

	public List<IBreakpoint> getJavaBreakpoints() {
		return javaBreakpoints;
	}

	public void addJavaBreakpoint(IBreakpoint javaBreakpoint) {
		this.javaBreakpoints.add(javaBreakpoint);
	}

	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		for (IBreakpoint javaBreakpoint : javaBreakpoints) {
			javaBreakpoint.setEnabled(enabled);
		}
	}

	@Override
	public void delete() throws CoreException {
		for (IBreakpoint javaBreakpoint : javaBreakpoints) {
			javaBreakpoint.delete();
		}
		//super.delete();
	}
}
