package de.unipaderborn.visuflow.builder;

import java.util.Iterator;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IProjectDescription;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IAdaptable;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.ui.handlers.HandlerUtil;

import de.unipaderborn.visuflow.VisuflowConstants;

/**
 * @author PAH-Laptop
 * 
 * This class handles adding the Visuflow Project Nature to Projects.
 * This enables Eclipse to identify if it has to run our builder on a specific
 * Project.
 *
 */
public class AddRemoveVisuFlowNatureHandler extends AbstractHandler implements VisuflowConstants {

	
	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 * this Method executes the Handler and thus toggles the nature status on a specific project, 
	 * by calling the toggleNature method.
	 */
	public Object execute(ExecutionEvent event) throws ExecutionException {
		ISelection selection = HandlerUtil.getCurrentSelection(event);
		//
		if (selection instanceof IStructuredSelection) {
			for (Iterator<?> it = ((IStructuredSelection) selection).iterator(); it
					.hasNext();) {
				Object element = it.next();
				IProject project = null;
				if (element instanceof IProject) {
					project = (IProject) element;
				} else if (element instanceof IAdaptable) {
					project = ((IAdaptable) element)
							.getAdapter(IProject.class);
				}
				if (project != null) {
					try {
						toggleNature(project);
					} catch (CoreException e) {
						throw new ExecutionException("Failed to toggle nature",
								e);
					}
				}
			}
		}

		return null;
	}

	/**
	 * Toggles Visuflow nature on a project
	 *
	 * @param project
	 *            to have Visuflow nature added or removed
	 */
	public void toggleNature(IProject project) throws CoreException {
		IProjectDescription description = project.getDescription();
		String[] natures = description.getNatureIds();

		for (int i = 0; i < natures.length; ++i) {
			if (VISUFLOW_NATURE.equals(natures[i])) {
				// Remove the nature
				String[] newNatures = new String[natures.length - 1];
				System.arraycopy(natures, 0, newNatures, 0, i);
				System.arraycopy(natures, i + 1, newNatures, i, natures.length - i - 1);
				description.setNatureIds(newNatures);
				project.setDescription(description, null);
				return;
			}
		}

		// Add the nature
		String[] newNatures = new String[natures.length + 1];
		System.arraycopy(natures, 0, newNatures, 0, natures.length);
		newNatures[natures.length] = VISUFLOW_NATURE;
		description.setNatureIds(newNatures);
		project.setDescription(description, null);
		for(String a : project.getDescription().getNatureIds()){
			System.out.println(a);
		}
	}

}