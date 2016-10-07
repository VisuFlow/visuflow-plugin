package de.unipaderborn.visuflow;

import java.awt.Frame;

import javax.swing.JTable;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class NodeView extends ViewPart {

	JTable nodeTable;
	private String[] columnNames = {""};
	private Object[][] data = {{""}};

	public NodeView(String[] columns, Object[][] data) {
		// TODO Auto-generated constructor stub
		this.columnNames = columns;
		this.data = data;
	}
	
	public NodeView() {
		// TODO Auto-generated constructor stub
		String[] columnNames = {"test"};
		
		Object[][] data = {
				{"test"}};
		
		this.columnNames = columnNames;
		this.data = data;
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
