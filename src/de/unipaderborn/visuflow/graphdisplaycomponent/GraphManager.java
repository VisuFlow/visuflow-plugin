package de.unipaderborn.visuflow.graphdisplaycomponent;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Container;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import java.util.ListIterator;
import java.util.Map.Entry;

import javax.swing.BorderFactory;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JFrame;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.border.Border;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.graphstream.ui.view.ViewerPipe;

import de.visuflow.callgraph.CallGraphGenerator;
import de.visuflow.callgraph.GraphStructure;
import graphstreamfeasibility.editors.test;
import soot.SootMethod;

public class GraphManager implements Runnable, ViewerListener {

    Graph graph;
    String styleSheet;
    private Viewer viewer;
    private ViewPanel view;
    HashMap<SootMethod, GraphStructure> analysisData;

    ViewerPipe fromViewer;

    Container panel;
    JApplet applet;
    JButton zoomInButton, zoomOutButton, viewCenterButton, filterGraphButton, toggleLayout;
    JToolBar settingsBar;
    JTextField attribute;
    JScrollPane scrollbar;
    JComboBox<String> methodList;
    JTable nodeData;

    double zoomInDelta, zoomOutDelta, maxZoomPercent, minZoomPercent;

    boolean autoLayoutEnabled = false;

    Layout graphLayout = new SpringBox();
    private Border raisedbevel = BorderFactory.createRaisedBevelBorder();

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
    }

    public Container getApplet() {
        return applet.getRootPane();
    }

    void createGraph(String graphName)
    {
        if(graph == null && graphName != null)
            graph = new MultiGraph(graphName);

        graph.clear();
        graph.addAttribute("ui.stylesheet", styleSheet);
        graph.setStrict(true);
        graph.setAutoCreate(true);
        graph.addAttribute("ui.quality");
        graph.addAttribute("ui.antialias");

        viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
        viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);

        view = viewer.addDefaultView(false);
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
        filterGraphButton.setBorder(raisedbevel);

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
        methodList = new JComboBox<>();
        methodList.addItem("Select Method");

        methodList.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                @SuppressWarnings("unchecked")
                JComboBox<String> methodBox = (JComboBox<String>) e.getSource();
                if(analysisData == null)
                    System.out.println("analysis data is null");
                try {
                    renderMethodCFG((GraphStructure) analysisData.values().toArray()[methodBox.getSelectedIndex()-1]);
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
                double viewPercent = view.getCamera().getViewPercent();
                if(rotationDirection > 0 && viewPercent > maxZoomPercent)
                {
                    view.getCamera().setViewPercent(viewPercent - zoomInDelta);
                }
                else if(viewPercent < minZoomPercent)
                {
                    view.getCamera().setViewPercent(viewPercent + zoomOutDelta);
                }
            }
        });

        view.addMouseMotionListener(new MouseMotionListener() {

            @Override
            public void mouseMoved(MouseEvent e) {
                // TODO Auto-generated method stub
                /*try {
					Collection<GraphicElement> highlightedNodes = ((View) e.getComponent()).allNodesOrSpritesIn(e.getX(), e.getY(), e.getX(), e.getY());
					Iterator<GraphicElement> nodes = highlightedNodes.iterator();
					while(nodes.hasNext())
					{
						Node curr = (Node) nodes.next();
						System.out.println("left " + curr.getAttribute("leftOp"));
						System.out.println("right " + curr.getAttribute("rightOp"));
					}
					//					System.out.println("Node to display pop info about " + ((View) e.getComponent()).allNodesOrSpritesIn(e.getX(), e.getY(), e.getX(), e.getY()));
				} catch (Exception e1) {
					// TODO Auto-generated catch block
					e1.printStackTrace();
				}*/
            }

            @Override
            public void mouseDragged(MouseEvent e) {
                // TODO Auto-generated method stub
                if(e.getButton() == 0)
                {
                    /*Point dest = e.getPoint();
					System.out.println("dragged with button");
					System.out.println(dest);*/

                    Point3 currViewCenter = view.getCamera().getViewCenter();
                    System.out.println("currentViewCenter " + currViewCenter);
                    System.out.println("clickCount " + e.getLocationOnScreen());

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
                }
            }
        });

        view.addMouseListener(new MouseListener() {

            @Override
            public void mouseReleased(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mousePressed(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseExited(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseEntered(MouseEvent e) {
                // TODO Auto-generated method stub

            }

            @Override
            public void mouseClicked(MouseEvent e) {
                // TODO Auto-generated method stub
                System.out.println("Inside the event " + e.getButton());
                if(e.getButton() == MouseEvent.BUTTON3)
                {
                    try {
                        Collection<GraphicElement> highlightedNodes = ((View) e.getComponent()).allNodesOrSpritesIn(e.getX(), e.getY(), e.getX(), e.getY());
                        Iterator<GraphicElement> nodes = highlightedNodes.iterator();
                        if(nodes.hasNext())
                        {
                            System.out.println("Inside the event");
                            test lineHighlighter = new test();
                            lineHighlighter.highlightCodeLine();
                        }
                    } catch (Exception e1) {
                        // TODO Auto-generated catch block
                        e1.printStackTrace();
                    }
                }
            }
        });
    }

    private void createZoomControls() {
        // TODO Auto-generated method stub
        zoomInButton = new JButton("+");
        zoomOutButton = new JButton("-");
        viewCenterButton = new JButton("reset");

        zoomInButton.setBackground(Color.gray);
        zoomInButton.setBorder(raisedbevel);
        zoomInButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                System.out.println("event triggered for zoomin");
                double viewPercent = view.getCamera().getViewPercent();
                //				if(viewPercent > maxZoomPercent)
                view.getCamera().setViewPercent(viewPercent - zoomInDelta);
            }
        });

        zoomOutButton.setBackground(Color.gray);
        zoomOutButton.setBorder(raisedbevel);
        zoomOutButton.addActionListener(new ActionListener() {

            @Override
            public void actionPerformed(ActionEvent e) {
                // TODO Auto-generated method stub
                double viewPercent = view.getCamera().getViewPercent();
                System.out.println("event triggered for zoomout");
                //				if(viewPercent < minZoomPercent)
                view.getCamera().setViewPercent(viewPercent + zoomOutDelta);
            }
        });

        viewCenterButton.setBorder(raisedbevel);
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
        toggleLayout.setBorder(raisedbevel);
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
                viewer.enableAutoLayout(graphLayout);
            }
            else if(viewer != null)
            {
                viewer.enableAutoLayout();
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

    void generateTestGraph()
    {
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
    }

    void generateGraphFromGraphStructure()
    {
        CallGraphGenerator generator = new CallGraphGenerator();
        analysisData = new HashMap<>();
        generator.runAnalysis(analysisData);

        Iterator<Entry<SootMethod, GraphStructure>> methodIterator = analysisData.entrySet().iterator();
        while(methodIterator.hasNext())
        {
            Entry<SootMethod, GraphStructure> curr = methodIterator.next();
            SootMethod currMethod = curr.getKey();
            methodList.addItem(currMethod.getName());
        }
        //		renderMethodCFG(analysisData.entrySet().iterator().next().getValue());
        experimentalLayout();
    }

    private void createNode(de.visuflow.callgraph.Node node)
    {
        if(graph.getNode(node.getId() + "") == null)
        {
            Node newNode = graph.addNode(node.getId() + "");
            newNode.setAttribute("ui.label", node.getLabel());
        }
    }

    private void createEdge(de.visuflow.callgraph.Node src, de.visuflow.callgraph.Node dest)
    {
        if(graph.getEdge("" + src.getId() + dest.getId()) == null)
            graph.addEdge(src.getId() + "" + dest.getId(), src.getId() + "", dest.getId() + "", true);
    }

    private void renderMethodCFG(GraphStructure interGraph) throws Exception
    {
        String[] columnNames = {"First Name",
                "Last Name",
                "Sport",
                "# of Years",
        "Vegetarian"};

        Object[][] data = {
                {"Kathy", "Smith",
                    "Snowboarding", new Integer(5), new Boolean(false)},
                {"John", "Doe",
                        "Rowing", new Integer(3), new Boolean(true)},
                {"Sue", "Black",
                            "Knitting", new Integer(2), new Boolean(false)},
                {"Jane", "White",
                                "Speed reading", new Integer(20), new Boolean(true)},
                {"Joe", "Brown",
                                    "Pool", new Integer(10), new Boolean(false)}
        };

        if(interGraph == null)
            throw new Exception("GraphStructure is null");

        createGraph(null);
        ListIterator<de.visuflow.callgraph.Edge> edgeIterator = interGraph.listEdges.listIterator();

        while(edgeIterator.hasNext())
        {
            de.visuflow.callgraph.Edge currEdgeIterator = edgeIterator.next();

            de.visuflow.callgraph.Node src = currEdgeIterator.getSource();
            de.visuflow.callgraph.Node dest = currEdgeIterator.getDestination();

            createNode(src);
            createNode(dest);
            createEdge(src, dest);
        }

        //		PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().showView("");

        /*Display display = PlatformUI.createDisplay();
		display.syncExec(new Runnable() {

			@Override
			public void run() {
				// TODO Auto-generated method stub
				try {
					NodeView view = (NodeView) PlatformUI.getWorkbench()
							.getActiveWorkbenchWindow()
							.getActivePage()
							.showView("visuflow.nodeView");

					System.out.println("View created ");

					view.setColumnNames(columnNames);
					view.setData(data);

					System.out.println("Data has been set");
				} catch (PartInitException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			}
		});
		System.out.println("Active shell " + display.getActiveShell());*/
    }

    private void experimentalLayout()
    {
        //		viewer.disableAutoLayout();
        int spacing = 2;
        int rowSpacing = 2;
        int nodeCount = graph.getNodeCount() * spacing;
        Iterator<Node> nodeIterator = graph.getNodeIterator();
        while(nodeIterator.hasNext())
        {
            Node curr = nodeIterator.next();

            Iterator<Edge> leavingEdgeIterator = curr.getEdgeIterator();
            int outEdges = 0;
            while(leavingEdgeIterator.hasNext())
            {
                Edge outEdge = leavingEdgeIterator.next();
                Node target = outEdge.getTargetNode();
                target.setAttribute("xyz", outEdges, nodeCount, 0);
                outEdges += rowSpacing;
            }

            curr.setAttribute("xyz", 0, nodeCount, 0);
            nodeCount -= spacing;
        }
    }

    void generateGraphFromGenerator()
    {
        BaseGenerator gen  = new LobsterGenerator();
        gen.setDirectedEdges(true, false);
        gen.addNodeLabels(true);
        gen.addSink(graph);

        gen.begin();
        for (int i = 0; i < 100; i++) {
            gen.nextEvents();
        }
        gen.end();

        fromViewer = viewer.newViewerPipe();
        fromViewer.addSink(graph);

        fromViewer.addSink(new SinkAdapter(){
            @Override
            public void nodeAttributeAdded(String sourceId, long timeId, String nodeId, String attribute, Object value) {
                if(attribute.equals("ui.clicked")){
                    toggleNode(nodeId);
                }
            }

            @Override
            public void nodeAttributeChanged(String sourceId, long timeId, String nodeId, String attribute, Object oldValue, Object newValue) {
                if(attribute.equals("ui.clicked")){
                    toggleNode(nodeId);
                }
            }
        });
    }

    void toggleNode(String id){
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
                    m.setAttribute("x", ((double) pos[0]) + Math.random() * 0.0001);
                    m.setAttribute("y", ((double) pos[1]) + Math.random() * 0.0001);

                    m.setAttribute("ui.hide");
                }

            }
        }
    }

    @Override
    public void run() {
        // TODO Auto-generated method stub
        generateGraphFromGraphStructure();

        fromViewer = viewer.newViewerPipe();
        fromViewer.addViewerListener(this);
        fromViewer.addSink(graph);

        while(true)
            fromViewer.pump();
    }

    @Override
    public void buttonPushed(String id) {
        // TODO Auto-generated method stub
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