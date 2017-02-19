package de.unipaderborn.visuflow.wizard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.resources.IContainer;
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
import de.unipaderborn.visuflow.builder.GlobalSettings;

public class WizardHandler extends Wizard implements INewWizard {

	private WizardPageHandler page;
	private WizardHandlerPageTwo pageTwo;
	private ISelection selection;
	private WizardInput wizardInput;

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
		pageTwo = new WizardHandlerPageTwo(selection);
		addPage(pageTwo);
	}

	public boolean performFinish() {
		final String analysisProjectPath = page.getContainerName().get("ProjectPath");
		final String targetProjectPath = page.getContainerName().get("TargetPath");
		final String analysisProjectName = page.getContainerName().get("ProjectName");
		wizardInput = new WizardInput();
		wizardInput.setFlowType(pageTwo.getContainerName().get("FlowSet"));
		wizardInput.setFlowType1(pageTwo.getContainerName().get("Type1"));
		wizardInput.setFlowtype2(pageTwo.getContainerName().get("Type2"));
		wizardInput.setClassNameFirst(pageTwo.getContainerName().get("CustomType1"));
		wizardInput.setClassNameSecond(pageTwo.getContainerName().get("CustomType2"));
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(analysisProjectPath, targetProjectPath, analysisProjectName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} catch (FileNotFoundException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				} catch (IOException e) {
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

	private void doFinish(
		String analysisProjectPath,
		String targetProjectPath,
		String analysisProjectName,
		IProgressMonitor monitor)
		throws CoreException, IOException {
		// create a sample file
		monitor.beginTask("Creating " + analysisProjectPath, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ProjectGenerator projectGen = new ProjectGenerator();
		IJavaProject sourceProject = projectGen.createProject(analysisProjectName,wizardInput);
		IResource resourceAnalysis = root.findMember(new Path(analysisProjectPath));
		IResource resourceTarget = root.findMember(new Path(targetProjectPath));
		if (!resourceAnalysis.exists() || !(resourceAnalysis instanceof IContainer)) {
			throwCoreException("Container \"" + analysisProjectPath + "\" does not exist.");
		}
		
		//IContainer containerAnalysis = (IContainer) resourceAnalysis;
		IJavaProject targetProject = JavaCore.create(resourceTarget.getProject());
		String key = "TargetProject_"+sourceProject.getProject().getName();
		GlobalSettings.put(key,resourceTarget.getLocation().toOSString()+ File.separator +  targetProject.getOutputLocation().lastSegment());
		//IJavaProject analysisProject = JavaCore.create(resourceAnalysis.getProject());
		GlobalSettings.put("AnalysisProject", sourceProject.getProject().getName());
		GlobalSettings.put("TargetProject", targetProject.getProject().getName());
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {

			}
		});
		monitor.worked(1);
	}
	
	private void throwCoreException(String message) throws CoreException {
		IStatus status =
			new Status(IStatus.ERROR, "TestPlugIn", IStatus.OK, message, null);
		throw new CoreException(status);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}
