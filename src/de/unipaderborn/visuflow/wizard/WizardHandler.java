package de.unipaderborn.visuflow.wizard;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
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
		final String containerName = page.getContainerName().get("ProjectPath");
		final String containerName1 = page.getContainerName().get("TargetPath");
		System.out.println("Container name is "+containerName);
		System.out.println("Container name is "+containerName1);
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(containerName, containerName1, monitor);
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
		String containerName,
		String containerName1,
		IProgressMonitor monitor)
		throws CoreException, FileNotFoundException {
		// create a sample file
		monitor.beginTask("Creating " + containerName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resource = root.findMember(new Path(containerName));
		IResource resource1 = root.findMember(new Path(containerName1));
		File fileDir = new File(containerName1);
		System.out.println("file directory is "+fileDir.getName());
		System.out.println("File directory is directory "+fileDir.isDirectory());
		
		if (!resource.exists() || !(resource instanceof IContainer)) {
			throwCoreException("Container \"" + containerName + "\" does not exist.");
		}
		
		IContainer container = (IContainer) resource;
		IContainer container1 = (IContainer) resource1;
		IJavaProject javaProject = JavaCore.create(resource1.getProject());
		System.out.println("Java Project Location is "+javaProject.getOutputLocation().toFile().getAbsolutePath());
		System.out.println("Project is "+container1.getProject());
		String key = "TargetProject_"+container.getProject().getName(); 
		//add here
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
	
	@SuppressWarnings("unused")
	private InputStream openContentStream() {
		String contents =
			"This is the initial file contents for *.java file that should be word-sorted in the Preview page of the multi-page editor";
		return new ByteArrayInputStream(contents.getBytes());
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
