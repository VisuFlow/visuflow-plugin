package de.unipaderborn.visuflow.ui.view;

import java.util.Dictionary;
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
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.ui.ISharedImages;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.part.ViewPart;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.jimple.internal.JAssignStmt;


public class UnitView extends ViewPart implements EventHandler {

	DataModel dataModel;
	Tree tree;
	Combo classCombo,methodCombo;
	Button checkBox;
	String classSelection, methodSelection;
	private List<VFUnit> listUnits;
	GridData gridUnitTable;

	class ViewLabelProvider extends LabelProvider implements ILabelProvider{
		public String getColumnText(Object obj, int index){
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index){
			return getImage(obj);
		}

		@Override
		public Image getImage(Object obj){
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}

	@Override
	public void createPartControl(Composite parent){
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

		tree = new Tree(parent, SWT.FILL| SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BACKGROUND | SWT.MULTI | SWT.BACKGROUND);
		tree.setHeaderVisible(true);
		tree.setLayout(layout);
		tree.setLayoutData(gridUnitTable);
		//methodCombo.select(0);
		classCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectedClass = classCombo.getText();
				for (VFClass vfclass : dataModel.listClasses()) {
					if(vfclass.getSootClass().getName().toString().equals(selectedClass))
					{
						for (VFMethod vfmethod : dataModel.listMethods(vfclass))
						{
							methodCombo.add(vfmethod.getSootMethod().getDeclaration());
						}
					}
				}
				methodCombo.select(0);
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		methodCombo.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				String selectMethod = methodCombo.getText();
				String selectedClass = classCombo.getText();
				for (VFClass vfclass : dataModel.listClasses()) {
					if(vfclass.getSootClass().getName().toString().equals(selectedClass))
					{
						for (VFMethod vfmethod : dataModel.listMethods(vfclass))
						{
							if(vfmethod.getSootMethod().getDeclaration().toString().equals(selectMethod))
							{
								tree.removeAll();
								listUnits = vfmethod.getUnits();
								for (VFUnit unit : listUnits) {
									TreeItem treeItem= new TreeItem(tree, SWT.NONE | SWT.BORDER);
									treeItem.setText(unit.getUnit().toString());
									if (unit.getUnit() instanceof JAssignStmt)
									{
										JAssignStmt stmt = (JAssignStmt)unit.getUnit();
										TreeItem treeLeft = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);						
										treeLeft.setText(new String[] {"Left"});
										TreeItem treeLeftValue= new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
										treeLeftValue.setText(new String[] {"Value : "+stmt.leftBox.getValue().toString()});
										TreeItem treeLeftClass= new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
										treeLeftClass.setText(new String[] {"Class : "+stmt.leftBox.getValue().getClass().toString()});

										TreeItem treeRight = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
										treeRight.setText(new String[] {"Right"});
										TreeItem treeRightValue= new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
										treeRightValue.setText(new String[] {"Value : "+stmt.leftBox.getValue().toString()});
										TreeItem treeRightClass= new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
										treeRightClass.setText(new String[] {"Class : "+stmt.leftBox.getValue().getClass().toString()});
									}
								}
							}
						}
					}
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		checkBox.addSelectionListener(new SelectionListener() {
			
			@Override
			public void widgetSelected(SelectionEvent e) {
				
				if(!listUnits.isEmpty())
				{
					tree.removeAll();
					for (VFUnit unit : listUnits) {
						TreeItem treeItem= new TreeItem(tree, SWT.NONE | SWT.BORDER);
						treeItem.setText(unit.getUnit().toString());
						if (unit.getUnit() instanceof JAssignStmt)
						{
							JAssignStmt stmt = (JAssignStmt)unit.getUnit();
							TreeItem treeLeft = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);						
							treeLeft.setText(new String[] {"Left"});
							TreeItem treeLeftValue= new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
							treeLeftValue.setText(new String[] {"Value : "+stmt.leftBox.getValue().toString()});
							TreeItem treeLeftClass= new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
							treeLeftClass.setText(new String[] {"Class : "+stmt.leftBox.getValue().getClass().toString()});

							TreeItem treeRight = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
							treeRight.setText(new String[] {"Right"});
							TreeItem treeRightValue= new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
							treeRightValue.setText(new String[] {"Value : "+stmt.leftBox.getValue().toString()});
							TreeItem treeRightClass= new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
							treeRightClass.setText(new String[] {"Class : "+stmt.leftBox.getValue().getClass().toString()});
						}
					}
				}
				
			}
			
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// TODO Auto-generated method stub
				
			}
		});
		
		

