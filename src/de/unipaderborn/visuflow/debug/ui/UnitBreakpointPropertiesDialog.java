package de.unipaderborn.visuflow.debug.ui;

import java.util.List;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.CaretEvent;
import org.eclipse.swt.custom.CaretListener;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.layout.RowLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.jimple.AssignStmt;
import soot.jimple.BreakpointStmt;
import soot.jimple.GotoStmt;
import soot.jimple.IdentityStmt;
import soot.jimple.IfStmt;
import soot.jimple.InvokeStmt;
import soot.jimple.MonitorStmt;
import soot.jimple.NopStmt;
import soot.jimple.RetStmt;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.SwitchStmt;
import soot.jimple.ThrowStmt;

public class UnitBreakpointPropertiesDialog extends Dialog implements SelectionListener, CaretListener {

	private Composite compositeType;
	private Composite compositeUnit;
	private Button bSuspendOnType;
	private Button bSuspendOnUnit;
	private Combo cmbTypes;
	private Combo cmbClass;
	private Combo cmbMethod;
	private StyledText unitSelectionArea;

	private List<VFClass> classes;
	private VFClass selectedClass;
	private VFMethod selectedMethod;
	private VFUnit selectedUnit;

	private Color lineHighlight = new Color(Display.getDefault(), 173, 216, 230);

	private boolean suspendOnUnit = true;
	private String unitType;

	// @formatter:off
	private static String[] stmts = {
			AssignStmt.class.getName(),
			BreakpointStmt.class.getName(),
			GotoStmt.class.getName(),
			IdentityStmt.class.getName(),
			IfStmt.class.getName(),
			InvokeStmt.class.getName(),
			MonitorStmt.class.getName(),
			NopStmt.class.getName(),
			RetStmt.class.getName(),
			ReturnStmt.class.getName(),
			ReturnVoidStmt.class.getName(),
			SwitchStmt.class.getName(),
			ThrowStmt.class.getName()
	};
	// @formatter:on

	private Object currentSelection;

