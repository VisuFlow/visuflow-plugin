package de.unipaderborn.visuflow;

import java.awt.Frame;
import java.util.List;

import javax.swing.JTable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class ResultView extends ViewPart {

	JTable nodeTable;
	String[] columnNames = {"Unit","Statement Type"};
	Object[][] data = new Object[10000][2];
	private List<VFClass> analysisData;

	public ResultView() {
		// TODO Auto-generated constructor stub
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		analysisData = dataModel.listClasses();
		if(!analysisData.isEmpty()) {
			VFClass first = analysisData.get(0);
			for (VFMethod vfMethod : first.getMethods()) {
				int row = 0;
				for(VFUnit vfUnit : vfMethod.getUnits()) {
					data[row][0] = vfUnit.getUnit();
					data[row][1] = vfUnit.getUnit().getClass();
					row++;
				}
			}
		}
	}

	public void setColumnNames(String[] columnNames) {
		this.columnNames = columnNames;
	}

	public void setData(Object[][] data) {
		this.data = data;
	}

	@Override
	public void createPartControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NO_BACKGROUND | SWT.EMBEDDED);
		Frame frame = SWT_AWT.new_Frame(composite);

		nodeTable = new JTable(data, columnNames);
		frame.add(nodeTable);
		frame.pack();
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub

	}

}
