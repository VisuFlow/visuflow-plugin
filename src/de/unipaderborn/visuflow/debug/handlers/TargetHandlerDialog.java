package de.unipaderborn.visuflow.debug.handlers;

import java.io.File;
import java.io.InputStream;
import java.util.HashMap;

import org.eclipse.core.resources.IContainer;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.dialogs.ContainerSelectionDialog;

import de.unipaderborn.visuflow.ProjectPreferences;
import de.unipaderborn.visuflow.builder.AddRemoveVisuFlowNatureHandler;
import de.unipaderborn.visuflow.builder.GlobalSettings;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.util.ServiceUtil;

/**
 * @author Zafar Habeeb
 *
 */
public class TargetHandlerDialog extends TitleAreaDialog {

	private Text containerSourceText,containerTargetText;
	private Button okButton;

	@SuppressWarnings("unused")
	private Text fileText;

	private ISelection selection;

	public TargetHandlerDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite containerDialog = (Composite) super.createDialogArea(parent);
		Composite container = new Composite(containerDialog, SWT.NONE);
		container.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		GridLayout layout = new GridLayout();
		container.setLayout(layout);
		layout.numColumns = 3;
		layout.verticalSpacing = 9;
		Label label = new Label(container, SWT.NULL);
		label.setText("Choose Analysis Project:");

		containerSourceText = new Text(container, SWT.BORDER | SWT.SINGLE);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		containerSourceText.setLayoutData(gd);
		containerSourceText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button button = new Button(container, SWT.PUSH);
		button.setText("Browse...");
		button.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse();
			}
		});

		Label labelFile = new Label(container, SWT.NULL);
		labelFile.setText("Choose Target Project:");

		containerTargetText = new Text(container, SWT.BORDER | SWT.SINGLE);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		containerTargetText.setLayoutData(gd);
		containerTargetText.addModifyListener(new ModifyListener() {
			@Override
			public void modifyText(ModifyEvent e) {
				dialogChanged();
			}
		});

		Button buttonFile = new Button(container, SWT.PUSH);
		buttonFile.setText("Browse...");
		buttonFile.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				handleBrowse2();
			}
		});

		initialize();
		dialogChanged();
		return container;
	}
	private void initialize() {
		if (selection != null && selection.isEmpty() == false
				&& selection instanceof IStructuredSelection) {
			IStructuredSelection ssel = (IStructuredSelection) selection;
			if (ssel.size() > 1)
				return;
			Object obj = ssel.getFirstElement();
			if (obj instanceof IResource) {
				IContainer container;
				if (obj instanceof IContainer)
					container = (IContainer) obj;
				else
					container = ((IResource) obj).getParent();
				containerSourceText.setText(container.getFullPath().toString());
			}
		}

	}

	/**
	 * Uses the standard container selection dialog to choose the new value for
	 * the container field.
	 */

	private void handleBrowse() {
		ContainerSelectionDialog dialog = new ContainerSelectionDialog(
				getShell(), ResourcesPlugin.getWorkspace().getRoot(), false,
				"Select new file container");
		if (dialog.open() == ContainerSelectionDialog.OK) {
			Object[] result = dialog.getResult();
			if (result.length == 1) {
				containerSourceText.setText(((Path) result[0]).toString());
			}
		}
	}

	private void handleBrowse2() {
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

	/**
	 * Ensures that both text fields are set.
	 */

	private void dialogChanged() {
		IResource container = ResourcesPlugin.getWorkspace().getRoot()
				.findMember(new Path(getContainerName().get("ProjectPath")));

		if(!containerSourceText.getText().isEmpty()  && !containerTargetText.getText().isEmpty())
		{
			okButton.setEnabled(true);
		}

		if (getContainerName().get("ProjectPath").length() == 0) {
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
		if(message != null) {
			setMessage("Error: "+message, IMessageProvider.INFORMATION);
		}
	}

	public HashMap<String, String> getContainerName() {
		HashMap<String, String> containerMap = new HashMap<>();
		containerMap.put("ProjectPath", containerSourceText.getText());
		containerMap.put("TargetPath", containerTargetText.getText());
		return containerMap;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Link Analysis and Target Project");
		InputStream in = getClass().getResourceAsStream("/icons/Link.png");
		newShell.setImage(new org.eclipse.swt.graphics.Image(newShell.getDisplay(), in));
	}

	@Override
	public void create() {
		super.create();
		setMessage("This wizard links the Target Java project with the Analysis project", IMessageProvider.INFORMATION);
	}

	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		okButton =  createButton(parent, IDialogConstants.OK_ID, "Apply", false);
		okButton.setEnabled(false);
		createButton(parent, IDialogConstants.CANCEL_ID,
				IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}



	@Override
	protected void okPressed() {

		IWorkspaceRoot root = ResourcesPlugin.getWorkspace().getRoot();
		IResource resourceAnalysis = root.findMember(new Path(containerSourceText.getText()));
		IResource resourceTarget = root.findMember(new Path(containerTargetText.getText()));
		//IContainer containerAnalysis = (IContainer) resourceAnalysis;
		IJavaProject targetProject = JavaCore.create(resourceTarget.getProject());
		//String key = "TargetProject_"+resourceAnalysis.getProject().getName();
		try {
			GlobalSettings.put("Target_Path",resourceTarget.getLocation().toOSString()+ File.separator +  targetProject.getOutputLocation().lastSegment());
		} catch (JavaModelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		IJavaProject analysisProject = JavaCore.create(resourceAnalysis.getProject());
		GlobalSettings.put("AnalysisProject", analysisProject.getProject().getName());
		GlobalSettings.put("TargetProject", targetProject.getProject().getName());
		ProjectPreferences projPref = new ProjectPreferences();
		projPref.createPreferences();
		AddRemoveVisuFlowNatureHandler addNature = new AddRemoveVisuFlowNatureHandler();
		try {
			if(!analysisProject.getProject().isNatureEnabled("JimpleBuilder.VisuFlowNature"))
				addNature.toggleNature(analysisProject.getProject());
		} catch (CoreException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		super.okPressed();
		ServiceUtil.getService(DataModel.class).triggerProjectRebuild();

	}

}