		Dictionary<String, Object> properties = new Hashtable<>();
		//properties.put(EventConstants.EVENT_TOPIC, new String[]{DataModel.EA_TOPIC_DATA_MODEL_CHANGED,DataModel.EA_TOPIC_DATA_SELECTION});
		properties.put(EventConstants.EVENT_TOPIC, DataModel.EA_TOPIC_DATA+"/*");
		ServiceUtil.registerService(EventHandler.class, this, properties);
	}

	@Override
	public void setFocus(){

	}

	@Override
	public void handleEvent(Event event) {
		if(event.getTopic().equals(DataModel.EA_TOPIC_DATA_SELECTION)) {
			getDisplay().asyncExec(new Runnable() {
				
				
				@SuppressWarnings("unchecked")
				@Override				
				public void run() {
					listUnits = (List<VFUnit>)event.getProperty("selectedMethodUnits");					
					if(checkBox.getSelection())
					{					
					tree.removeAll();
					for (VFUnit unit : listUnits) {
						TreeItem treeItem= new TreeItem(tree, SWT.NONE | SWT.BORDER);
						treeItem.setText(unit.getUnit().toString());
						if (unit.getUnit() instanceof JAssignStmt)
						{
							JAssignStmt stmt = (JAssignStmt)unit.getUnit();
							TreeItem treeLeft = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);						
							treeLeft.setText(new String[] {"Left"});
							TreeItem treeLeftValue= new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
							treeLeftValue.setText(new String[] {"Value : "+stmt.leftBox.getValue().toString()});
							TreeItem treeLeftClass= new TreeItem(treeLeft, SWT.LEFT | SWT.BORDER);
							treeLeftClass.setText(new String[] {"Class : "+stmt.leftBox.getValue().getClass().toString()});

							TreeItem treeRight = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
							treeRight.setText(new String[] {"Right"});
							TreeItem treeRightValue= new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
							treeRightValue.setText(new String[] {"Value : "+stmt.leftBox.getValue().toString()});
							TreeItem treeRightClass= new TreeItem(treeRight, SWT.LEFT | SWT.BORDER);
							treeRightClass.setText(new String[] {"Class : "+stmt.leftBox.getValue().getClass().toString()});
						}
					}
				}
				}
			});
		}
		
		
		
		if(event.getTopic().equals(DataModel.EA_TOPIC_DATA_MODEL_CHANGED)) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					dataModel = ServiceUtil.getService(DataModel.class);
					for (VFClass vfclass : dataModel.listClasses()) {
						classCombo.add(vfclass.getSootClass().getName());						
					}
					classCombo.select(0);
					classSelection = classCombo.getText().trim();
					for (VFClass vfclass : dataModel.listClasses()) {
						if(vfclass.getSootClass().getName().toString().equals(classSelection))
						{
							for (VFMethod vfmethod : dataModel.listMethods(vfclass))
							{
								methodCombo.add(vfmethod.getSootMethod().getDeclaration());
							}
						}
					}
					methodCombo.select(0);
				}
			});
		}
	}

	// TODO move this to a utility class ?!?
	public static Display getDisplay() {
		Display display = Display.getCurrent();
		if (display == null) {
			display = Display.getDefault();
		}
		return display;
	}
}

