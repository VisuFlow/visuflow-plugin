package de.unipaderborn.visuflow.debug;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;

import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.ILaunch;
import org.eclipse.debug.core.ILaunchConfiguration;
import org.eclipse.debug.core.ILaunchConfigurationWorkingCopy;
import org.eclipse.jdt.launching.JavaLaunchDelegate;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.debug.monitoring.MonitoringServer;

public class LaunchConfigurationDelegate extends JavaLaunchDelegate implements VisuflowConstants {

	private static final transient Logger logger = Visuflow.getDefault().getLogger();

	@Override
	public void launch(ILaunchConfiguration configuration, String mode, ILaunch launch, IProgressMonitor monitor) throws CoreException {
		ILaunchConfigurationWorkingCopy configCopy = configuration.copy("visuflow");
		String projectName = configCopy.getAttribute("org.eclipse.jdt.launching.PROJECT_ATTR", "");

		// extract and install agent.jar
		File agentJar = extractAndInstallAgent(configCopy);

		// launch monitoring server
		MonitoringServer monitoringServer = new MonitoringServer();
		monitoringServer.start();

		// add a listener to react to the termination of the lanuched JVM
		// we have to shutdown the MonitoringServer and clean up
		TerminationListener shutdownListener = new TerminationListener(projectName, monitoringServer, agentJar);
		DebugPlugin.getDefault().addDebugEventListener(shutdownListener);

		try {
			logger.info("Waiting for monitoring server before launch");
			if(monitoringServer.waitForServer(500)) {
				// launch the program
				logger.info("Launching the user analysis");
				super.launch(configCopy, mode, launch, monitor);
			}
		} catch (Exception e) {
			logger.error("Couldn't launch user analysis", e);
		}

	}

	private File extractAndInstallAgent(ILaunchConfigurationWorkingCopy configCopy) throws CoreException {
		File agentJar;
		try {
			agentJar = extractAgent();
			String vmArgs = configCopy.getAttribute("org.eclipse.jdt.launching.VM_ARGUMENTS", "");
			vmArgs = vmArgs + " -javaagent:" + agentJar.getAbsolutePath();
			configCopy.setAttribute("org.eclipse.jdt.launching.VM_ARGUMENTS", vmArgs);
		} catch (IOException e) {
			throw new RuntimeException("Couldn't install visuflow agent", e);
		}
		return agentJar;
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
