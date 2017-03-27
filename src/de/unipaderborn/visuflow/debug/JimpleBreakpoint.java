package de.unipaderborn.visuflow.debug;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.LineBreakpoint;

/**
 * A JimpleBreakpoint is a virtual breakpoint for a jimple unit.
 * It is a container for the actual conditional Java breakpoints, which are set in the flow functions
 * of the user analysis.
 *
 * @author henni@upb.de
 *
 */
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

	/**
	 * Returns the list of conditional Java breakpoints, which are associated with this Jimple breakpoint.
	 *
	 * @return a list of IBreakpoint, which represents the Java breakpoints, which are associated with this Jimple breakpoint.
	 */
	public List<IBreakpoint> getJavaBreakpoints() {
		return javaBreakpoints;
	}

	/**
	 * Returns the list of conditional Java breakpoints, which are associated with this Jimple breakpoint.
	 *
	 * @param javaBreakpoint the conditional Java breakpoint to add to this Jimple breakpoint
	 */
	public void addJavaBreakpoint(IBreakpoint javaBreakpoint) {
		this.javaBreakpoints.add(javaBreakpoint);
	}

	/**
	 * Enables or disabled this breakpoint. It also applies
	 * the change to all of its' Java breakpoints.
	 */
	@Override
	public void setEnabled(boolean enabled) throws CoreException {
		super.setEnabled(enabled);
		for (IBreakpoint javaBreakpoint : javaBreakpoints) {
			javaBreakpoint.setEnabled(enabled);
		}
	}

	/**
	 * This method makes sure, that all associated Java breakpoints
	 * get deleted, when this Jimple breakpoints gets deleted.
	 */
	@Override
	public void delete() throws CoreException {
		for (IBreakpoint javaBreakpoint : javaBreakpoints) {
			javaBreakpoint.delete();
		}
		//super.delete();
	}

	/**
	 * Returns, if this breakpoint is a temporary one. Temporary breakpoints
	 * are used to step through the Jimple code. As soon, as a temporary
	 * breakpoint is hit, it gets deleted, so that only breakpoints set
	 * by the user will remain, when the user analysis is terminated.
	 */
	public boolean isTemporary() {
		return getMarker().getAttribute("Jimple.temporary", false);
	}
}
