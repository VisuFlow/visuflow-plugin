package de.unipaderborn.visuflow.debug.ui;

import java.util.Collections;
import java.util.Set;

import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTargetFactory;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.ui.IWorkbenchPart;

import de.unipaderborn.visuflow.VisuflowConstants;

/**
 * Glue code to enable the double-click behavior in the ruler of the JimpleEditor
 * @author henni@upb.de
 *
 */
public class JimpleBreakpointToggleTargetFactory implements IToggleBreakpointsTargetFactory, VisuflowConstants {

	@Override
	public Set<String> getToggleTargets(IWorkbenchPart part, ISelection selection) {
		return Collections.singleton(VISUFLOW_TOGGLE_BREAKPOINTS_TARGET);
	}

	@Override
	public String getDefaultToggleTarget(IWorkbenchPart part, ISelection selection) {
		return VISUFLOW_TOGGLE_BREAKPOINTS_TARGET;
	}

	@Override
	public IToggleBreakpointsTarget createToggleTarget(String targetID) {
		return new ToggleJimpleBreakpointsTarget();
	}

	@Override
	public String getToggleTargetName(String targetID) {
		return "Toggle Jimple Breakpoint";
	}

	@Override
	public String getToggleTargetDescription(String targetID) {
		return null;
	}

}
