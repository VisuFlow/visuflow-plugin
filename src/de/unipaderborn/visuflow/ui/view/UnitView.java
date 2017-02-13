package de.unipaderborn.visuflow.ui.view;

import java.util.ArrayList;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.List;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.unipaderborn.visuflow.debug.handlers.NavigationHandler;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.jimple.internal.JAddExpr;
import soot.jimple.internal.JAssignStmt;
import soot.jimple.internal.JGotoStmt;
import soot.jimple.internal.JIdentityStmt;
import soot.jimple.internal.JIfStmt;
import soot.jimple.internal.JInvokeStmt;
import soot.jimple.internal.JReturnStmt;

public class UnitView extends ViewPart implements EventHandler {

	static DataModel dataModel;
	static Tree tree;
	static Combo classCombo, methodCombo;
	Button checkBox;
	String classSelection, methodSelection;
	private List<VFUnit> listUnits;
	private List<VFNode> nodeList;
	GridData gridUnitTable;

	class ViewLabelProvider extends LabelProvider implements ILabelProvider {
		public String getColumnText(Object obj, int index) {
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index) {
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj) {
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	@Override
	public void createPartControl(Composite parent) {
		GridLayout layout = new GridLayout(3, true);
		parent.setLayout(layout);

		GridData gridVFClass = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridVFClass.widthHint = SWT.DEFAULT;
		gridVFClass.heightHint = SWT.DEFAULT;

		classCombo = new Combo(parent, SWT.DROP_DOWN);
		classCombo.setLayout(layout);
		classCombo.setLayoutData(gridVFClass);

		GridData gridVFMethod = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridVFMethod.widthHint = SWT.DEFAULT;
		gridVFMethod.heightHint = SWT.DEFAULT;

		methodCombo = new Combo(parent, SWT.DROP_DOWN);
		methodCombo.setLayout(layout);
		methodCombo.setLayoutData(gridVFMethod);

		GridData gridCheck = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridCheck.widthHint = SWT.DEFAULT;
		gridCheck.heightHint = SWT.DEFAULT;

		checkBox = new Button(parent, SWT.CHECK);
		checkBox.setText("Sync with Graph View");
		checkBox.setLayoutData(gridCheck);

		gridUnitTable = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gridUnitTable.widthHint = SWT.DEFAULT;
		gridUnitTable.heightHint = SWT.DEFAULT;

		tree = new Tree(parent, SWT.FILL | SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BACKGROUND | SWT.MULTI | SWT.BACKGROUND);
		tree.setHeaderVisible(true);
		tree.setLayout(layout);
		tree.setLayoutData(gridUnitTable);
		// methodCombo.select(0);
		classCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedClass = classCombo.getText();
				for (VFClass vfclass : dataModel.listClasses()) {
					if (vfclass.getSootClass().getName().toString().equals(selectedClass)) {
						methodCombo.removeAll();
						for (VFMethod vfmethod : dataModel.listMethods(vfclass)) {
							methodCombo.add(vfmethod.getSootMethod().getDeclaration());
						}
					}
				}
				methodCombo.select(0);
				String selectMethod = methodCombo.getText();
				for (VFClass vfclass : dataModel.listClasses()) {
					if (vfclass.getSootClass().getName().toString().equals(selectedClass)) {
						for (VFMethod vfmethod : dataModel.listMethods(vfclass)) {
							if (vfmethod.getSootMethod().getDeclaration().toString().equals(selectMethod)) {
								tree.removeAll();
								listUnits = vfmethod.getUnits();
								populateUnits(listUnits);
							}
						}
					}
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		methodCombo.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectMethod = methodCombo.getText();
				String selectedClass = classCombo.getText();
				for (VFClass vfclass : dataModel.listClasses()) {
					if (vfclass.getSootClass().getName().toString().equals(selectedClass)) {
						for (VFMethod vfmethod : dataModel.listMethods(vfclass)) {
							if (vfmethod.getSootMethod().getDeclaration().toString().equals(selectMethod)) {
								tree.removeAll();
								listUnits = vfmethod.getUnits();
								populateUnits(listUnits);
							}
						}
					}
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		checkBox.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {

				if (!listUnits.isEmpty()) {
					tree.removeAll();
					
					populateUnits(listUnits);
				}

			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {

			}
		});

		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, DataModel.EA_TOPIC_DATA_MODEL_CHANGED);
		ServiceUtil.registerService(EventHandler.class, this, properties);
		properties.put(EventConstants.EVENT_TOPIC, DataModel.EA_TOPIC_DATA_SELECTION);
		ServiceUtil.registerService(EventHandler.class, this, properties);
		properties.put(EventConstants.EVENT_TOPIC, DataModel.EA_TOPIC_DATA_FILTER_GRAPH);
		ServiceUtil.registerService(EventHandler.class, this, properties);

		reloadData();
	}

	@Override
	public void setFocus() {

	}

	public static void populateUnits(List<VFUnit> listUnits) {
		for (VFUnit unit : listUnits) {
			TreeItem treeItem = new TreeItem(tree, SWT.NONE | SWT.BORDER);
			treeItem.setText(unit.getUnit().toString());
			int stType = 0;
			if (unit.getUnit() instanceof JAssignStmt)
				stType = 1;
			else if (unit.getUnit() instanceof JAddExpr)
				stType = 2;
			else if (unit.getUnit() instanceof JInvokeStmt)
				stType = 3;
			else if (unit.getUnit() instanceof JReturnStmt)
				stType = 4;
			else if (unit.getUnit() instanceof JIdentityStmt)
				stType = 5;
			else if (unit.getUnit() instanceof JIfStmt)
				stType = 6;
			else if (unit.getUnit() instanceof JGotoStmt)
				stType = 7;
			switch (stType) {

			case 1:

				JAssignStmt jassStmt = (JAssignStmt) unit.getUnit();
				TreeItem treeUnitAssType = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				treeUnitAssType.setText(new String[] { "Unit Type : " + jassStmt.getClass().toString() });

				TreeItem treeLeft = new TreeItem(treeUnitAssType, SWT.LEFT | SWT.BORDER);
				treeLeft.setText(new String[] { "Left" });
				TreeItem treeLeftValue = new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
				treeLeftValue.setText(new String[] { "Value : " + jassStmt.leftBox.getValue().toString() });
				TreeItem treeLeftClass = new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
				treeLeftClass.setText(new String[] { "Class : " + jassStmt.leftBox.getValue().getClass().toString() });

				TreeItem treeRight = new TreeItem(treeUnitAssType, SWT.LEFT | SWT.BORDER);
				treeRight.setText(new String[] { "Right" });
				TreeItem treeRightValue = new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
				treeRightValue.setText(new String[] { "Value : " + jassStmt.rightBox.getValue().toString() });
				TreeItem treeRightClass = new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
				treeRightClass.setText(new String[] { "Class : " + jassStmt.rightBox.getValue().getClass().toString() });
				break;

			case 2:

				JAddExpr jaddStmt = (JAddExpr) unit.getUnit();
				TreeItem treeUnitAddType = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				treeUnitAddType.setText(new String[] { "Unit Type : " + jaddStmt.getClass().toString() });

				TreeItem treeOp1 = new TreeItem(treeUnitAddType, SWT.LEFT | SWT.BORDER);
				treeOp1.setText(new String[] { "Operator 1" });
				TreeItem treeOp1Value = new TreeItem(treeOp1, SWT.LEFT | SWT.BORDER);
				treeOp1Value.setText(new String[] { "Value : " + jaddStmt.getOp1().toString() });
				TreeItem treeOp1Class = new TreeItem(treeOp1, SWT.LEFT | SWT.BORDER);
				treeOp1Class.setText(new String[] { "Class : " + jaddStmt.getOp1().getClass().toString() });

				TreeItem treeOp2 = new TreeItem(treeUnitAddType, SWT.LEFT | SWT.BORDER);
				treeOp2.setText(new String[] { "Operator 2" });
				TreeItem treeOp2Value = new TreeItem(treeOp2, SWT.LEFT | SWT.BORDER);
				treeOp2Value.setText(new String[] { "Value : " + jaddStmt.getOp1().toString() });
				TreeItem treeOp2Class = new TreeItem(treeOp2, SWT.LEFT | SWT.BORDER);
				treeOp2Class.setText(new String[] { "Class : " + jaddStmt.getOp1().getClass().toString() });
				break;

			case 3:

				JInvokeStmt jinvokestmt = (JInvokeStmt) unit.getUnit();
				TreeItem treeUnitInvType = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				treeUnitInvType.setText(new String[] { "Unit Type : " + jinvokestmt.getClass().toString() });

				TreeItem treeInvokeExp = new TreeItem(treeUnitInvType, SWT.LEFT | SWT.BORDER);
				treeInvokeExp.setText(new String[] { "Invoke Expression : " + jinvokestmt.getInvokeExpr() });

				TreeItem treeMethodClass = new TreeItem(treeInvokeExp, SWT.LEFT | SWT.BORDER);
				treeMethodClass.setText(new String[] { "Method Declaring Class : " + jinvokestmt.getInvokeExpr().getMethod().getDeclaringClass() });

				TreeItem treeMethodSig = new TreeItem(treeInvokeExp, SWT.LEFT | SWT.BORDER);
				treeMethodSig.setText(new String[] { "Method Signature : " + jinvokestmt.getInvokeExpr().getMethod().getDeclaration() });

				int argCount = jinvokestmt.getInvokeExpr().getMethod().getParameterCount();
				TreeItem treeArgcount = new TreeItem(treeInvokeExp, SWT.LEFT | SWT.BORDER);
				treeArgcount.setText(new String[] { "Parameter Count : " + argCount });

				if (argCount > 0) {
					for (int i = 0; i < argCount; i++) {
						TreeItem treeArg = new TreeItem(treeArgcount, SWT.LEFT | SWT.BORDER);
						treeArg.setText(new String[] { "Parameter " + (i + 1) + " type : " + jinvokestmt.getInvokeExpr().getMethod().getParameterType(i) });
					}
				}

				TreeItem treeFieldref = new TreeItem(treeInvokeExp, SWT.LEFT | SWT.BORDER);
				if (jinvokestmt.containsFieldRef())
					treeFieldref.setText(new String[] { "Field Reference : " + jinvokestmt.getFieldRef() });
				else
					treeFieldref.setText(new String[] { "Field Reference : Null" });

				TreeItem treeMethodReturnType = new TreeItem(treeInvokeExp, SWT.LEFT | SWT.BORDER);
				treeMethodReturnType.setText(new String[] { "Method return Type : " + jinvokestmt.getInvokeExpr().getMethod().getReturnType() });
				break;

			case 4:

				JReturnStmt jretStmt = (JReturnStmt) unit.getUnit();
				TreeItem treeUnitRetType = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				treeUnitRetType.setText(new String[] { "Unit Type : " + jretStmt.getClass().toString() });

				if (jretStmt.containsInvokeExpr()) {
					TreeItem treeReturnInv = new TreeItem(treeUnitRetType, SWT.LEFT | SWT.BORDER);
					treeReturnInv.setText(new String[] { "Return Expression : " + jretStmt.getInvokeExpr() });
				}

				TreeItem treeReturnValue = new TreeItem(treeUnitRetType, SWT.LEFT | SWT.BORDER);
				treeReturnValue.setText(new String[] { "Return Value : " + jretStmt.getOp() });

				TreeItem treeReturnType = new TreeItem(treeUnitRetType, SWT.LEFT | SWT.BORDER);
				treeReturnType.setText(new String[] { "Return Value : " + jretStmt.getOp().getType() });
				break;

			case 5:

				JIdentityStmt jidenStmt = (JIdentityStmt) unit.getUnit();
				TreeItem treeUnitIdenType = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				treeUnitIdenType.setText(new String[] { "Unit Type : " + jidenStmt.getClass().toString() });

				TreeItem treeIdenLeft = new TreeItem(treeUnitIdenType, SWT.LEFT | SWT.BORDER);
				treeIdenLeft.setText(new String[] { "Left" });
				TreeItem treeIdenLeftValue = new TreeItem(treeIdenLeft, SWT.LEFT | SWT.BORDER);
				treeIdenLeftValue.setText(new String[] { "Value : " + jidenStmt.leftBox.getValue().toString() });
				TreeItem treeIdenLeftClass = new TreeItem(treeIdenLeft, SWT.LEFT | SWT.BORDER);
				treeIdenLeftClass.setText(new String[] { "Class : " + jidenStmt.leftBox.getValue().getClass().toString() });

				TreeItem treeIdenRight = new TreeItem(treeUnitIdenType, SWT.LEFT | SWT.BORDER);
				treeIdenRight.setText(new String[] { "Right" });
				TreeItem treeIdenRightValue = new TreeItem(treeIdenRight, SWT.LEFT | SWT.BORDER);
				treeIdenRightValue.setText(new String[] { "Value : " + jidenStmt.rightBox.getValue().toString() });
				TreeItem treeIdenRightClass = new TreeItem(treeIdenRight, SWT.LEFT | SWT.BORDER);
				treeIdenRightClass.setText(new String[] { "Class : " + jidenStmt.rightBox.getValue().getClass().toString() });
				break;
				
			case 6:

				JIfStmt jifStmt = (JIfStmt) unit.getUnit();
				TreeItem treeUnitIfType = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				treeUnitIfType.setText(new String[] { "Unit Type : " + jifStmt.getClass().toString() });
				
				TreeItem treeIfCond = new TreeItem(treeUnitIfType, SWT.LEFT | SWT.BORDER);
				treeIfCond.setText(new String[] { "Condition : " + jifStmt.getCondition() });

				TreeItem treeIfCondClass = new TreeItem(treeUnitIfType, SWT.LEFT | SWT.BORDER);
				treeIfCondClass.setText(new String[] { "Condition Class : " + jifStmt.getCondition().getClass() });

				TreeItem treeifCondTarget = new TreeItem(treeUnitIfType, SWT.LEFT | SWT.BORDER);
				treeifCondTarget.setText(new String[] { "Target Unit : " + jifStmt.getTarget() });
				break;
				
			case 7:

				JGotoStmt jgotoStmt = (JGotoStmt) unit.getUnit();
				TreeItem treeUnitgotoType = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				treeUnitgotoType.setText(new String[] { "Unit Type : " + jgotoStmt.getClass().toString() });
				
				TreeItem treegotoTarget = new TreeItem(treeUnitgotoType, SWT.LEFT | SWT.BORDER);
				treegotoTarget.setText(new String[] { "Go to target : " + jgotoStmt.getTarget() });
				break;

			case 0:

				break;
			}
		}
		
		Menu menu = new Menu(tree);
		tree.setMenu(menu);
		MenuItem menuItemJimple = new MenuItem(menu, SWT.None);
		menuItemJimple.setText("View Jimple Code");
		menuItemJimple.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem selectedItem = tree.getSelection()[0];
				while(selectedItem.getParentItem()!=null)
				{
					selectedItem = selectedItem.getParentItem();
				}
				HashMap<String, Object> unitDetails = getUnitDetails(selectedItem.getText());
				ArrayList<VFUnit> jimpleArrayList = new ArrayList<>();
				jimpleArrayList.add((VFUnit)unitDetails.get("unit"));
				NavigationHandler nh = new NavigationHandler();
				nh.HighlightJimpleLine(jimpleArrayList);				
				List<VFNode> cfgArrayList = new ArrayList<>();
				cfgArrayList.add(new VFNode((VFUnit)unitDetails.get("unit"),0));
				try {
					ServiceUtil.getService(DataModel.class).filterGraph(cfgArrayList, true, null);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		MenuItem menuItemGraph = new MenuItem(menu, SWT.None);
		menuItemGraph.setText("View in CFG");
		menuItemGraph.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				TreeItem selectedItem = tree.getSelection()[0];
				while(selectedItem.getParentItem()!=null)
				{
					selectedItem = selectedItem.getParentItem();
				}
				HashMap<String, Object> unitDetails = getUnitDetails(selectedItem.getText());
				List<VFNode> al = new ArrayList<>();
				al.add(new VFNode((VFUnit)unitDetails.get("unit"),0));
				try {
					ServiceUtil.getService(DataModel.class).filterGraph(al, true, null);
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
		
		MenuItem menuItemJavaSource = new MenuItem(menu, SWT.None);
		menuItemJavaSource.setText("View Java Source");
		menuItemJavaSource.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				TreeItem selectedItem = tree.getSelection()[0];
				while(selectedItem.getParentItem()!=null)
				{
					selectedItem = selectedItem.getParentItem();
				}
				HashMap<String, Object> unitDetails = getUnitDetails(selectedItem.getText());
				ArrayList<VFUnit> jimpleArrayList = new ArrayList<>();
				jimpleArrayList.add((VFUnit)unitDetails.get("unit"));
				NavigationHandler handler = new NavigationHandler();
				handler.NavigateToSource(jimpleArrayList.get(0));
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				
			}
		});
	}
	
	
	public static HashMap<String, Object> getUnitDetails(String unitText)
	{
		String selectedClass = classCombo.getText();
		String selectedMethod = methodCombo.getText();
		HashMap<String, Object> unitDetails = new HashMap<>();
		VFClass classSelected = null;
		VFMethod methodSelected = null;
		VFUnit unitSelected = null;
		List<VFClass> listofClass = dataModel.listClasses();
		for (VFClass vfClass : listofClass) {
			if(vfClass.getSootClass().getName().toString().equals(selectedClass))
			{
				classSelected = vfClass;
				break;
			}
		}
		List<VFMethod> listofMethods = dataModel.listMethods(classSelected);
		for (VFMethod vfMethod : listofMethods) {
			if(vfMethod.getSootMethod().getDeclaration().toString().equals(selectedMethod))
			{
				methodSelected = vfMethod;
				break;
			}
		}
		List<VFUnit> listofUnits = dataModel.listUnits(methodSelected);
		for (VFUnit vfUnit : listofUnits) {
			if(vfUnit.getUnit().toString().equals(unitText))
			{
				unitSelected = vfUnit;
			}
		}
		unitDetails.put("className", classSelected);
		unitDetails.put("methodName", methodSelected);
		unitDetails.put("unit", unitSelected);
		return unitDetails;
	}

	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().equals(DataModel.EA_TOPIC_DATA_SELECTION)) {
			getDisplay().asyncExec(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					listUnits = (List<VFUnit>) event.getProperty("selectedMethodUnits");
					if (checkBox.getSelection()) {
						tree.removeAll();
						populateUnits(listUnits);
					}
				}
			});
		}

		if (event.getTopic().equals(DataModel.EA_TOPIC_DATA_MODEL_CHANGED)) {
			reloadData();
		} else if (event.getTopic().equals(DataModel.EA_TOPIC_DATA_FILTER_GRAPH)) {
			getDisplay().asyncExec(new Runnable() {

				@SuppressWarnings("unchecked")
				@Override
				public void run() {
					nodeList = (List<VFNode>) event.getProperty("nodesToFilter");
					if(nodeList.isEmpty())
						return;
					VFClass selectedClass = nodeList.get(0).getVFUnit().getVfMethod().getVfClass();
					VFMethod selectedMethod = nodeList.get(0).getVFUnit().getVfMethod();
					int i = -1;
					for (String classString : classCombo.getItems()) {
						i++;
						if (classString.equals(selectedClass.getSootClass().getName().toString())) {
							classCombo.select(i);
							methodCombo.removeAll();
							for (VFMethod vfmethod : dataModel.listMethods(selectedClass)) {
								methodCombo.add(vfmethod.getSootMethod().getDeclaration());
							}
							break;
						}
					}
					int j = -1;
					for (String methodString : methodCombo.getItems()) {
						j++;
						if (methodString.equals(selectedMethod.getSootMethod().getDeclaration().toString())) {
							methodCombo.select(j);
							tree.removeAll();
							populateUnits(selectedMethod.getUnits());
							break;
						}
					}
					for (VFNode vfNode : nodeList) {
						
						String unitString = vfNode.getUnit().toString();
						for (TreeItem treeItem : tree.getItems()) {
							if (treeItem.getText().equals(unitString)) {
								tree.setSelection(treeItem);
								break;
							}
						}
					}

				}
			});
		}
	}

	private void reloadData() {
		getDisplay().asyncExec(new Runnable() {
			@Override
			public void run() {
				dataModel = ServiceUtil.getService(DataModel.class);
				classCombo.removeAll();
				methodCombo.removeAll();
				for (VFClass vfclass : dataModel.listClasses()) {
					classCombo.add(vfclass.getSootClass().getName());
				}
				classCombo.select(0);
				classSelection = classCombo.getText().trim();
				for (VFClass vfclass : dataModel.listClasses()) {
					if (vfclass.getSootClass().getName().toString().equals(classSelection)) {
						for (VFMethod vfmethod : dataModel.listMethods(vfclass)) {
							methodCombo.add(vfmethod.getSootMethod().getDeclaration());
						}
					}
				}
				methodCombo.select(0);
				String selectMethod = methodCombo.getText();
				String selectedClass = classCombo.getText();
				for (VFClass vfclass : dataModel.listClasses()) {
					if (vfclass.getSootClass().getName().toString().equals(selectedClass)) {
						for (VFMethod vfmethod : dataModel.listMethods(vfclass)) {
							if (vfmethod.getSootMethod().getDeclaration().toString().equals(selectMethod)) {
								tree.removeAll();
								listUnits = vfmethod.getUnits();
								populateUnits(listUnits);
								break;
							}
							
							break;
						}
					}
				}
			}
		});
	}
	
	
	public static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}
}
