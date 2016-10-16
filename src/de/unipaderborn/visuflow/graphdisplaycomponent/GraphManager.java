package de.unipaderborn.visuflow.graphdisplaycomponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.JToolBar;

import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.util.ServiceUtil;
import de.visuflow.callgraph.ControlFlowGraph;

public class GraphManager implements Runnable, ViewerListener {

	Graph graph;
	String styleSheet;
	private Viewer viewer;
	private ViewPanel view;
	List<VFClass> analysisData;

	ViewerPipe fromViewer;

	Container panel;
	JApplet applet;
	JButton zoomInButton, zoomOutButton, viewCenterButton, filterGraphButton, toggleLayout;
	JToolBar settingsBar;
	JTextField attribute;
	JScrollPane scrollbar;
	JComboBox<VFMethod> methodList;

	double zoomInDelta, zoomOutDelta, maxZoomPercent, minZoomPercent;

	boolean autoLayoutEnabled = false;

	Layout graphLayout = new SpringBox();

	public GraphManager(String graphName, String styleSheet)
	{
		System.setProperty("sun.awt.noerasebackground", "true");
		System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		this.zoomInDelta = .2;
		this.zoomOutDelta = .2;
		this.maxZoomPercent = .5;
		this.minZoomPercent = 2.0;
		this.styleSheet = styleSheet;
		createGraph(graphName);
		createUI();

		EventHandler dataModelHandler = new EventHandler() {
			@Override
			public void handleEvent(Event event) {
				@SuppressWarnings("unchecked")
				List<VFClass> vfClasses = (List<VFClass>) event.getProperty("model");
				System.out.println("Model changed " + vfClasses.size() + " " + vfClasses);
			}
		};
		Hashtable<String, String> properties = new Hashtable<String, String>();
		properties.put(EventConstants.EVENT_TOPIC, DataModel.EA_TOPIC_DATA_MODEL_CHANGED);
		ServiceUtil.registerService(EventHandler.class, dataModelHandler, properties);
	}

	public Container getApplet() {
		return applet.getRootPane();
	}

	void createGraph(String graphName)
	{
		if(graph == null && graphName != null)
		{
			System.out.println("Graph object is null... Creating a new Graph");
			graph = new MultiGraph(graphName);
		}

		graph.clear();
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.setStrict(true);
		graph.setAutoCreate(true);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");

		viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);