	public UnitBreakpointPropertiesDialog(Shell parentShell) {
		super(parentShell);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		Composite container = (Composite) super.createDialogArea(parent);
		container.getShell().addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				lineHighlight.dispose();
			}
		});

		Composite verticalGroup = new Composite(container, SWT.NONE);
		verticalGroup.setLayout(new GridLayout());

		compositeType = new Composite(verticalGroup, SWT.NONE);
		compositeType.setLayout(new RowLayout(SWT.HORIZONTAL));
		bSuspendOnType = new Button(compositeType, SWT.CHECK);
		bSuspendOnType.setText("Suspend on type");
		bSuspendOnType.addSelectionListener(this);
		cmbTypes = new Combo(compositeType, SWT.READ_ONLY);
		cmbTypes.addSelectionListener(this);
		cmbTypes.setEnabled(false);

		compositeUnit = new Composite(verticalGroup, SWT.NONE);
		compositeUnit.setLayout(new RowLayout(SWT.HORIZONTAL));
		bSuspendOnUnit = new Button(compositeUnit, SWT.CHECK);
		bSuspendOnUnit.setText("Suspend on unit");
		bSuspendOnUnit.setSelection(true);
		bSuspendOnUnit.addSelectionListener(this);
		cmbClass = new Combo(compositeUnit, SWT.READ_ONLY);
		cmbClass.addSelectionListener(this);
		cmbMethod = new Combo(compositeUnit, SWT.READ_ONLY);
		cmbMethod.addSelectionListener(this);


		Label spacer = new Label(verticalGroup, SWT.NONE);
		spacer.setText("");

		Label label = new Label(verticalGroup, SWT.NONE);
		label.setText("Selected the unit to suspend on by clicking on a line below:");

		Composite textContainer = new Composite(verticalGroup, SWT.NONE);
		textContainer.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,1));
		textContainer.setLayout(new GridLayout());
		unitSelectionArea = new StyledText(textContainer, SWT.MULTI | SWT.H_SCROLL | SWT.V_SCROLL);
		unitSelectionArea.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true, 1,1));
		unitSelectionArea.setLayoutData(new GridData(750, 400));
		unitSelectionArea.setEditable(false);
		unitSelectionArea.addCaretListener(this);
		unitSelectionArea.addSelectionListener(this);

		fillCombos();

		cmbClass.select(0);
		cmbMethod.select(0);
		cmbTypes.select(0);

		return container;
	}

	private void fillCombos() {
		for (String stmt : stmts) {
			cmbTypes.add(stmt);
		}

		DataModel model = ServiceUtil.getService(DataModel.class);

		classes = model.listClasses();
		classes.forEach(cls -> {
			cmbClass.add(cls.getSootClass().getName());
		});
		if(classes.size() > 0) {
			onClassSelected(classes.get(0));
			onMethodSelected(selectedClass.getMethods().get(0));
		}
	}

	private void onClassSelected(VFClass vfClass) {
		selectedClass = vfClass;
		cmbMethod.removeAll();
		for (VFMethod method : vfClass.getMethods()) {
			cmbMethod.add(method.getSootMethod().getName());
		}
		cmbMethod.select(0);
		onMethodSelected(selectedClass.getMethods().get(0));
	}

	private void onMethodSelected(VFMethod method) {
		selectedMethod = method;
		StringBuilder sb = new StringBuilder();
		for (VFUnit unit : method.getUnits()) {
			sb.append(unit.getUnit().toString()).append("\n\n");
		}
		unitSelectionArea.setText(sb.toString());
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
		return new Point(800, 600);
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
			unitSelectionArea.setEnabled(false);
			cmbTypes.setEnabled(true);
			bSuspendOnUnit.setSelection(false);
		} else if(e.getSource() != currentSelection && e.getSource() == bSuspendOnUnit) {
			currentSelection = e.getSource();
			cmbMethod.setEnabled(true);
			cmbClass.setEnabled(true);
			unitSelectionArea.setEnabled(true);
			cmbTypes.setEnabled(false);
			bSuspendOnType.setSelection(false);
		} else if(e.getSource() == cmbClass) {
			int idx = cmbClass.getSelectionIndex();
			VFClass clazz = classes.get(idx);
			onClassSelected(clazz);
		} else if (e.getSource() == cmbMethod) {
			int idx = cmbMethod.getSelectionIndex();
			VFMethod method = selectedClass.getMethods().get(idx);
			onMethodSelected(method);
		} else if (e.getSource() == unitSelectionArea) {
			int caretPos = unitSelectionArea.getCaretOffset();
			unitSelectionArea.setSelection(caretPos);
		}
	}

	@Override
	public void widgetDefaultSelected(SelectionEvent e) {
		widgetSelected(e);
	}

	@Override
	public void caretMoved(CaretEvent event) {
		int offset = event.caretOffset;
		int line = unitSelectionArea.getLineAtOffset(offset);
		if (line % 2 == 0) {
			String lineContent = unitSelectionArea.getLine(line);
			if (!lineContent.trim().isEmpty()) {
				// remove previous selection
				int lineCount = unitSelectionArea.getLineCount();
				unitSelectionArea.setLineBackground(0, lineCount, null);

				// set the new selection
				unitSelectionArea.setLineBackground(line, 1, lineHighlight);

				int unitIndex = line / 2;
				selectedUnit = selectedMethod.getUnits().get(unitIndex);
			}
		}
	}

	@Override
	protected void okPressed() {
		if(bSuspendOnType.getSelection()) {
			unitType = stmts[cmbTypes.getSelectionIndex()];
			suspendOnUnit = false;
		} else {
			suspendOnUnit = true;
		}
		super.okPressed();
	}

	public boolean isSuspendOnUnit() {
		return suspendOnUnit;
	}

	public VFUnit getSelectedUnit() {
		return selectedUnit;
	}

	public String getUnitType() {
		return unitType;
	}
}