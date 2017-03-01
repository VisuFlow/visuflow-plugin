package de.unipaderborn.visuflow.debug;

import java.io.File;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.RuntimeProcess;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.debug.monitoring.MonitoringServer;

public class TerminationListener implements IDebugEventSetListener, VisuflowConstants {
	private Logger logger = Visuflow.getDefault().getLogger();

	private String projectName;
	private MonitoringServer monitoringServer;
	private File agentJar;

	public TerminationListener(String projectName, MonitoringServer monitoringServer, File agentJar) {
		this.projectName = projectName;
		this.monitoringServer = monitoringServer;
		this.agentJar = agentJar;
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		// System.out.println("Events " + events.length);
		for (int i = 0; i < events.length; i++) {
			DebugEvent debugEvent = events[i];
			if (debugEvent.getKind() == DebugEvent.TERMINATE) {
				// this event is fired for each thread and stuff, but we only want to remove our breakpoints,
				// when the JVM process terminates
				if (debugEvent.getSource() instanceof RuntimeProcess) {
					// remove this debug event listener to release it for garbage collection
					DebugPlugin.getDefault().removeDebugEventListener(this);

					// stop the monitoring server
					monitoringServer.stop();

					// delete the agent jar
					if (agentJar != null && agentJar.exists()) {
						agentJar.delete();
					}

					// remove the jimple instruction pointer marker (green debug line highlighting)
					removeJimpleInstructionPointerMarker();
				}
			}
		}
	}

	private void removeJimpleInstructionPointerMarker() {
		try {
			IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
			project.deleteMarkers(JIMPLE_INSTRUCTIONPOINTER_MARKER, true, IResource.DEPTH_INFINITE);
		} catch (CoreException e) {
			logger.error("Couldn't remove jimple instruction pointer marker", e);
		}
	}
}
