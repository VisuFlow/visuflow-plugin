package graphstreamfeasibility.views;

import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.*;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.Body;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;

import org.eclipse.jface.viewers.*;
import org.eclipse.swt.graphics.Image;
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
		
		Body body = dataModel.listClasses().get(0).getMethods().get(2).getBody();
		Tree tree = new Tree(parent, SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BACKGROUND | SWT.MULTI | SWT.BACKGROUND);
	    tree.setHeaderVisible(true);
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

