package graphstreamfeasibility.editors;


import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.LayoutManager;
import java.io.StringWriter;
import java.text.Collator;
import java.util.ArrayList;
import java.util.Collections;
import java.util.StringTokenizer;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.UnsupportedLookAndFeelException;

import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResourceChangeEvent;
import org.eclipse.core.resources.IResourceChangeListener;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.jface.dialogs.ErrorDialog;
import org.eclipse.swt.SWT;
import org.eclipse.swt.awt.SWT_AWT;
import org.eclipse.swt.custom.StyledText;
import org.eclipse.swt.graphics.Font;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.FontDialog;
import org.eclipse.ui.*;
import org.eclipse.ui.editors.text.TextEditor;
import org.eclipse.ui.part.FileEditorInput;
import org.eclipse.ui.part.MultiPageEditorPart;
import org.graphstream.graph.Graph;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.eclipse.ui.ide.IDE;

/**
 * An example showing how to create a multi-page editor.
 * This example has 3 pages:
 * <ul>
 * <li>page 0 contains a nested text editor.
 * <li>page 1 allows you to change the font used in page 2
 * <li>page 2 shows the words in page 0 in sorted order
 * </ul>
 */
public class MultiPageEditor extends MultiPageEditorPart implements IResourceChangeListener{

	/** The text editor used in page 0. */
	private TextEditor editor;

	/** The font chosen in page 1. */
	private Font font;

	/** The text widget used in page 2. */
	private StyledText text;

