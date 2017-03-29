package de.unipaderborn.visuflow.ui;

import java.io.InputStream;

import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.IMessageProvider;
import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import de.unipaderborn.visuflow.model.VFUnit;

/**
 *This class provides a dialog box where the user enters Attribute and attribute's values while
 * setting custom attributes to units.
 * 
 * @author kouot@mail.upb.de
 * @param attr The attribute the user would like to set
 * @param attrValue The value of the custom attribute the user would like to set
 * 	
 */

public class Attribute extends TitleAreaDialog {
	private Text tfAttr;
	private Text tfAttrValue;

	private String attr = "";
	private String attrValue = "";

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

		tfAttr = new Text(container, SWT.BORDER);
		tfAttr.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tfAttr.setText(attr);
		tfAttr.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text textWidget = (Text) e.getSource();
				String userText = textWidget.getText();
				attr = userText;
			}
		});

		Label lAttr = new Label(container, SWT.NONE);
		GridData gd_lblNewLabel = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_lblNewLabel.horizontalIndent = 1;
		lAttr.setLayoutData(gd_lblNewLabel);
		lAttr.setText("Attribute value:");

		tfAttrValue = new Text(container, SWT.BORDER | SWT.BORDER);
		tfAttrValue.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		tfAttrValue.setText(attrValue);
		tfAttrValue.addModifyListener(new ModifyListener() {

			@Override
			public void modifyText(ModifyEvent e) {
				Text textWidget = (Text) e.getSource();
				String passwordText = textWidget.getText();
				attrValue = passwordText;
			}
		});
		return container;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Set custom attributes");
		InputStream in = getClass().getClassLoader().getResourceAsStream("/icons/sample.gif");
		newShell.setImage(new Image(newShell.getDisplay(), in));

	}

	@Override

	public void create() {
		super.create();
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
		attr = tfAttr.getText();
		attrValue = tfAttrValue.getText();
		super.okPressed();
	}

	public String getAnalysis() {
		return attr;
	}

	public void setAnalysis(String analysis) {
		this.attr = analysis;
	}

	public String getAttribute() {
		return attrValue;
	}

	public void setAttribute(String attribute) {
		this.attrValue = attribute;
	}

}