package de.unipaderborn.visuflow.wizard;

import java.util.HashMap;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.dialogs.IDialogPage;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

public class WizardPageHandler extends WizardPage {
	private Text containerTargetText,containerProjectName,containerPackageName,containerClassName;
	private Combo analysisType;
	private Button[] analysisFramework = new Button[2];

	@SuppressWarnings("unused")
	private Text fileText;

	/**
	 * Constructor for SampleNewWizardPage.
	 * 
	 * @param pageName
	 */
	public WizardPageHandler(ISelection selection) {
		super("wizardPage");
		setTitle("Create New Analysis Project");
		setDescription("This wizard creates new analysis project based on user-inputs");
		//this.selection = selection;
	}

	/**
	 * @see IDialogPage#createControl(Composite)
	 */
	public void createControl(Composite parent) {
		Composite container = new Composite(parent, SWT.NULL);
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 5;
		layout.verticalSpacing = 15;
		
		Label labelProject = new Label(container, SWT.NULL);
		labelProject.setText("Name of the Project: ");
		labelProject.setToolTipText("Specify the analysis project name");

		containerProjectName = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		containerProjectName.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		new Label(container, SWT.NONE).setLayoutData(gd);
		
		Label labelPackage = new Label(container, SWT.NULL);
		labelPackage.setText("Package: ");

		containerPackageName = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		containerPackageName.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		new Label(container, SWT.NONE).setLayoutData(gd);
		
		Label labelClass = new Label(container, SWT.NULL);
		labelClass.setText("Class Name: ");
		labelClass.setToolTipText("Name of the class containing main method");

		containerClassName = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		containerClassName.setLayoutData(gd);
		
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		new Label(container, SWT.NONE).setLayoutData(gd);
	
//		Label label = new Label(container, SWT.NULL);
//		label.setText("Choose Folder: ");
//
//		containerSourceText = new Text(container, SWT.BORDER | SWT.SINGLE);
//		gd = new GridData(GridData.FILL_HORIZONTAL);
//		gd.horizontalSpan = 3;
//		containerSourceText.setLayoutData(gd);		
//
//		Button button = new Button(container, SWT.PUSH);
//		button.setText("Browse...");
//		button.addSelectionListener(new SelectionAdapter() {
//			public void widgetSelected(SelectionEvent e) {
//				handleFolderBrowse();
//			}
//		});		
		
		Label labelFile = new Label(container, SWT.NULL);
		labelFile.setText("Choose Target Project:");
		labelFile.setToolTipText("Select target project on which you would like to run the analysis");

		containerTargetText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		containerTargetText.setLayoutData(gd);
//		containerTargetText.addModifyListener(new ModifyListener() {
//			public void modifyText(ModifyEvent e) {
//				dialogChanged();
//			}
//		});

		Button buttonFile = new Button(container, SWT.PUSH);
		buttonFile.setText("Browse...");
		buttonFile.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent e) {
				handleProjectBrowse();
				dialogChanged();
			}
		});
		
		Label labelAnalysis = new Label(container, SWT.NULL);
		labelAnalysis.setText("Analysis Type/Framework: ");
		
		analysisType = new Combo(container, SWT.DROP_DOWN);
		analysisType.setItems(new String[]{"Select","Inter Procedural Analysis","Intra Procedural Analysis"});
		analysisType.select(0);
		
		analysisFramework[0] = new Button(container, SWT.RADIO);
		analysisFramework[0].setSelection(true);
		analysisFramework[0].setText("Soot");
		//analysisType[0].setBounds(10, 5, 75, 30);
		
		analysisFramework[1] = new Button(container, SWT.RADIO);
		analysisFramework[1].setSelection(false);
		analysisFramework[1].setText("IFDS/IDE");
		//analysisType[1].setBounds(10, 50, 75, 30);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 1;
		new Label(container, SWT.NONE).setLayoutData(gd);
		
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
	
//	private void handleFolderBrowse() {
//		DirectoryDialog dialog = new DirectoryDialog(getShell());
//		 containerSourceText.setText(dialog.open());
//	}

	/**
	 * Ensures that both text fields are set.
	 */

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
		//containerMap.put("ProjectPath", containerSourceText.getText());
		containerMap.put("TargetPath", containerTargetText.getText());
		containerMap.put("ProjectName", containerProjectName.getText());
		containerMap.put("PackageName", containerPackageName.getText());
		containerMap.put("ClassName", containerClassName.getText());
		containerMap.put("AnalysisType", analysisType.getText());
		if(analysisFramework[0].getSelection())
		{
			containerMap.put("AnalysisFramework", analysisFramework[0].getText());
		}
		if(analysisFramework[1].getSelection())
		{
			containerMap.put("AnalysisFramework", analysisFramework[0].getText());
		}
		return containerMap;
	}
}
