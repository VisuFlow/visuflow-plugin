package de.unipaderborn.visuflow.debug;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.debug.BreakpointLocator.BreakpointLocation;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class JimpleBreakpointManager implements VisuflowConstants, IResourceChangeListener, EventHandler {

	private static JimpleBreakpointManager instance = new JimpleBreakpointManager();

	private Logger logger = Visuflow.getDefault().getLogger();

	private List<JimpleBreakpoint> breakpoints;
	private BreakpointLocator breakpointLocator = new BreakpointLocator();

	private IJavaThread suspendedThread;
	private IJavaBreakpoint suspendedAtBreakpoint;

	// private constructor, this is a singleton
	private JimpleBreakpointManager() {
		breakpoints = Collections.synchronizedList(new ArrayList<>());
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
		registerAtEventHandler();
	}

	private void registerAtEventHandler() {
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, EA_TOPIC_DEBUGGING_ACTION_ALL);
		ServiceUtil.registerService(EventHandler.class, this, properties);
	}

	public static JimpleBreakpointManager getInstance() {
		return instance;
	}

	public JimpleBreakpoint createBreakpoint(IMarker m) throws CoreException {
		JimpleBreakpoint jimpleBreakpoint = createBreakpointWithoutJavaBreakpoints(m);
		createJavaBreakpoints(jimpleBreakpoint);
		return jimpleBreakpoint;
	}

	private JimpleBreakpoint createBreakpointWithoutJavaBreakpoints(IMarker m) throws CoreException {
		//		int lineNumber = m.getAttribute(IMarker.LINE_NUMBER, -1);
		//		String project = m.getAttribute("Jimple.project", "");
		//		String filePath = m.getAttribute("Jimple.file", "");
		//		IFile file = getFile(project, filePath); //(IFile) m.getResource();
		//		int charStart = m.getAttribute("Jimple.unit.charStart", -1);
		//		int charEnd = m.getAttribute("Jimple.unit.charEnd", -1);
		//		String unitFqn = m.getAttribute("Jimple.unit.fqn", "fqn.not.available");

		// this makes sure, that the jimple editor highlights the line, when an unit breakpoint is hit
		//UnitDebugStepTracker stepTracker = new UnitDebugStepTracker(file, lineNumber, charStart, charEnd, unitFqn);
		//DebugPlugin.getDefault().addDebugEventListener(stepTracker);

		JimpleBreakpoint jimpleBreakpoint = new JimpleBreakpoint(m);
		registerBreakpoint(jimpleBreakpoint);

		return jimpleBreakpoint;
	}

	private void createJavaBreakpoints(JimpleBreakpoint jimpleBreakpoint) throws CoreException {
		IMarker m = jimpleBreakpoint.getMarker();
		List<BreakpointLocation> breakpointLocations = breakpointLocator.findFlowFunctions();
		for (BreakpointLocation breakpointLocation : breakpointLocations) {
			IJavaLineBreakpoint javaBreakpoint = (IJavaLineBreakpoint) createMethodEntryBreapoint(breakpointLocation);
			IJavaLineBreakpoint javaLineBreakpoint = javaBreakpoint;
			javaLineBreakpoint.setConditionEnabled(true);
			String requiredFqn = m.getAttribute("Jimple.unit.fqn", "");
			String escapedRequiredFqn = requiredFqn.replaceAll("\"", "\\\\\"");
			javaLineBreakpoint.setCondition("new String(d.getTag(\"Fully Qualified Name\").getValue()).equals(\""+escapedRequiredFqn+"\")");
			javaLineBreakpoint.setConditionSuspendOnTrue(true);
			IMarker javaBreakpointMarker = javaBreakpoint.getMarker();
			javaBreakpointMarker.setAttribute("Jimple." + IMarker.LINE_NUMBER, m.getAttribute(IMarker.LINE_NUMBER, -1));
			javaBreakpointMarker.setAttribute("Jimple.unit.charStart", m.getAttribute("Jimple.unit.charStart", -1));
			javaBreakpointMarker.setAttribute("Jimple.unit.charEnd", m.getAttribute("Jimple.unit.charEnd", -1));
			javaBreakpointMarker.setAttribute("Jimple.unit.fqn", requiredFqn);
			javaBreakpointMarker.setAttribute("Jimple.project", m.getAttribute("Jimple.project", ""));
			javaBreakpointMarker.setAttribute("Jimple.file", m.getAttribute("Jimple.file", ""));
			jimpleBreakpoint.addJavaBreakpoint(javaBreakpoint);
			javaBreakpoint.addBreakpointListener(JAVA_BREAKPOINT_LISTENER);
		}
	}

	private void registerBreakpoint(JimpleBreakpoint jimpleBreakpoint) throws CoreException {
		DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(jimpleBreakpoint);
		DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(new IBreakpointListener() {
			@Override
			public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
				if (breakpoint instanceof JimpleBreakpoint) {
					try {
						breakpoint.delete();
						DebugPlugin.getDefault().getBreakpointManager().removeBreakpointListener(this);
					} catch (CoreException e) {
						// TODO
						e.printStackTrace();
					}
				}
			}

			@Override
			public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
			}

			@Override
			public void breakpointAdded(IBreakpoint breakpoint) {
			}
		});
	}

	private IBreakpoint createMethodEntryBreapoint(BreakpointLocation location) throws CoreException {
		int charStart = location.offset;
		int charEnd = charStart + location.length;
		int hitCount = 0; // no hit count
		boolean register = true; // register at BreakpointManager
		boolean entry = true; // suspend at method entry
		boolean exit = false; // suspend at method exit
		boolean nativeOnly = false; // suspend for native methods
		Map<String, Object> attrs = null;

		IJavaMethodBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(location.resource, location.className, location.methodName,
				location.methodSignature, entry, exit, nativeOnly, location.lineNumber, charStart, charEnd, hitCount, register, attrs);

		return breakpoint;
	}

	public void addBreakpoint(JimpleBreakpoint breakpoint) {
		breakpoints.add(breakpoint);
	}

	public void removeBreakpoint(JimpleBreakpoint breakpoint) throws CoreException {
		breakpoints.remove(breakpoint);
		breakpoint.delete();
	}

	private void loadBreakpoints(IResource project) {
		try {
			IMarker[] markers = project.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
			List<IMarker> methodBreakpoints = filterJavaMethodBreakpoints(markers);
			List<IMarker> jimpleChildBreakpoints = filterJimpleChildBreakpoints(methodBreakpoints);
			for (IMarker marker : jimpleChildBreakpoints) {
				String unitFqn = (String) marker.getAttribute("Jimple.unit.fqn");
				WorkspaceJob createBreakpoint = new WorkspaceJob("Create Jimple breakpoint") {
					@Override
					public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
						JimpleBreakpoint jimpleBreakpoint = getJimpleBreakpoint(unitFqn);
						if(jimpleBreakpoint == null) {
							IMarker jimpleMarker = createJimpleMarker(marker);
							jimpleBreakpoint = createBreakpointWithoutJavaBreakpoints(jimpleMarker);
							addBreakpoint(jimpleBreakpoint);
						}

						IBreakpoint javaBreakpoint = DebugPlugin.getDefault().getBreakpointManager().getBreakpoint(marker);
						jimpleBreakpoint.addJavaBreakpoint(javaBreakpoint);
						System.out.println("------------------------- JimpleBreakpoint created");
						return Status.OK_STATUS;
					}
				};
				createBreakpoint.schedule();
			}
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	private IMarker createJimpleMarker(IMarker marker) throws CoreException {
		String project = (String) marker.getAttribute("Jimple.project");
		String file = (String) marker.getAttribute("Jimple.file");
		IFile jimpleFile = getFile(project, file);

		IMarker jimpleMarker = null;
		IMarker[] markers = jimpleFile.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true, IResource.DEPTH_INFINITE);
		if(markers != null) {
			List<IMarker> jimpleBreakpoints = filterJimpleChildBreakpoints(Arrays.asList(markers));
			if(!jimpleBreakpoints.isEmpty()) {
				jimpleMarker = jimpleBreakpoints.get(0);
			} else {
				jimpleMarker = jimpleFile.createMarker(IBreakpoint.BREAKPOINT_MARKER);
			}
		} else {
			jimpleMarker = jimpleFile.createMarker(IBreakpoint.BREAKPOINT_MARKER);
		}

		jimpleMarker.setAttribute(IMarker.LINE_NUMBER, marker.getAttribute("Jimple." + IMarker.LINE_NUMBER));
		jimpleMarker.setAttribute("Jimple.unit.charStart", marker.getAttribute("Jimple.unit.charStart"));
		jimpleMarker.setAttribute("Jimple.unit.charEnd", marker.getAttribute("Jimple.unit.charEnd"));
		jimpleMarker.setAttribute("Jimple.unit.fqn", marker.getAttribute("Jimple.unit.fqn"));
		jimpleMarker.setAttribute("Jimple.project", marker.getAttribute("Jimple.project"));
		jimpleMarker.setAttribute("Jimple.file", marker.getAttribute("Jimple.file"));
		return jimpleMarker;
	}

	private List<IMarker> filterJimpleChildBreakpoints(List<IMarker> methodBreakpoints) {
		return methodBreakpoints.stream().filter(m -> {
			try {
				return m.getAttribute("Jimple.unit.fqn") != null;
			} catch (CoreException e) {
				logger.error("Couldn't filter persisted breakpoints for jimple breakpoints", e);
				return false;
			}
		}).collect(Collectors.toList());
	}

	private List<IMarker> filterJavaMethodBreakpoints(IMarker[] markers) throws CoreException {
		List<IMarker> filteredList = new ArrayList<>();
		for (IMarker marker : markers) {
			if(marker.getType().equals("org.eclipse.jdt.debug.javaMethodBreakpointMarker")) {
				filteredList.add(marker);
			}
		}
		return filteredList;
	}

	private IFile getFile(String projectName, String path) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IFile file = project.getFile(path);
		return file;
	}

	private JimpleBreakpoint getJimpleBreakpoint(String unitFqn) throws CoreException {
		for (JimpleBreakpoint jimpleBreakpoint : breakpoints) {
			IMarker m = jimpleBreakpoint.getMarker();
			String markerFqn = (String) m.getAttribute("Jimple.unit.fqn");
			if(markerFqn != null && markerFqn.equals(unitFqn)) {
				return jimpleBreakpoint;
			}
		}
		return null;
	}

	void breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		suspendedThread = thread;
		suspendedAtBreakpoint = breakpoint;
	}

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta= event.getDelta();
		if (delta != null) {
			try {
				delta.accept(new BreakpointManagerVisitor());
			} catch (CoreException ce) {
				logger.error("Couldn't run visitor to restore breakpoints", ce);
			}
		}
	}

	private class BreakpointManagerVisitor implements IResourceDeltaVisitor {
		@Override
		public boolean visit(IResourceDelta delta) {
			if (delta == null) {
				return false;
			}
			if (0 != (delta.getFlags() & IResourceDelta.OPEN) && 0 == (delta.getFlags() & IResourceDelta.MOVED_FROM)) {
				handleProjectResourceOpenStateChange(delta.getResource());
				return false;
			}
			return true;
		}

		/**
		 * A project has been opened or closed.  Updates the breakpoints for
		 * that project
		 * @param project the {@link IProject} that was changed
		 */
		private void handleProjectResourceOpenStateChange(final IResource project) {
			if (!project.isAccessible()) {
				//closed
				for (IBreakpoint breakpoint : breakpoints) {
					IResource markerResource= breakpoint.getMarker().getResource();
					if (project.getFullPath().isPrefixOf(markerResource.getFullPath())) {
						// TODO remove breakpoints ?!?
					}
				}
				return;
			}
			loadBreakpoints(project);
		}
	}

	@Override
	public void handleEvent(Event event) {
		String topic = event.getTopic();
		if(topic.equals(EA_TOPIC_DEBUGGING_ACTION_RESUME)) {
			handleResumeEvent();
		} else if(topic.equals(EA_TOPIC_DEBUGGING_ACTION_STEP_OVER)) {
			handleStepOverEvent();
		}
	}

	private void handleResumeEvent() {
		try {
			removeTemporaryBreakpoints();
			suspendedThread.resume();
		} catch (Exception e) {
			logger.error("Couldn't resume suspended thread", e);
		}
	}

	private void handleStepOverEvent() {
		IMarker marker = suspendedAtBreakpoint.getMarker();
		try {
			// find the next unit to stop at
			String unitFqn = marker.getAttribute("Jimple.unit.fqn").toString();
			VFUnit nextUnitToStopAt = findNextUnit(unitFqn);

			// TODO disable the currently active unit breakpoint by disabling all of its' conditional java breakpoints

			// create a new JimpleBreakpoint for the found unit
			IMarker newMarker = createBreakpointMarkerForNextUnit(nextUnitToStopAt, marker);

			// remove previous set temporary breakpoints
			removeTemporaryBreakpoints();

			// create a new temporary breakpoint
			JimpleBreakpoint jimpleBreakpoint = createBreakpoint(newMarker);
			addBreakpoint(jimpleBreakpoint);

			// now resume the thread so that it can reach the new breakpoints
			suspendedThread.resume();
		} catch (Exception e) {
			logger.error("Couldn't execute \"step over\" in suspended thread", e);
		}
	}

	public void removeTemporaryBreakpoints() throws CoreException {
		for (Iterator<JimpleBreakpoint> iterator = breakpoints.iterator(); iterator.hasNext();) {
			JimpleBreakpoint jimpleBreakpoint = iterator.next();
			if(jimpleBreakpoint.isTemporary()) {
				DebugPlugin.getDefault().getBreakpointManager().removeBreakpoint(jimpleBreakpoint, true);
				iterator.remove();
				jimpleBreakpoint.getMarker().delete();
			}
		}
	}

	private IMarker createBreakpointMarkerForNextUnit(VFUnit nextUnitToStopAt, IMarker marker) throws CoreException, IOException {
		UnitLocation location = UnitLocator.locateUnit(nextUnitToStopAt);
		// FIXME for now we use the same resource, but to support stepping beyond the end of a method, the actual resource
		// has to be determined
		IFile file = location.project.getFile(location.jimpleFile);

		IMarker m = file.createMarker(marker.getType());
		m.setAttribute(IMarker.LINE_NUMBER, location.line);
		m.setAttribute("Jimple.file", file.getProjectRelativePath().toString());
		m.setAttribute("Jimple.project", marker.getAttribute("Jimple.project"));
		m.setAttribute("Jimple.unit.charStart", location.charStart);
		m.setAttribute("Jimple.unit.charEnd", location.charEnd);
		m.setAttribute("Jimple.unit.fqn", nextUnitToStopAt.getFullyQualifiedName());
		m.setAttribute("Jimple.temporary", true);
		return m;
	}

	private VFUnit findNextUnit(String unitFqn) {
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		VFUnit unit = dataModel.getVFUnit(unitFqn);
		VFUnit unitAfter = unit.getVfMethod().getUnitAfter(unit);
		// FIXME handle end of method (at the moment a NoSuchElementException is thrown)
		return unitAfter;
	}
}
