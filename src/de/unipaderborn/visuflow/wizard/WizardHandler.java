package de.unipaderborn.visuflow.wizard;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

import org.eclipse.core.internal.runtime.Activator;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ProjectScope;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.Path;
import org.eclipse.core.runtime.preferences.IEclipsePreferences;
import org.eclipse.core.runtime.preferences.IScopeContext;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.operation.IRunnableWithProgress;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.ui.INewWizard;
import org.eclipse.ui.IWorkbench;
import org.osgi.service.prefs.BackingStoreException;

import de.unipaderborn.visuflow.ProjectPreferences;
import de.unipaderborn.visuflow.builder.GlobalSettings;

public class WizardHandler extends Wizard implements INewWizard {

	private WizardPageHandler page;
	private WizardHandlerPageTwo pageTwo;
	private ISelection selection;
	private WizardInput wizardInput;

	public WizardHandler() {
		super();
		setNeedsProgressMonitor(true);
	}

	public void addPages() {
		page = new WizardPageHandler(selection);
		addPage(page);
		pageTwo = new WizardHandlerPageTwo(selection);
		addPage(pageTwo);
	}
	
	@Override
	public boolean canFinish() {
		if(getContainer().getCurrentPage() == page)
			return false;
			else 
			return true;
	}

	public boolean performFinish() {
		final String targetProjectPath = page.getContainerName().get("TargetPath");
		final String analysisProjectName = page.getContainerName().get("ProjectName");
		wizardInput = new WizardInput();
		wizardInput.setTargetPath(targetProjectPath);
		wizardInput.setProjectName(analysisProjectName);
		wizardInput.setPackageName(page.getContainerName().get("PackageName"));
		wizardInput.setClassName(page.getContainerName().get("ClassName"));
		wizardInput.setAnalysisType(page.getContainerName().get("AnalysisType"));
		wizardInput.setAnalysisFramework(page.getContainerName().get("AnalysisFramework"));
		wizardInput.setAnalysisDirection(pageTwo.getContainerName().get("AnalysisDirection"));
		wizardInput.setFlowType(pageTwo.getContainerName().get("FlowSet"));
		wizardInput.setFlowType1(pageTwo.getContainerName().get("Type1"));
		wizardInput.setFlowtype2(pageTwo.getContainerName().get("Type2"));
		wizardInput.setCustomClassFirst(pageTwo.getContainerName().get("CustomType1"));
		wizardInput.setCustomClassSecond(pageTwo.getContainerName().get("CustomType2"));
		String sootLocation = pageTwo.getContainerName().get("sootLocation");
		Path sootPath = new Path(sootLocation);
		wizardInput.setSootPath(sootPath);
		IRunnableWithProgress op = new IRunnableWithProgress() {
			public void run(IProgressMonitor monitor) throws InvocationTargetException {
				try {
					doFinish(targetProjectPath, analysisProjectName, monitor);
				} catch (CoreException e) {
					throw new InvocationTargetException(e);
				} catch (FileNotFoundException e) {
					
					e.printStackTrace();
				} catch (IOException e) {
					
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
			System.out.println(realException.getStackTrace().toString());
			return false;
		}
		return true;
	}

	private void doFinish(
		String targetProjectPath,
		String analysisProjectName,
		IProgressMonitor monitor)
		throws CoreException, IOException {
		monitor.beginTask("Creating " +analysisProjectName, 2);
		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		ProjectGenerator projectGen = new ProjectGenerator();
		IJavaProject sourceProject = projectGen.createProject(wizardInput);
		IResource resourceTarget = root.findMember(new Path(targetProjectPath));
		IJavaProject targetProject = JavaCore.create(resourceTarget.getProject());
		GlobalSettings.put("Target_Path",resourceTarget.getLocation().toOSString()+ File.separator +  targetProject.getOutputLocation().lastSegment());
		GlobalSettings.put("AnalysisProject", sourceProject.getProject().getName());
		GlobalSettings.put("TargetProject", targetProject.getProject().getName());
		ProjectPreferences projPref = new ProjectPreferences();
		projPref.createPreferences();
		monitor.worked(1);
		monitor.setTaskName("Opening file for editing...");
		getShell().getDisplay().asyncExec(new Runnable() {
			public void run() {

			}
		});
		monitor.worked(1);
	}

	public void init(IWorkbench workbench, IStructuredSelection selection) {
		this.selection = selection;
	}

}
