package de.unipaderborn.visuflow.debug;

import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import de.unipaderborn.visuflow.debug.monitoring.MonitoringServer;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class LaunchConfigurationDelegate extends JavaLaunchDelegate {

	private DataModel dataModel = ServiceUtil.getService(DataModel.class);

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		String attr = configuration.getAttribute("test.attr", "fallback");
		System.out.println("Launching: " + attr);

		Map<String, Object> launchAttrs = configuration.getAttributes();
		for (Entry<String, Object> entry : launchAttrs.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		// launch monitoring server
		MonitoringServer monitoringServer = new MonitoringServer();
		monitoringServer.start();

		IDebugEventSetListener shutdownListener = new IDebugEventSetListener() {
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
							monitoringServer.stop();
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(shutdownListener);

		// launch the program
		super.launch(configuration, mode, launch, monitor);
	}
}
