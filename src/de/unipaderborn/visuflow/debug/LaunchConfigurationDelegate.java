package de.unipaderborn.visuflow.debug;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.core.model.IDebugTarget;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaVariable;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import de.unipaderborn.visuflow.debug.ui.BreakpointLocator;
import de.unipaderborn.visuflow.debug.ui.BreakpointLocator.BreakpointLocation;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class LaunchConfigurationDelegate extends JavaLaunchDelegate {

	private DataModel dataModel = ServiceUtil.getService(DataModel.class);
	private IDebugTarget target;
	private List<IBreakpoint> breakpoints = new ArrayList<>();
	private IDebugEventSetListener listener = new IDebugEventSetListener() {
		@Override
		public void handleDebugEvents(DebugEvent[] events) {
			for (DebugEvent debugEvent : events) {
				//System.out.println(debugEvent);
				if (debugEvent.getKind() == DebugEvent.SUSPEND && debugEvent.getDetail() == DebugEvent.BREAKPOINT) {
					IJavaThread thread = (IJavaThread) debugEvent.getSource();
					try {
						IJavaStackFrame top = (IJavaStackFrame) thread.getTopStackFrame();
						if(top == null) {
							continue;
						}

						IBreakpoint[] breakpoints = thread.getBreakpoints();
						for (IBreakpoint breakpoint : breakpoints) {
							IJavaMethodBreakpoint methodBreakpoint = breakpoint.getAdapter(IJavaMethodBreakpoint.class);
							if(methodBreakpoint != null) {
								IJavaVariable var;
								if(methodBreakpoint.isEntrySuspend(target)) {
									// method entry
									var = top.findVariable("inSet"); // TODO determine the variable names in BreakpointLocator or so
									dataModel.setInSet(null, var.getName(), var.getValue().getValueString());
								} else {
									// method exit
									var = top.findVariable("outSet"); // TODO determine the variable names in BreakpointLocator or so
									dataModel.setOutSet(null, var.getName(), var.getValue().getValueString());
								}

								if (var != null) {
									// TODO find a better way to determine, that is breakpoint has been set by us
									// only then we should resume the process
									target.resume();
								}
							}
						}
					} catch (Exception e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				} else if(debugEvent.getKind() == DebugEvent.TERMINATE) {
					// remove this debug event listener to release it for garbage collection
					DebugPlugin.getDefault().removeDebugEventListener(this);

					// remove breakpoints
					// TODO this is the wrong place to do it, occasionally exceptions are thrown
					for (IBreakpoint breakpoint : breakpoints) {
						try {
							DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(breakpoint, true);
						} catch (CoreException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					}
					breakpoints.clear();
				}
			}
		}
	};

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String attr = configuration.getAttribute("test.attr", "fallback");
		System.out.println("Launching: " + attr);

		Map<String, Object> launchAttrs = configuration.getAttributes();
		for (Entry<String, Object> entry : launchAttrs.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		// find flow functions to observe in-sets and out-sets
		BreakpointLocator locator = new BreakpointLocator();
		List<BreakpointLocation> locations = locator.findFlowFunctions();
		for (BreakpointLocation breakpointLocation : locations) {
			createMethodEntryBreapoint(breakpointLocation);
		}

		// add DebugEventListener, so that we can inspect variable values
		// at the breakpoints
		DebugPlugin.getDefault().addDebugEventListener(listener);

		// launch the program
		super.launch(configuration, mode, launch, monitor);
		target = launch.getDebugTarget();
	}

	private IBreakpoint createMethodEntryBreapoint(BreakpointLocation location) throws CoreException {
		int charStart = location.offset;
		int charEnd = charStart + location.length;
		int hitCount = 0; // no hit count
		boolean register = true; // register at BreakpointManager
		boolean entry = true; // suspend at method entry
		boolean exit = true; // suspend at method exit
		boolean nativeOnly = false; // suspend for native methods
		Map<String, Object> attrs = null;

		System.out.println("resource: " + location.resource);
		System.out.println("class: " + location.className);
		System.out.println("method: " + location.methodName);
		System.out.println("signature: " + location.methodSignature);
		System.out.println("resource: " + location.resource);
		System.out.println("entry: " + entry);
		System.out.println("exit: " + exit);
		System.out.println("nativeOnly: " + nativeOnly);
		System.out.println("line: " + location.lineNumber);
		System.out.println("charStart: " + charStart);
		System.out.println("charEnd: " + charEnd);
		System.out.println("hit count: " + hitCount);
		System.out.println("register: " + register);
		System.out.println("attrs: " + attrs);

		IJavaMethodBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(location.resource, location.className, location.methodName,
				location.methodSignature, entry, exit, nativeOnly, location.lineNumber, charStart, charEnd, hitCount, register, attrs);
		breakpoints.add(breakpoint);
		return breakpoint;
	}
}
