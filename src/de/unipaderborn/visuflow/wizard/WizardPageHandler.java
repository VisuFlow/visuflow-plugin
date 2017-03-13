package de.unipaderborn.visuflow.wizard;

import java.util.HashMap;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class WizardPageHandler extends WizardPage {
	private Text containerTargetText,containerProjectName,containerPackageName,containerClassName;

	@SuppressWarnings("unused")
	private Text fileText;


	public WizardPageHandler(ISelection selection) {
		super("wizardPage");
		setTitle("Create New Analysis Project");
		setDescription("This wizard creates new analysis project based on user-inputs");
		//this.selection = selection;
	}


	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		layout.numColumns = 4;
		layout.verticalSpacing = 15;
		container.setLayout(layout);
		
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		
		Label labelProject = new Label(container, SWT.NULL);
		labelProject.setText("Name of the Project: ");
		labelProject.setToolTipText("Specify the analysis project name");

		containerProjectName = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		containerProjectName.setLayoutData(gd);
		
		new Label(container, SWT.NONE);
		
		Label labelPackage = new Label(container, SWT.NULL);
		labelPackage.setText("Package: ");

		containerPackageName = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		containerPackageName.setLayoutData(gd);
		
		new Label(container, SWT.NONE);
		
		Label labelClass = new Label(container, SWT.NULL);
		labelClass.setText("Class Name: ");
		labelClass.setToolTipText("Name of the class containing main method");

		containerClassName = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		containerClassName.setLayoutData(gd);
		
		new Label(container, SWT.NONE);	
		
		Label labelFile = new Label(container, SWT.NULL);
		labelFile.setText("Choose Target Project:");
		labelFile.setToolTipText("Select target project on which you would like to run the analysis");

		containerTargetText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		containerTargetText.setLayoutData(gd);
		
		Button buttonFile = new Button(container, SWT.PUSH);
		buttonFile.setText("Browse...");
		buttonFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleProjectBrowse();
				dialogChanged();
			}
		});
		
		//initialize();
		//dialogChanged();
		setControl(container);
	}

	
	private void handleProjectBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerTargetText.setText(((Path) result[0]).toString());
			}
		}
	}

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName().get("TargetPath")));
		

		if (getContainerName().get("TargetPath").length() == 0) {
			updateStatus("File container must be specified");
			return;
		}
		if (container == null
				|| (container.getType() & (IResource.PROJECT | IResource.FOLDER)) == 0) {
			updateStatus("File container must exist");
			return;
		}
		if (!container.isAccessible()) {
			updateStatus("Project must be writable");
			return;
		}
		updateStatus(null);
	}

	private void updateStatus(String message) {
		setErrorMessage(message);
		setPageComplete(message == null);
	}

	public HashMap<String, String> getContainerName() {
		HashMap<String, String> containerMap = new HashMap<>();
		containerMap.put("TargetPath", containerTargetText.getText());
		containerMap.put("ProjectName", containerProjectName.getText());
		containerMap.put("PackageName", containerPackageName.getText());
		containerMap.put("ClassName", containerClassName.getText());
		return containerMap;
	}
}
