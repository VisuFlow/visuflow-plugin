package de.unipaderborn.visuflow.ui.view;

import java.awt.Frame;
import java.net.URL;

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
			e.printStackTrace();
		}

		Composite composite = new Composite(parent, SWT.EMBEDDED | SWT.NO_BACKGROUND);
		ClassLoader loader = CFGView.class.getClassLoader();
		//URL stylesheetUrl = loader.getResource("/styles/stylesheet.css");
		URL stylesheetUrl = loader.getResource("/styles/myStyleSheet.css");
		System.out.println("Loading stylesheet from " + stylesheetUrl.toExternalForm());
		GraphManager manager = new GraphManager("VisuFlow Graph", "url('"+stylesheetUrl.toString()+"')");
		Thread t = new Thread(manager);
		t.start();

		System.out.println("CFG view created");

		Frame frame = SWT_AWT.new_Frame(composite);
		frame.add(manager.getApplet());
		frame.pack();
	}

	@Override
	public void setFocus() {
		System.out.println(getClass().getName() + " Set Focus");
	}

}
