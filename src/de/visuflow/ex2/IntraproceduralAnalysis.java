package de.visuflow.ex2;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import de.visuflow.reporting.IReporter;
import heros.edgefunc.EdgeIdentity;
import soot.Body;
import soot.PhaseOptions;
import soot.SootMethod;
import soot.Unit;
import soot.dava.StaticDefinitionFinder;
import soot.options.Options;
import soot.tagkit.SourceLnPosTag;
import soot.toolkits.graph.ExceptionalUnitGraph;
import soot.toolkits.scalar.ForwardFlowAnalysis;
import soot.toolkits.graph.*;
import de.visuflow.ex2.*;

public class IntraproceduralAnalysis extends ForwardFlowAnalysis<Unit, Set<FlowAbstraction>> {
	public int flowThroughCount = 0;
	private final SootMethod method;
	private final IReporter reporter;
	static ExceptionalUnitGraph eg;
	public static int nodeNumber=0;
	public static int nodeCount=0;
	public static int edgeCount=0;
	public static HashMap<Unit,Integer> nodesMap = new HashMap<>();
	public static HashMap<Integer, List<Integer>> edgesMap = new HashMap<>();
	public List<HashMap> listEdges1 = new ArrayList<HashMap>();
	public static Node[] nodes = new Node[20];
	public static Edge[] edges = new Edge[20];
	public static List<Node> listNodes = new ArrayList<>();
	public static List<Edge> listEdges = new ArrayList<>();
	
	
	public IntraproceduralAnalysis(Body b, IReporter reporter, GraphStructure g) {
		super(new ExceptionalUnitGraph(b));
		
		this.method = b.getMethod();
		this.reporter = reporter;
		System.out.println(b);
		Options.v().set_keep_line_number(true);
		if(b.getMethod().getDeclaration().toString().contains("sourceToSink"))
		{
		eg = new ExceptionalUnitGraph(b);
		List<Unit> list = eg.getHeads();
		Iterator<Unit> it1 = list.iterator();
		while(it1.hasNext())
		{
			Unit head = it1.next();
			System.out.println("------Head----of------is-----"+head);
			nodeNumber++;
			nodesMap.put(head,nodeNumber);
			nodes[nodeNumber-1]= new Node(head.toString(), nodeNumber);
			Node node = new Node(head.toString(), nodeNumber);
			listNodes.add(node);
			//System.out.println("++++++++++++++++++++++++++++++++++++++++++"+nodes[0].getLabel());
			traverseUnits(head);
		}
		Set s = edgesMap.entrySet();
		Iterator i = s.iterator();
		while(i.hasNext())
		{
			Map.Entry m = (Map.Entry)i.next();
			System.out.println(m.getKey()+"----->"+m.getValue());
		}
		}
		listEdges1.add(nodesMap);
		listEdges1.add(edgesMap);
		Iterator<Node> nodeIterator = listNodes.iterator();
		while(nodeIterator.hasNext())
		{
			Node n = nodeIterator.next();
			System.out.println("Node id is "+n.getId()+" Node label is "+n.getLabel());
		}
		
		Iterator<Edge> edgeIterator = listEdges.iterator();
		while(edgeIterator.hasNext())
		{
			Edge edge = edgeIterator.next();
			System.out.println("Edge id is "+edge.getId()+" Edge source is "+edge.getSource().id+" Edge destination is "+edge.getDestination().id);
		}
		g.edgesMap=edgesMap;
		g.nodesMap=nodesMap;
}
	public static void traverseUnits(Unit currentNode)
	{
		//int i=0,j=0;
		boolean present=false,edge = false;
		Unit previousNode = null;
		//System.out.println(currentNode);
		List<Unit> l = eg.getSuccsOf(currentNode);
		Iterator<Unit> it = l.iterator();
		while(it.hasNext())
		{
			Unit temp = it.next();
			//System.out.println(temp);
			previousNode = temp;
			Set s = nodesMap.entrySet();
			Iterator i = s.iterator();
			while(i.hasNext())
			{
				//System.out.println("Herreeeeeee");
				Map.Entry m = (Map.Entry)i.next();
//				System.out.println("Unit number is "+m.getValue());
//				System.out.println("Unit key is "+m.getKey());
				if(m.getKey().equals(temp))
				{
					present=true;
				}
				
			}
			if(!present)
			{
			nodeNumber++;
			nodesMap.put(temp, nodeNumber);
			nodes[nodeNumber-1] = new Node(temp.toString(), nodeNumber);
			Node node = new Node(temp.toString(), nodeNumber);
			listNodes.add(node);
			}
//			SourceLnPosTag tag = (SourceLnPosTag)temp.getTag("SourceLnPosTag");
//			System.out.println("Tag value is "+tag);
//			System.out.println("Line number is "+temp.getJavaSourceStartLineNumber());
//			if(tag!=null)
//			{
//				int line = tag.startLn();
//				System.out.println("Line number of unit is "+line);
//			}
			System.out.println("Draw edge from "+ nodesMap.get(currentNode) + " to "+nodesMap.get(previousNode));
			if(present)
			{
				System.out.println(present);
			  for(int k=0; k<nodes.length;k++)
			  {
				  //System.out.println("--->----->----->---->----->---->"+nodes[k].getLabel());
				  if(nodes[k].getLabel().equals(temp.toString()))
				  {
					  edges[edgeCount++]= new Edge(edgeCount, nodes[nodeNumber-2], nodes[k]);
					  System.out.println(nodes[k].getId());
					  Edge edge1 = new Edge(edgeCount, nodes[nodeNumber-2], nodes[k]);
					  listEdges.add(edge1);
					  break;
				  }
			  }
			}
			else
			{
				edges[edgeCount++]= new Edge(edgeCount, nodes[nodeNumber-2], nodes[nodeNumber-1]);
				Edge edge1 = new Edge(edgeCount, nodes[nodeNumber-2], nodes[nodeNumber-1]);
				listEdges.add(edge1);
			}
			//System.out.println("Draw edge from "+ set.get(currentNode) + " to "+set.get(previousNode));
			//edges[edgeCount] = new Edge(++edgeCount, nodes[nodeCount-1], nodes[nodeCount]);
			//set1.put(set.get(currentNode), set.get(previousNode));
			Set s1 = edgesMap.entrySet();
			Iterator i1 = s1.iterator();
			while(i1.hasNext())
			{
				Map.Entry m = (Map.Entry)i1.next();
				if(m.getKey().equals(nodesMap.get(currentNode)))
				{
					edge=true;
				}
				
			}
			if(!edge)
			{
				List<Integer> list = new ArrayList<>();
				list.add(nodesMap.get(previousNode));
				edgesMap.put(nodesMap.get(currentNode), list);
			}
			if(edge)
			{				
				List<Integer> list = edgesMap.get(nodesMap.get(currentNode));
				list.add(nodesMap.get(previousNode));
				edgesMap.put(nodesMap.get(currentNode), list);
			}
			traverseUnits(temp);
		}
		if(previousNode!=null)
		{
		
		}
	}

	@Override
	protected void flowThrough(Set<FlowAbstraction> in, Unit d, Set<FlowAbstraction> out) {
		
		System.out.println(d);
		//System.out.println("Predecesors are "+eg.getPredsOf(d));
		
	}

	@Override
	protected Set<FlowAbstraction> newInitialFlow() {
		return new HashSet<FlowAbstraction>();
	}

	@Override
	protected Set<FlowAbstraction> entryInitialFlow() {
		return new HashSet<FlowAbstraction>();
	}

	@Override
	protected void merge(Set<FlowAbstraction> in1, Set<FlowAbstraction> in2, Set<FlowAbstraction> out) {
		out.addAll(in1);
		out.addAll(in2);
	}

	@Override
	protected void copy(Set<FlowAbstraction> source, Set<FlowAbstraction> dest) {
		dest.clear();
		dest.addAll(source);
	}

	public void doAnalyis() {
		super.doAnalysis();
	}

}