	/**
	 * Creates a multi-page editor example.
	 */
	public MultiPageEditor() {
		super();
		ResourcesPlugin.getWorkspace().addResourceChangeListener(this);
	}
	/**
	 * Creates page 0 of the multi-page editor,
	 * which contains a text editor.
	 */
	void createPage0() {
		try {
			editor = new TextEditor();
			int index = addPage(editor, getEditorInput());
			setPageText(index, editor.getTitle());
		} catch (PartInitException e) {
			ErrorDialog.openError(
					getSite().getShell(),
					"Error creating nested text editor",
					null,
					e.getStatus());
		}
	}
	/**
	 * Creates page 1 of the multi-page editor,
	 * which allows you to change the font used in page 2.
	 */
	void createPage1() {
		Composite composite = new Composite(getContainer(), SWT.EMBEDDED | SWT.NO_BACKGROUND);
		Frame frame = SWT_AWT.new_Frame(composite);
		
//		Composite composite = new Composite(getContainer(), SWT.NONE);
		
		Viewer viewer = new Viewer(generateTestGraph(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false);

		viewer.enableAutoLayout();
		
		JPanel panel = new JPanel(new BorderLayout());
		panel.setLayout(new BoxLayout(panel, BoxLayout.LINE_AXIS));
		
		JButton zoomInButton = new JButton();
		JButton zoomOutButton = new JButton();
		zoomInButton.setText("ZoomIn");
		zoomOutButton.setText("ZoomOut");
		
		panel.add(zoomInButton, BorderLayout.PAGE_START);
		panel.add(zoomOutButton, BorderLayout.PAGE_END);
		panel.add((Component) view);
		
		frame.add(panel);
		
		/*GridLayout layout = new GridLayout();
		composite.setLayout(layout);
		layout.numColumns = 2;

		Button fontButton = new Button(composite, SWT.NONE);
		GridData gd = new GridData(GridData.BEGINNING);
		gd.horizontalSpan = 2;
		fontButton.setLayoutData(gd);
		fontButton.setText("Change Font...");

		Viewer viewer = new Viewer(generateTestGraph(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false);

		viewer.enableAutoLayout();
		frame.add((Component) view);

		Button test = new Button(composite, SWT.NONE);
		GridData gdtest = new GridData(GridData.END);
		gd.horizontalSpan = 1;
		test.setLayoutData(gdtest);
		test.setText("Test");

		fontButton.addSelectionListener(new SelectionAdapter() {
			public void widgetSelected(SelectionEvent event) {
				setFont();
			}
		});*/

		int index = addPage(composite);
		setPageText(index, "Properties");
	}

	protected void demonstrateZoom(View view) {
		// TODO Auto-generated method stub
		double max = 5.0;
		double min = 0.0;
		double step = .20;
		long sleep = 100;

		for(double i=min;i<max;i+=step)
		{
			view.getCamera().setViewPercent(i);
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}

		for(double i=max;i>min;i-=step)
		{
			view.getCamera().setViewPercent(i);
			try {
				Thread.sleep(sleep);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
	/**
	 * Creates page 2 of the multi-page editor,
	 * which shows the sorted text.
	 */
	void createPage2() {
		Composite composite = new Composite(getContainer(), SWT.NONE);
		FillLayout layout = new FillLayout();
		composite.setLayout(layout);
		text = new StyledText(composite, SWT.H_SCROLL | SWT.V_SCROLL);
		text.setEditable(false);

		int index = addPage(composite);
		setPageText(index, "Preview");
	}

	void createSampleGraph() {

		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (ClassNotFoundException | InstantiationException | IllegalAccessException
				| UnsupportedLookAndFeelException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		Composite composite = new Composite(getContainer(), SWT.EMBEDDED | SWT.NO_BACKGROUND);
		java.awt.Frame frame = SWT_AWT.new_Frame(composite);

		Viewer viewer = new Viewer(generateTestGraph(), Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		View view = viewer.addDefaultView(false);

		viewer.enableAutoLayout();
		frame.add((Component) view);

		int index = addPage(composite);
		setPageText(index, "Sample Graph");
	}

	View createZoomTestGraph() {

		Composite composite = new Composite(getContainer(), SWT.EMBEDDED | SWT.NO_BACKGROUND);
		java.awt.Frame frame = SWT_AWT.new_Frame(composite);

		Viewer viewer = new Viewer(generateTestGraph(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false);

		viewer.enableAutoLayout();
		frame.add((Component) view);

		int index = addPage(composite);
		setPageText(index, "Zoomable Graph");
		setActivePage(index);

		ZoomDemo zoomTest = new ZoomDemo(view);
		Thread zoomer = new Thread(zoomTest);
		zoomer.start();

		return view;

	}

	public Graph generateTestGraph()
	{
		System.setProperty("sun.awt.noerasebackground", "true");
		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new MultiGraph("Tutorial 1");

		graph.addAttribute("ui.stylesheet", "url('file:../../styles/stylesheet.css')");
		graph.setStrict(false);
		graph.setAutoCreate( true );

		for (int i = 0; i < 50; i++) {
			String source = i + "";
			int temp = i + 1;
			String destination = temp + "";
			graph.addEdge(source+destination, source, destination);
			graph.addEdge(i+"", source, destination, true);
		}
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");
		//graph.addAttribute("ui.stylesheet", "graph {fill-color: white;}node {size: 10px, 15px;shape: box;fill-color: green;stroke-mode: plain;stroke-color: yellow;}node#1 {fill-color: blue;}node:clicked {fill-color: red;}");
		return graph;
	}
	/**
	 * Creates the pages of the multi-page editor.
	 */
	protected void createPages() {
		//		createZoomTestGraph();
//		createSampleGraph();
//				createCollapseTestGraph();
		//		createZoomTestGraph();
				createPage1();
	}
	private void createCollapseTestGraph() {
		CollapseSubTree collapsableGraph = new CollapseSubTree();
		// TODO Auto-generated method stub
		Composite composite = new Composite(getContainer(), SWT.EMBEDDED | SWT.NO_BACKGROUND);
		java.awt.Frame frame = SWT_AWT.new_Frame(composite);

		Viewer viewer = new Viewer(collapsableGraph.getG(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false);
		
		viewer.disableAutoLayout();

		collapsableGraph.setViewer(viewer);
		collapsableGraph.setView(view);

		viewer.enableAutoLayout();
		frame.add((Component) collapsableGraph.getView());

		int index = addPage(composite);
		setPageText(index, "Collapsable Graph Test");
		setActivePage(index);

		Thread t = new Thread(collapsableGraph);
		t.start();
	}
	/**
	 * The <code>MultiPageEditorPart</code> implementation of this 
	 * <code>IWorkbenchPart</code> method disposes all nested editors.
	 * Subclasses may extend.
	 */
	public void dispose() {
		ResourcesPlugin.getWorkspace().removeResourceChangeListener(this);
		super.dispose();
	}
	/**
	 * Saves the multi-page editor's document.
	 */
	public void doSave(IProgressMonitor monitor) {
		getEditor(0).doSave(monitor);
	}
	/**
	 * Saves the multi-page editor's document as another file.
	 * Also updates the text for page 0's tab, and updates this multi-page editor's input
	 * to correspond to the nested editor's.
	 */
	public void doSaveAs() {
		IEditorPart editor = getEditor(0);
		editor.doSaveAs();
		setPageText(0, editor.getTitle());
		setInput(editor.getEditorInput());
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart
	 */
	public void gotoMarker(IMarker marker) {
		setActivePage(0);
		IDE.gotoMarker(getEditor(0), marker);
	}
	/**
	 * The <code>MultiPageEditorExample</code> implementation of this method
	 * checks that the input is an instance of <code>IFileEditorInput</code>.
	 */
	public void init(IEditorSite site, IEditorInput editorInput)
			throws PartInitException {
		if (!(editorInput instanceof IFileEditorInput))
			throw new PartInitException("Invalid Input: Must be IFileEditorInput");
		super.init(site, editorInput);
	}
	/* (non-Javadoc)
	 * Method declared on IEditorPart.
	 */
	public boolean isSaveAsAllowed() {
		return true;
	}
	/**
	 * Calculates the contents of page 2 when the it is activated.
	 */
	protected void pageChange(int newPageIndex) {
		super.pageChange(newPageIndex);
		if (newPageIndex == 2) {
			sortWords();
		}
	}
	/**
	 * Closes all project files on project close.
	 */
	public void resourceChanged(final IResourceChangeEvent event){
		if(event.getType() == IResourceChangeEvent.PRE_CLOSE){
			Display.getDefault().asyncExec(new Runnable(){
				public void run(){
					IWorkbenchPage[] pages = getSite().getWorkbenchWindow().getPages();
					for (int i = 0; i<pages.length; i++){
						if(((FileEditorInput)editor.getEditorInput()).getFile().getProject().equals(event.getResource())){
							IEditorPart editorPart = pages[i].findEditor(editor.getEditorInput());
							pages[i].closeEditor(editorPart,true);
						}
					}
				}            
			});
		}
	}
	/**
	 * Sets the font related data to be applied to the text in page 2.
	 */
	void setFont() {
		FontDialog fontDialog = new FontDialog(getSite().getShell());
		fontDialog.setFontList(text.getFont().getFontData());
		FontData fontData = fontDialog.open();
		if (fontData != null) {
			if (font != null)
				font.dispose();
			font = new Font(text.getDisplay(), fontData);
			text.setFont(font);
		}
	}
	/**
	 * Sorts the words in page 0, and shows them in page 2.
	 */
	void sortWords() {

		String editorText =
				editor.getDocumentProvider().getDocument(editor.getEditorInput()).get();

		StringTokenizer tokenizer =
				new StringTokenizer(editorText, " \t\n\r\f!@#\u0024%^&*()-_=+`~[]{};:'\",.<>/?|\\");
		ArrayList<String> editorWords = new ArrayList<String>();
		while (tokenizer.hasMoreTokens()) {
			editorWords.add(tokenizer.nextToken());
		}

		Collections.sort(editorWords, Collator.getInstance());
		StringWriter displayText = new StringWriter();
		for (int i = 0; i < editorWords.size(); i++) {
			displayText.write(((String) editorWords.get(i)));
			displayText.write(System.getProperty("line.separator"));
		}
		text.setText(displayText.toString());
	}
}