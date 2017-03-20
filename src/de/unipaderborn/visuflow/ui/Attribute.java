package de.unipaderborn.visuflow.ui;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.unipaderborn.visuflow.model.VFUnit;

public class Attribute extends TitleAreaDialog {
	private Text tfAnalysis;
	private Text tfAttr;

	private String analysis = "";
	private String attribute = "";

	public Attribute(Shell parentShell) {
		super(parentShell);
	}

	public Attribute(VFUnit vfUnit, Shell parentShell) {
		super(parentShell);

	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		GridLayout layout = new GridLayout();
		layout.marginRight = 5;
		layout.marginLeft = 10;
		container.setLayout(layout);

		Label lblAttribute = new Label(container, SWT.NONE);
		lblAttribute.setText("Attribute:");

		tfAnalysis = new Text(container, SWT.BORDER);
		tfAnalysis.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tfAnalysis.setText(analysis);
		tfAnalysis.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text textWidget = (Text) e.getSource();
				String userText = textWidget.getText();
				analysis = userText;
			}
		});

		Label lAttr = new Label(container, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lAttr.setLayoutData(gd_lblNewLabel);
		lAttr.setText("Attribute value:");

		tfAttr = new Text(container, SWT.BORDER | SWT.BORDER);
		tfAttr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tfAttr.setText(attribute);
		tfAttr.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text textWidget = (Text) e.getSource();
				String passwordText = textWidget.getText();
				attribute = passwordText;
			}
		});
		return container;
	}

	@Override

	protected void configureShell(Shell newShell) {

		super.configureShell(newShell);

		newShell.setText("Set custom attributes");

		newShell.setImage(new org.eclipse.swt.graphics.Image(newShell.getDisplay(), "icons/sample.gif"));

	}

	@Override

	public void create() {

		super.create();

		// setTitle("This wizard links the Target Java project with

		setMessage("Please enter the attribute and its value. ", IMessageProvider.INFORMATION);

	}

	// override method to use "Login" as label for the OK button
	@Override
	protected void createButtonsForButtonBar(Composite parent) {
		createButton(parent, IDialogConstants.OK_ID, "Set", true);
		createButton(parent, IDialogConstants.CANCEL_ID, IDialogConstants.CANCEL_LABEL, false);
	}

	@Override
	protected Point getInitialSize() {
		return new Point(450, 300);
	}

	@Override
	protected void okPressed() {
		analysis = tfAnalysis.getText();
		attribute = tfAttr.getText();
		super.okPressed();
		// System.out.println("Analysis :"+ analysis);
		// System.out.println("Attribute :"+ attribute);
	}

	public String getAnalysis() {
		return analysis;
	}

	public void setAnalysis(String analysis) {
		this.analysis = analysis;
	}

	public String getAttribute() {
		return attribute;
	}

	public void setAttribute(String attribute) {
		this.attribute = attribute;
	}

}