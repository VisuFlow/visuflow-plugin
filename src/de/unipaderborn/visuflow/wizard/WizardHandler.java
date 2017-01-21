package de.unipaderborn.visuflow.wizard;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IFolder;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.Status;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.eclipse.ui.IWorkbenchWizard;
import de.unipaderborn.visuflow.builder.GlobalSettings;

public class WizardHandler extends Wizard implements INewWizard {

	private WizardPageHandler page;
	private ISelection selection;

	/**
	 * Constructor for SampleNewWizard.
	 */
	public WizardHandler() {
		super();
		setNeedsProgressMonitor(true);
	}
	
	/**
	 * Adding the page to the wizard.
	 */

	public void addPages() {
		page = new WizardPageHandler(selection);
		addPage(page);
	}

	/**
	 * This method is called when 'Finish' button is pressed in
	 * the wizard. We will create an operation and run it
	 * using wizard as execution context.
	 */
	public boolean performFinish() {
		final String analysisProjectPath = page.getContainerName().get("ProjectPath");
		final String targetProjectPath = page.getContainerName().get("TargetPath");
		System.out.println("Anaysis path "+analysisProjectPath);
		System.out.println("Target path "+targetProjectPath);
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(analysisProjectPath, targetProjectPath, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} finally {
					monitor.done();
				}
			}
		};
		try {
			getContainer().run(true, false, op);
		} catch (InterruptedException e) {
			return false;
		} catch (InvocationTargetException e) {
			Throwable realException = e.getTargetException();
			MessageDialog.openError(getShell(), "Error", realException.getMessage());
			return false;
		}
		return true;
	}
	
	/**
	 * The worker method. It will find the container, create the
	 * file if missing or just replace its contents, and open
	 * the editor on the newly created file.
	 * @throws FileNotFoundException 
	 */

	private void doFinish(
		String analysisProjectPath,
		String targetProjectPath,
		IProgressMonitor monitor)
		throws CoreException, FileNotFoundException {
		// create a sample file
		monitor.beginTask("Creating " + analysisProjectPath, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resourceAnalysis = root.findMember(new Path(analysisProjectPath));
		IResource resourceTarget = root.findMember(new Path(targetProjectPath));
		System.out.println("Target Resource is "+resourceTarget.getProject());
		if (!resourceAnalysis.exists() || !(resourceAnalysis instanceof IContainer)) {
			throwCoreException("Container \"" + analysisProjectPath + "\" does not exist.");
		}
		
		IContainer containerAnalysis = (IContainer) resourceAnalysis;
		IJavaProject javaProject = JavaCore.create(resourceTarget.getProject());
		String key = "TargetProject_"+containerAnalysis.getProject().getName();
		GlobalSettings.put(key,root.getLocation().toFile()+javaProject.getOutputLocation().toFile().getPath());
		IJavaProject analysisProject = JavaCore.create(resourceAnalysis.getProject());
		GlobalSettings.put("AnalysisProject", analysisProject.getProject().getName());
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {

			}
		});
		monitor.worked(1);
	}
	
	/**
	 * We will initialize file contents with a sample text.
	 * @throws CoreException 
	 * @throws FileNotFoundException 
	 */
	
	void copyFiles (File srcFolder, IContainer destFolder, IProgressMonitor monitor) throws CoreException, FileNotFoundException {
	    for (File f: srcFolder.listFiles()) {
	        if (f.isDirectory()) {
	            IFolder newFolder = destFolder.getFolder(new Path(f.getName()));
	            newFolder.create(true, true, monitor);
	            copyFiles(f, newFolder, monitor);
	        } else {
	            IFile newFile = destFolder.getFile(new Path(f.getName()));
	            newFile.create(new FileInputStream(f), true, monitor);
	        }
	    }
	}
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "TestPlugIn", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	/**
	 * We will accept the selection in the workbench to see if
	 * we can initialize from it.
	 * @see IWorkbenchWizard#init(IWorkbench, IStructuredSelection)
	 */
	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}
