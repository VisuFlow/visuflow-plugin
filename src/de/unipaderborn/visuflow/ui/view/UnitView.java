package de.unipaderborn.visuflow.ui.view;

import java.util.Dictionary;
import java.util.Hashtable;

import org.eclipse.jface.viewers.ILabelProvider;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.List;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
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
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.Body;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;


public class UnitView extends ViewPart implements EventHandler {

	DataModel dataModel;
	Tree tree;
	Combo classCombo,methodCombo;

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

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridData.widthHint = SWT.DEFAULT;
		gridData.heightHint = SWT.DEFAULT;

		classCombo = new Combo(parent, SWT.DROP_DOWN);
		classCombo.setLayout(layout);
		classCombo.setLayoutData(gridData);


		GridData gridData1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridData1.widthHint = SWT.DEFAULT;
		gridData1.heightHint = SWT.DEFAULT;

		methodCombo = new Combo(parent, SWT.DROP_DOWN);
		methodCombo.setLayout(layout);
		methodCombo.setLayoutData(gridData1);

		GridData gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gridData3.widthHint = SWT.DEFAULT;
		gridData3.heightHint = SWT.DEFAULT;

		tree = new Tree(parent, SWT.FILL| SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BACKGROUND | SWT.MULTI | SWT.BACKGROUND);
		tree.setHeaderVisible(true);
		tree.setLayout(layout);
		tree.setLayoutData(gridData3);

		Dictionary<String, String> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, DataModel.EA_TOPIC_DATA_SELECTION);
		ServiceUtil.registerService(EventHandler.class, this, properties);
	}

	@Override
	public void setFocus(){

	}

	@Override
	public void handleEvent(Event event) {
		if(event.getTopic().equals(DataModel.EA_TOPIC_DATA_SELECTION)) {
			getDisplay().asyncExec(new Runnable() {
				@Override
				public void run() {
					
					for (VFClass vfclass : dataModel.listClasses()) {
						classCombo.add(vfclass.getSootClass().toString());
						
						for(VFMethod vfmethod : dataModel.listMethods(vfclass))
						{
							methodCombo.add(vfmethod.getSootMethod().toString());
						}
					}
					tree.removeAll();
					java.util.List<Unit> listUnits = (java.util.List<Unit>)event.getProperty("selectedMethodUnits");
					for (Unit unit : listUnits) {
						TreeItem treeItem= new TreeItem(tree, SWT.NONE | SWT.BORDER);
						treeItem.setText(unit.toString());
						if (unit instanceof JAssignStmt)
						{
							JAssignStmt stmt = (JAssignStmt)unit;
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

