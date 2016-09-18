package de.unipaderborn.visuflow.debug.ui;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

public class UnitBreakpointPropertiesDialog extends Dialog implements SelectionListener {

    private Composite compositeType;
    private Composite compositeUnit;
    private Button bSuspendOnType;
    private Button bSuspendOnUnit;
    private Combo cmbTypes;
    private Combo cmbClass;
    private Combo cmbMethod;

    private Object currentSelection;

    public UnitBreakpointPropertiesDialog(Shell parentShell) {
        super(parentShell);
    }

    @Override
    protected Control createDialogArea(Composite parent) {
        System.out.println("create dialog");
        Composite container = (Composite) super.createDialogArea(parent);

        Composite verticalGroup = new Composite(container, SWT.NONE);
        verticalGroup.setLayout(new RowLayout(SWT.VERTICAL));

        compositeType = new Composite(verticalGroup, SWT.NONE);
        compositeType.setLayout(new RowLayout(SWT.HORIZONTAL));
        bSuspendOnType = new Button(compositeType, SWT.CHECK);
        bSuspendOnType.setText("Suspend on type");
        bSuspendOnType.addSelectionListener(this);
        cmbTypes = new Combo(compositeType, SWT.READ_ONLY);
        cmbTypes.addSelectionListener(this);
        cmbTypes.add("JAssignmnt");
        cmbTypes.add("JExpression");
        cmbTypes.add("JReturnStmt");
        cmbTypes.select(0);

        compositeUnit = new Composite(verticalGroup, SWT.NONE);
        compositeUnit.setLayout(new RowLayout(SWT.HORIZONTAL));
        bSuspendOnUnit = new Button(compositeUnit, SWT.CHECK);
        bSuspendOnUnit.setText("Suspend on unit");
        bSuspendOnUnit.addSelectionListener(this);
        cmbClass = new Combo(compositeUnit, SWT.READ_ONLY);
        cmbClass.addSelectionListener(this);
        cmbClass.add("HelloWorld");
        cmbClass.select(0);
        cmbMethod = new Combo(compositeUnit, SWT.READ_ONLY);
        cmbMethod.addSelectionListener(this);
        cmbMethod.add("main()");
        cmbMethod.select(0);

        return container;
    }

    // overriding this methods allows you to set the
    // title of the custom dialog
    @Override
    protected void configureShell(Shell newShell) {
        super.configureShell(newShell);
        newShell.setText("Unit Breakpoint Properties");
    }

    @Override
    protected Point getInitialSize() {
        return new Point(450, 300);
    }

    @Override
    protected boolean isResizable() {
        return true;
    }

    @Override
    public void widgetSelected(SelectionEvent e) {
        if(e.getSource() != currentSelection && e.getSource() == bSuspendOnType) {
            currentSelection = e.getSource();
            cmbMethod.setEnabled(false);
            cmbClass.setEnabled(false);
            cmbTypes.setEnabled(true);
            bSuspendOnUnit.setSelection(false);
            System.out.println("Suspend on Type");
        } else if(e.getSource() != currentSelection && e.getSource() == bSuspendOnUnit) {
            currentSelection = e.getSource();
            cmbMethod.setEnabled(true);
            cmbClass.setEnabled(true);
            cmbTypes.setEnabled(false);
            bSuspendOnType.setSelection(false);
            System.out.println("Suspend on Unit");
        }
    }

    @Override
    public void widgetDefaultSelected(SelectionEvent e) {
        widgetSelected(e);
    }

}