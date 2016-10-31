package de.unipaderborn.visuflow.ui.view;

import java.awt.Frame;

import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

import de.unipaderborn.visuflow.ui.graph.GraphManager;

public class CFGView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		createGraphComposite(parent);
	}

	private void createGraphComposite(Composite parent) {
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		Composite composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		GraphManager manager = new GraphManager("VisuFlow Graph", "url('file:styles/stylesheet.css')");
		Thread t = new Thread(manager);
		t.start();

		Frame frame = SWT_AWT.new_Frame(composite);
		frame.add(manager.getApplet());
		frame.pack();
	}

	@Override
	public void setFocus() {
		System.out.println(getClass().getName() + " Set Focus");
	}

}
