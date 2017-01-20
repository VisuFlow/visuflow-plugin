package de.unipaderborn.visuflow;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.IResourceDelta;
import org.eclipse.core.resources.IResourceDeltaVisitor;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;

import de.unipaderborn.visuflow.builder.VisuFlowNature;

public class ProjectListener implements IResourceChangeListener {

	@Override
	public void resourceChanged(IResourceChangeEvent event) {
		IResourceDelta delta = event.getDelta();
		if (delta.getResource().getType() == IResource.ROOT) {
			List<IProject> projects = getProjects(event.getDelta());
			// do something with new projects
			for (IProject project : projects) {
				try {
					triggerBuild(project);
				} catch (CoreException e) {
					e.printStackTrace();
				}
			}
		}
	}

	public void triggerBuild(IProject project) throws CoreException {
		if (isVisuflowNatureEnabled(project) && ResourcesPlugin.getWorkspace().isAutoBuilding()) {
			WorkspaceJob fullBuild = new WorkspaceJob("Full build for " + project.getName()) {
				@Override
				public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
					project.build(IncrementalProjectBuilder.FULL_BUILD, monitor);
					return Status.OK_STATUS;
				}
			};
			fullBuild.schedule();
		}
	}

	private boolean isVisuflowNatureEnabled(IProject project) throws CoreException {
		return project.isOpen() && project.hasNature(VisuFlowNature.NATURE_ID) && project.isNatureEnabled(VisuFlowNature.NATURE_ID);
	}

	private List<IProject> getProjects(IResourceDelta delta) {
		final List<IProject> projects = new ArrayList<>();
		try {
			delta.accept(new IResourceDeltaVisitor() {
				@Override
				public boolean visit(IResourceDelta delta) throws CoreException {
					boolean opened = (delta.getFlags() & IResourceDelta.OPEN) == IResourceDelta.OPEN;
					if (opened && delta.getResource().getType() == IResource.PROJECT) {
						IProject project = (IProject) delta.getResource();
						if (project.isAccessible()) {
							projects.add(project);
						}
					}
					// only continue for the workspace root
					return delta.getResource().getType() == IResource.ROOT;
				}
			});
		} catch (CoreException e) {
			// handle error
		}
		return projects;
	}
}