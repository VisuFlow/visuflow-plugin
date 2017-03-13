package de.unipaderborn.visuflow;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceVisitor;
import org.eclipse.core.resources.IWorkspace;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.ui.plugin.AbstractUIPlugin;
import org.osgi.framework.BundleContext;

import de.unipaderborn.visuflow.debug.JimpleBreakpointManager;

public class Visuflow extends AbstractUIPlugin {
	public static Visuflow plugin;
	public static BundleContext context;
	private Logger logger;

	/**
	 * A resource listener, which observes the workspace for projects
	 * to become available. A new project is then build (full build) immediately.
	 */
	private ProjectListener projectListener = new ProjectListener();

	@Override
	public void start(BundleContext context) {

		Visuflow.context = context;
		plugin = this;
		logger = new Logger(getLog());
		logger.info("Visuflow plug-in starting...");
		JimpleBreakpointManager.getInstance();

		// iterate over all projects in the workspace to find projects with the
		// visuflow nature. for these projects we trigger a full build to fill
		// the data model directly after the launch. there is also the ProjectListener,
		// which triggers a build for newly opened projects (e.g. opening a closed one or
		// importing a project etc.)
		// the builds are only executed, if the workspace is set to auto build
		IWorkspace workspace = ResourcesPlugin.getWorkspace();
		workspace.addResourceChangeListener(projectListener, IResourceChangeEvent.POST_CHANGE);
		if(workspace.isAutoBuilding()) {
			IWorkspaceRoot root = workspace.getRoot();
			try {
				root.accept(new IResourceVisitor() {
					@Override
					public boolean visit(IResource resource) throws CoreException {
						if(resource.getType() == IResource.PROJECT) {
							IProject project = (IProject) resource;
							projectListener.triggerBuild(project);
							return false;
						}
						return true;
					}
				});
			} catch (CoreException e) {
				logger.error("Error while trying to build visuflow projects", e);
			}
		}
	}

	@Override
	public void stop(BundleContext context) {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(projectListener);
		Visuflow.context = null;
	}

	public static Visuflow getDefault() {
		return plugin;
	}

	public Logger getLogger() {
		return logger;
	}
}
