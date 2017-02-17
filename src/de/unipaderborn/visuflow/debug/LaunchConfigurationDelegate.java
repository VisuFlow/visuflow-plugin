package de.unipaderborn.visuflow.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Map;
import java.util.Map.Entry;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.debug.core.model.RuntimeProcess;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.debug.monitoring.MonitoringServer;

public class LaunchConfigurationDelegate extends JavaLaunchDelegate {

	private static final transient Logger logger = Visuflow.getDefault().getLogger();

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		ILaunchConfigurationWorkingCopy configCopy = configuration.copy("visuflow");

		String attr = configCopy.getAttribute("test.attr", "fallback");
		System.out.println("Launching: " + attr);

		Map<String, Object> launchAttrs = configCopy.getAttributes();
		for (Entry<String, Object> entry : launchAttrs.entrySet()) {
			System.out.println(entry.getKey() + "=" + entry.getValue());
		}

		// extract and install agent.jar
		File agentJar;
		try {
			agentJar = extractAgent();
			String vmArgs = configCopy.getAttribute("org.eclipse.jdt.launching.VM_ARGUMENTS", "");
			vmArgs = vmArgs + " -javaagent:" + agentJar.getAbsolutePath();
			configCopy.setAttribute("org.eclipse.jdt.launching.VM_ARGUMENTS", vmArgs);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't install visuflow agent", e);
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
							//monitoringServer.stop();

							// delete the agent jar
							if (agentJar != null && agentJar.exists()) {
								agentJar.delete();
							}
						}
					}
				}
			}
		};
		DebugPlugin.getDefault().addDebugEventListener(shutdownListener);


		try {
			logger.info("Waiting for monitoring server before launch");
			if(monitoringServer.waitForServer(5000)) {
				// launch the program
				logger.info("Launching the user analysis");
				super.launch(configCopy, mode, launch, monitor);
			}
		} catch (Exception e) {
			logger.error("Couldn't launch user analysis", e);
		}

	}

	private File extractAgent() throws IOException {
		InputStream in = getClass().getResourceAsStream("/lib/agent.jar");
		OutputStream out = null;
		File agentJar;
		try {
			agentJar = File.createTempFile("agent", ".jar");
			out = new FileOutputStream(agentJar);
			byte[] b = new byte[1024];
			int len = -1;
			while ((len = in.read(b)) >= 0) {
				out.write(b, 0, len);
			}
		} finally {
			if(out != null) {
				out.close();
			}
		}
		return agentJar;
	}
}
