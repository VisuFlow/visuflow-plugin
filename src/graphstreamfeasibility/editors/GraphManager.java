package graphstreamfeasibility.editors;

import java.util.Iterator;

import org.graphstream.algorithm.generator.BaseGenerator;
import org.graphstream.algorithm.generator.LobsterGenerator;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.stream.ProxyPipe;
import org.graphstream.stream.SinkAdapter;
import org.graphstream.ui.view.View;
import org.graphstream.ui.view.Viewer;

public class GraphManager implements Runnable {
	
	Viewer viewer;
	View view;
	Graph graph;
	
	String stylesheet = "" +
            "node {" +
            "   text-background-mode: rounded-box;" +
            "   text-background-color: gray; " +
            "   text-color: #222;" +
            "}" +
            "node.plus {" +
            "   text-background-color: #F33; " +
            "   text-color: #DDD;" +
            "}";
	
	GraphManager()
	{
//		createSampleGraph();
		createSampleGraphFromGenarator();
	}
	
	public Graph generateTestGraph()
	{
		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new MultiGraph("Tutorial 1");

		graph.addAttribute("ui.stylesheet", "url('file:C:/Users/Shashank B S/visuflow/GraphStreamFeasibility/styles/stylesheet.css')");
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
	
	public Graph generateTestGraphfromGenerator()
	{
		System.setProperty("gs.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		Graph graph = new MultiGraph("Tutorial 1");
		graph.setAttribute("stylesheet", stylesheet);
		setGraph(graph);
		
		return graph;
	}
	
	public Graph getGraph() {
		return graph;
	}

	public void setGraph(Graph graph) {
		this.graph = graph;
	}

	void createSampleGraphFromGenarator() {

		Viewer viewer = new Viewer(generateTestGraphfromGenerator(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false);
		viewer.enableAutoLayout();
		setViewer(viewer);
		setView(view);
	}
	
	void generateAndSink(Graph g)
	{
		BaseGenerator gen  = new LobsterGenerator();
        gen.setDirectedEdges(true, false);
        gen.addNodeLabels(true);
        gen.addSink(g);

        gen.begin();
        for (int i = 0; i < 30; i++) {
            gen.nextEvents();
        }
        gen.end();

        ProxyPipe fromViewer = viewer.newViewerPipe();
        fromViewer.addSink(g);

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
            void toggleNode(String id){
                Node n  = g.getNode(id);
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
        });
	}
	
	void createSampleGraph() {

		Viewer viewer = new Viewer(generateTestGraph(), Viewer.ThreadingModel.GRAPH_IN_ANOTHER_THREAD);
		View view = viewer.addDefaultView(false);
		viewer.enableAutoLayout();
		setViewer(viewer);
		setView(view);
	}
	
	protected void demonstrateZoom() {
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
		
		try {
			Thread.sleep(2000);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		
		view.getCamera().resetView();
		
	}

	public Viewer getViewer() {
		return viewer;
	}

	public void setViewer(Viewer viewer) {
		this.viewer = viewer;
	}

	public View getView() {
		return view;
	}

	public void setView(View view) {
		this.view = view;
	}

	@Override
	public void run() {
		// TODO Auto-generated method stub
		//createSampleGraph();
//		demonstrateZoom();
//		createSampleGraphFromGenarator();
//		generateAndSink(graph);
		
		BaseGenerator gen  = new LobsterGenerator();
        gen.setDirectedEdges(true, false);
        gen.addNodeLabels(true);
        gen.addSink(graph);

        gen.begin();
        for (int i = 0; i < 30; i++) {
            gen.nextEvents();
        }
        gen.end();

        ProxyPipe fromViewer = viewer.newViewerPipe();
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
        });
	}
	
}
