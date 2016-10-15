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

import soot.jimple.BreakpointStmt;
import soot.jimple.DefinitionStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SwitchStmt;
import soot.jimple.ThrowStmt;

public class UnitBreakpointPropertiesDialog extends Dialog implements SelectionListener {

    private Composite compositeType;
    private Composite compositeUnit;
    private Button bSuspendOnType;
    private Button bSuspendOnUnit;
    private Combo cmbTypes;
    private Combo cmbClass;
    private Combo cmbMethod;

    // @formatter:off
    private static String[] stmts = {
            BreakpointStmt.class.getSimpleName(),
            DefinitionStmt.class.getSimpleName(),
            GotoStmt.class.getSimpleName(),
            IfStmt.class.getSimpleName(),
            InvokeStmt.class.getSimpleName(),
            MonitorStmt.class.getSimpleName(),
            NopStmt.class.getSimpleName(),
            RetStmt.class.getSimpleName(),
            ReturnStmt.class.getSimpleName(),
            ReturnVoidStmt.class.getSimpleName(),
            SwitchStmt.class.getSimpleName(),
            ThrowStmt.class.getSimpleName()
    };
    // @formatter:on

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

        compositeUnit = new Composite(verticalGroup, SWT.NONE);
        compositeUnit.setLayout(new RowLayout(SWT.HORIZONTAL));
        bSuspendOnUnit = new Button(compositeUnit, SWT.CHECK);
        bSuspendOnUnit.setText("Suspend on unit");
        bSuspendOnUnit.addSelectionListener(this);
        cmbClass = new Combo(compositeUnit, SWT.READ_ONLY);
        cmbClass.addSelectionListener(this);
        cmbMethod = new Combo(compositeUnit, SWT.READ_ONLY);
        cmbMethod.addSelectionListener(this);

        fillCombos();

        cmbClass.select(0);
        cmbMethod.select(0);
        cmbTypes.select(0);

        return container;
    }

    private void fillCombos() {
    	// TODO implement with DataModel
//        for (String stmt : stmts) {
//            cmbTypes.add(stmt);
//        }
//
//        CallGraphGenerator generator = new CallGraphGenerator();
//        Map<VFMethod, ControlFlowGraph> analysisData; analysisData = new HashMap<>();
//        generator.runAnalysis(analysisData);
//
//        Set<String> classes = new HashSet<>();
//        analysisData.keySet().forEach(key -> {
//            classes.add(key.getSootMethod().getDeclaringClass().getName());
//        });
//        classes.forEach(cls -> {
//            cmbClass.add(cls);
//        });
//
//        analysisData.keySet().forEach(key -> {
//            cmbMethod.add(key.getSootMethod().getName());
//        });
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
        return new Point(600, 300);
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