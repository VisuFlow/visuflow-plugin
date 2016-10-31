package de.unipaderborn.visuflow.ui.view;

import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.Body;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.ui.*;
import org.eclipse.swt.widgets.Tree;
import org.eclipse.swt.widgets.TreeColumn;
import org.eclipse.swt.widgets.TreeItem;
import org.eclipse.swt.SWT;


public class UnitView extends ViewPart{

	DataModel dataModel = ServiceUtil.getService(DataModel.class);

	class ViewLabelProvider extends LabelProvider implements ILabelProvider{
		public String getColumnText(Object obj, int index){
			return getText(obj);
		}

		public Image getColumnImage(Object obj, int index){
			return getImage(obj);
		}

		public Image getImage(Object obj){
			return PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_ELEMENT);
		}
	}	   

	public void createPartControl(Composite parent){

		GridLayout layout = new GridLayout(3, true);
		parent.setLayout(layout);

		GridData gridData = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridData.widthHint = SWT.DEFAULT;
		gridData.heightHint = SWT.DEFAULT;

		Combo combo = new Combo(parent, SWT.DROP_DOWN);
		combo.setLayout(layout);
		combo.setLayoutData(gridData);
		for (VFMethod method : dataModel.listClasses().get(0).getMethods()) {

			combo.add(method.getSootMethod().getName());
		}				

		GridData gridData1 = new GridData(SWT.FILL, SWT.FILL, true, false, 1, 1);
		gridData1.widthHint = SWT.DEFAULT;
		gridData1.heightHint = SWT.DEFAULT;

		Combo combo1 = new Combo(parent, SWT.DROP_DOWN);
		combo1.setLayout(layout);
		combo1.setLayoutData(gridData1);
		combo1.setItems();

		GridData gridData3 = new GridData(SWT.FILL, SWT.FILL, true, true, 3, 1);
		gridData3.widthHint = SWT.DEFAULT;
		gridData3.heightHint = SWT.DEFAULT;

		Body body = dataModel.listClasses().get(0).getMethods().get(2).getBody();
		Tree tree = new Tree(parent, SWT.FILL| SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BACKGROUND | SWT.MULTI | SWT.BACKGROUND);
		tree.setHeaderVisible(true);
		tree.setLayout(layout);
		tree.setLayoutData(gridData3);
		TreeColumn column1 = new TreeColumn(tree, SWT.LEFT | SWT.BORDER);
		column1.setText("Unit");
		column1.setWidth(200);
		TreeColumn column2 = new TreeColumn(tree, SWT.LEFT | SWT.BORDER);
		column2.setText("Value");
		column2.setWidth(200);
		TreeColumn column3 = new TreeColumn(tree, SWT.LEFT | SWT.BORDER);
		column3.setText("Type");
		column3.setWidth(200);
		for (Unit unit : body.getUnits()) {
			TreeItem treeItem= new TreeItem(tree, SWT.NONE | SWT.BORDER);
			treeItem.setText(unit.toString());
			if (unit instanceof JAssignStmt)
			{
				JAssignStmt stmt = (JAssignStmt)unit;
				TreeItem treeItemchild = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);

				treeItemchild.setText(new String[] {"Left",stmt.leftBox.getValue().toString(),stmt.leftBox.getValue().getClass().toString()});
				TreeItem right = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
				right.setText(new String[] {"Right",stmt.rightBox.getValue().toString(),stmt.rightBox.getValue().getClass().toString()});
			}
		}
	}

	public void setFocus(){

	}

}

