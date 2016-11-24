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
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.Body;
import soot.Unit;
import soot.jimple.internal.JAssignStmt;
import soot.tagkit.Tag;


public class UnitView extends ViewPart implements EventHandler {

    DataModel dataModel = ServiceUtil.getService(DataModel.class);
    Tree tree;
    Combo combo;

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

        combo = new Combo(parent, SWT.DROP_DOWN);
        combo.setLayout(layout);
        combo.setLayoutData(gridData);
        combo.addSelectionListener(new SelectionListener() {

            @Override
            public void widgetSelected(SelectionEvent e) {
                int index = combo.getSelectionIndex();
                VFMethod method = dataModel.listClasses().get(0).getMethods().get(index);
                dataModel.setSelectedMethod(method);
            }

            @Override
            public void widgetDefaultSelected(SelectionEvent e) {
                System.out.println(e);
            }
        });

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

        tree = new Tree(parent, SWT.FILL| SWT.LEFT | SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL | SWT.BACKGROUND | SWT.MULTI | SWT.BACKGROUND);
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

        //        Body body = dataModel.listClasses().get(0).getMethods().get(2).getBody();
        //        for (Unit unit : body.getUnits()) {
        //            TreeItem treeItem= new TreeItem(tree, SWT.NONE | SWT.BORDER);
        //            treeItem.setText(unit.toString());
        //            if (unit instanceof JAssignStmt)
        //            {
        //                JAssignStmt stmt = (JAssignStmt)unit;
        //                TreeItem treeItemchild = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
        //
        //                treeItemchild.setText(new String[] {"Left",stmt.leftBox.getValue().toString(),stmt.leftBox.getValue().getClass().toString()});
        //                TreeItem right = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
        //                right.setText(new String[] {"Right",stmt.rightBox.getValue().toString(),stmt.rightBox.getValue().getClass().toString()});
        //            }
        //        }

        Dictionary<String, String> properties = new Hashtable<>();
        properties.put(EventConstants.EVENT_TOPIC, DataModel.EA_TOPIC_DATA_MODEL_CHANGED);
        ServiceUtil.registerService(EventHandler.class, this, properties);
    }

    @Override
    public void setFocus(){

    }

    @Override
    public void handleEvent(Event event) {
        if(event.getTopic().equals(DataModel.EA_TOPIC_DATA_MODEL_CHANGED)) {
            getDisplay().asyncExec(new Runnable() {
                @Override
                public void run() {
                    Body body = dataModel.listClasses().get(0).getMethods().get(0).getBody();
                    tree.removeAll();
                    for (Unit unit : body.getUnits()) {
                        TreeItem treeItem= new TreeItem(tree, SWT.NONE | SWT.BORDER);
                        treeItem.setText(unit.toString());
                        Tag fqn = unit.getTag("visuflow.unit.fqn");
                        try {
                            System.out.println(new String(fqn.getValue(), "utf-8"));
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                        if (unit instanceof JAssignStmt)
                        {
                            JAssignStmt stmt = (JAssignStmt)unit;
                            TreeItem treeItemchild = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);

                            treeItemchild.setText(new String[] {"Left",stmt.leftBox.getValue().toString(),stmt.leftBox.getValue().getClass().toString()});
                            TreeItem right = new TreeItem(treeItem, SWT.LEFT | SWT.BORDER);
                            right.setText(new String[] {"Right",stmt.rightBox.getValue().toString(),stmt.rightBox.getValue().getClass().toString()});
                        }
                    }

                    combo.removeAll();
                    for (VFMethod method : dataModel.listClasses().get(0).getMethods()) {
                        combo.add(method.getSootMethod().getName());
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