		view = viewer.addDefaultView(false);
		fromViewer = viewer.newViewerPipe();
	}

	private void createUI() {
		// TODO Auto-generated method stub
		createZoomControls();
		createViewListeners();
		createAttributeControls();
		createToggleLayoutButton();
		createMethodComboBox();
		createSettingsBar();
		createPanel();
		createAppletContainer();
	}

	private void createAppletContainer() {
		// TODO Auto-generated method stub
		applet = new JApplet();

		scrollbar = new JScrollPane(panel);
		applet.add(scrollbar);
	}

	private void createAttributeControls() {
		// TODO Auto-generated method stub
		attribute = new JTextField("ui.screenshot,C:/Users/Shashank B S/Desktop/image.png");
		filterGraphButton = new JButton("SetAttribute");

		filterGraphButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				String[] newAttribute = attribute.getText().split(",");
				graph.setAttribute(newAttribute[0], newAttribute[1]);
			}
		});
	}

	private void createMethodComboBox()
	{
		methodList = new JComboBox<VFMethod>();
//		methodList.addItem("Select Method");

		methodList.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				@SuppressWarnings("unchecked")
				JComboBox<String> methodBox = (JComboBox<String>) e.getSource();
				if(analysisData == null)
					System.out.println("analysis data is null");
				try {
					VFMethod selectedMethod = (VFMethod) methodBox.getSelectedItem();
					System.out.println(selectedMethod.getControlFlowGraph().listEdges.size());
					System.out.println(selectedMethod.getControlFlowGraph().listNodes.size());
					renderMethodCFG(selectedMethod.getControlFlowGraph());
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}
				experimentalLayout();
			}
		});
	}

	private void createSettingsBar() {
		// TODO Auto-generated method stub
		settingsBar = new JToolBar("ControlsBar", JToolBar.HORIZONTAL);

		settingsBar.add(zoomInButton);
		settingsBar.add(zoomOutButton);
		settingsBar.add(viewCenterButton);
		settingsBar.add(methodList);
		settingsBar.add(filterGraphButton);
		settingsBar.add(attribute);
		settingsBar.add(toggleLayout);
	}

	private void createPanel() {
		// TODO Auto-generated method stub
		panel = new JFrame().getContentPane();
		panel.add(view);
		panel.add(settingsBar, BorderLayout.PAGE_START);
	}

	private void createViewListeners() {
		// TODO Auto-generated method stub
		view.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				// TODO Auto-generated method stub
				int rotationDirection = e.getWheelRotation();
				if(rotationDirection > 0)
					zoomIn();
				else
					zoomOut();
			}
		});

		view.addMouseMotionListener(new MouseMotionListener() {

			@Override
			public void mouseMoved(MouseEvent e) {
				// TODO Auto-generated method stub
				/*try {
					Collection<GraphicElement> highlightedNodes = ( (View) e.getComponent()).allNodesOrSpritesIn(e.getX(), e.getY(), e.getX(), e.getY());
					Iterator<GraphicElement> nodes = highlightedNodes.iterator();
					while(nodes.hasNext())
					{
						Node curr = (Node) nodes.next();
//						System.out.println("left " + curr.getAttribute("leftOp"));
//						System.out.println("right " + curr.getAttribute("rightOp"));
					}
					//System.out.println("Node to display pop info about " + ((View) e.getComponent()).allNodesOrSpritesIn(e.getX(), e.getY(), e.getX(), e.getY()));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				// TODO Auto-generated method stub
				/*if(e.getButton() == 0)
				{
					Point dest = e.getPoint();
					System.out.println("dragged with button");
					System.out.println(dest);

					Point3 currViewCenter = view.getCamera().getViewCenter();

					for(int i=0; i<e.getClickCount(); i++)
					{
						view.getCamera().setViewCenter(currViewCenter.x+.2, currViewCenter.y+.2, 0);
						//						try {
						//							Thread.sleep(1000);
						//						} catch (InterruptedException e1) {
						//							// TODO Auto-generated catch block
						//							e1.printStackTrace();
						//						}
					}
				}*/
			}
		});
	}

	private void zoomIn()
	{
		double viewPercent = view.getCamera().getViewPercent();
		System.out.println("zooming In " + viewPercent);
		if(viewPercent > maxZoomPercent)
			view.getCamera().setViewPercent(viewPercent - zoomInDelta);
	}

	private void zoomOut()
	{
		double viewPercent = view.getCamera().getViewPercent();
		if(viewPercent < minZoomPercent)
			view.getCamera().setViewPercent(viewPercent + zoomOutDelta);
	}

	private void createZoomControls() {
		// TODO Auto-generated method stub
		zoomInButton = new JButton("+");
		zoomOutButton = new JButton("-");
		viewCenterButton = new JButton("reset");

		zoomInButton.setBackground(Color.gray);
		zoomInButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				zoomIn();
			}
		});

		zoomOutButton.setBackground(Color.gray);
		zoomOutButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				zoomOut();
			}
		});

		viewCenterButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				view.getCamera().resetView();
			}
		});
	}

	private void createToggleLayoutButton()
	{
		toggleLayout = new JButton();
		toggleAutoLayout();
		toggleLayout.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				// TODO Auto-generated method stub
				toggleAutoLayout();
			}
		});
	}

	private void toggleAutoLayout()
	{
		if(!autoLayoutEnabled)
		{
			if(viewer != null && graphLayout != null)
			{
				//				viewer.enableAutoLayout(graphLayout);
				experimentalLayout();
			}
			else if(viewer != null)
			{
				//				viewer.enableAutoLayout();
				experimentalLayout();
			}
			autoLayoutEnabled = true;
			toggleLayout.setText("Disable Layouting");
		}
		else
		{
			viewer.disableAutoLayout();
			autoLayoutEnabled = false;
			toggleLayout.setText("Enable Layouting");
		}
	}

	void generateGraphFromGraphStructure()
	{
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		analysisData = dataModel.listClasses();
		if(!analysisData.isEmpty()) {
			VFClass first = analysisData.get(0);
			for (VFMethod vfMethod : first.getMethods()) {
				methodList.addItem(vfMethod);
			}
		}
	}

	private void renderMethodCFG(ControlFlowGraph interGraph) throws Exception
	{
		if(interGraph == null)
			throw new Exception("GraphStructure is null");

		createGraph(null);
		graph.addAttribute("ui.stylesheet", styleSheet);
		ListIterator<de.visuflow.callgraph.Edge> edgeIterator = interGraph.listEdges.listIterator();

		while(edgeIterator.hasNext())
		{
			de.visuflow.callgraph.Edge currEdgeIterator = edgeIterator.next();

			de.visuflow.callgraph.Node src = currEdgeIterator.getSource();
			de.visuflow.callgraph.Node dest = currEdgeIterator.getDestination();

			if(graph.getNode(src.getId() + "") == null)
				graph.addNode(src.getId() + "").setAttribute("ui.label", src.getLabel());

			if(graph.getNode(dest.getId() + "") == null)
				graph.addNode(dest.getId() + "").setAttribute("ui.label", dest.getLabel());

			if(graph.getEdge("" + src.getId() + dest.getId()) == null)
				graph.addEdge(src.getId() + "" + dest.getId(), src.getId() + "", dest.getId() + "", true);
		}
	}

	private void experimentalLayout()
	{
		//		viewer.disableAutoLayout();
		double spacing = 2.0;
		double rowSpacing = 12.0;
		double nodeCount = graph.getNodeCount() * spacing;
		Iterator<Node> nodeIterator = graph.getNodeIterator();
		while(nodeIterator.hasNext())
		{
			Node curr = nodeIterator.next();

			Iterator<Edge> leavingEdgeIterator = curr.getEdgeIterator();
			double outEdges = 0.0;
			while(leavingEdgeIterator.hasNext())
			{
				Edge outEdge = leavingEdgeIterator.next();
				Node target = outEdge.getTargetNode();
				target.setAttribute("xyz", outEdges, nodeCount, 0.0);
				outEdges += rowSpacing;
			}

			curr.setAttribute("xyz", 0.0, nodeCount, 0.0);
			nodeCount -= spacing;
		}
	}

	void toggleNode(String id){
		System.out.println("Togglenodes called");
		Node n  = graph.getNode(id);
		Object[] pos = n.getAttribute("xyz");
		Iterator<Node> it = n.getBreadthFirstIterator(true);
		if(n.hasAttribute("collapsed")){
			n.removeAttribute("collapsed");
			while(it.hasNext()){
				Node m  =  it.next();

				for(Edge e : m.getLeavingEdgeSet()) {
					e.removeAttribute("ui.hide");
				}
				m.removeAttribute("layout.frozen");
				m.setAttribute("x",((double)pos[0])+Math.random()*0.0001);
				m.setAttribute("y",((double)pos[1])+Math.random()*0.0001);

				m.removeAttribute("ui.hide");

			}
			n.removeAttribute("ui.class");

		} else {
			n.setAttribute("ui.class", "plus");
			n.setAttribute("collapsed");

			while(it.hasNext()){
				Node m  =  it.next();

				for(Edge e : m.getLeavingEdgeSet()) {
					e.setAttribute("ui.hide");
				}
				if(n != m) {
					m.setAttribute("layout.frozen");
					//					m.setAttribute("x", ((double) pos[0]) + Math.random() * 0.0001);
					//					m.setAttribute("y", ((double) pos[1]) + Math.random() * 0.0001);

					m.setAttribute("xyz", ((double) pos[0]) + Math.random() * 0.0001, ((double) pos[1]) + Math.random() * 0.0001, 0.0);

					m.setAttribute("ui.hide");
				}

			}
		}
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		generateGraphFromGraphStructure();

//		fromViewer = viewer.newViewerPipe();
		fromViewer.addViewerListener(this);
		fromViewer.addSink(graph);

		// FIXME the Thread.sleep slows down the loop, so that it does not eat up the CPU
        // but this really should be implemented differently. isn't there an event listener
        // or something we can use, so that we call pump() only when necessary
        while(true) {
        	try {
				Thread.sleep(1);
			} catch (InterruptedException e) {
			}
            fromViewer.pump();
        }
	}

	@Override
	public void buttonPushed(String id) {
		// TODO Auto-generated method stub
		System.out.println("Button pushed on node " + id);
		toggleNode(id);
		experimentalLayout();
		//		Node selectedNode = graph.getNode(id);
		//		if(selectedNode.hasAttribute("ui.class"))
		//		{
		//			System.out.println("Node has attribute clicked");
		//			selectedNode.removeAttribute("ui.class");
		//		}
		//		selectedNode.addAttribute("ui.class", "clicked");
	}

	@Override
	public void buttonReleased(String id) {
		// TODO Auto-generated method stub

	}

	@Override
	public void viewClosed(String id) {
		// TODO Auto-generated method stub

	}
}