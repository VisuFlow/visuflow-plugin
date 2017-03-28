package de.unipaderborn.visuflow.ui.graph;

import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;

import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;

/**
 * This class sets the layout of the graph.
 * @author Shashank B S
 *
 */
public class HierarchicalLayout {

	private static double spacingX = 16.0;
	private static double spacingY = 3.0;

	/**
	 * Set layout of the graph.
	 * @param graph
	 */
	static void layout(Graph graph) {
		Iterator<Node> nodeIterator = graph.getNodeIterator();
		Node first = nodeIterator.next();
		Iterator<Node> depthFirstIterator = first.getDepthFirstIterator();

		//Assign the layer to each node
		first.setAttribute("layoutLayer", 0);
		while (depthFirstIterator.hasNext()) {
			Node curr = depthFirstIterator.next();
			int inDegree = curr.getInDegree();
			int layer = 1;
			if (inDegree == 1) {
				Iterable<Edge> edges = curr.getEachEnteringEdge();
				for (Edge edge : edges) {
					layer = edge.getOpposite(curr).getAttribute("layoutLayer");
					layer++;
				}
			}
			if (inDegree > 1) {
				Iterable<Edge> edges = curr.getEachEnteringEdge();
				int parentLayer = layer;
				for (Edge edge : edges) {
					Node parent = edge.getOpposite(curr);
					if (curr.hasAttribute("layoutLayer")) {
						int currLayer = parent.getAttribute("layoutLayer");
						if(currLayer > parentLayer)
							parentLayer = currLayer;
					}
				}
				layer = parentLayer++;
			}
			if(curr.hasAttribute("layoutLayer"))
				curr.removeAttribute("layoutLayer");
			curr.setAttribute("layoutLayer", layer);
		}

		HashMap<Integer, Integer> levelCount = new HashMap<>();
		for (Node node : graph) {
			int layer = node.getAttribute("layoutLayer");
			if (levelCount.containsKey(layer)) {
				int currCount = levelCount.get(layer);
				levelCount.remove(layer);
				levelCount.put(layer, currCount++);
			} else {
				levelCount.put(layer, 1);
			}
		}

		for (Node node : graph) {
			Collection<Edge> leavingEdgeSet = node.getLeavingEdgeSet();
			Edge[] childEdge = new Edge[leavingEdgeSet.size()];
			leavingEdgeSet.toArray(childEdge);
			int directionResolver = childEdge.length/2;
			int even = childEdge.length % 2;

			if (even == 0) {
				for (int i = 0; i < childEdge.length; i++) {
					Node child = childEdge[i].getOpposite(node);
					if (i < directionResolver) {
						child.setAttribute("directionResolver", -1);
					} else {
						child.setAttribute("directionResolver", 1);
					}
				}
			} else {
				for (int i = 0; i < childEdge.length; i++) {
					Node child = childEdge[i].getOpposite(node);
					if (i > directionResolver) {
						child.setAttribute("directionResolver", -1);
					} else if (i < directionResolver) {
						child.setAttribute("directionResolver", 1);
					}
				}
			}
		}

		//Assign the coordinates to each node
		Iterator<Node> breadthFirstIterator = first.getBreadthFirstIterator();
		first.setAttribute("xyz", spacingX, spacingY * graph.getNodeCount(), 0.0);
		while (breadthFirstIterator.hasNext()) {
			Node curr = breadthFirstIterator.next();
			positionNode(curr);
		}

		// reset the "layouted" flag
		breadthFirstIterator = first.getBreadthFirstIterator();
		while (breadthFirstIterator.hasNext()) {
			Node curr = breadthFirstIterator.next();
			curr.removeAttribute("layouted");
		}
	}

	/**
	 * Assign the coordinates to the node in the graph.
	 * @param node
	 */
	private static void positionNode(Node node) {
		Node parent = findParentWithHighestLevel(node);
		if(parent == null)
			return;

		double[] positionOfParent = Toolkit.nodePosition(parent);
		int outDegreeOfParent = parent.getOutDegree();
		if (outDegreeOfParent == 1) {
			node.setAttribute("xyz", positionOfParent[0], positionOfParent[1] - spacingY, 0.0);
			//node.setAttribute("layouted", "true");
		} else {
			if(node.hasAttribute("directionResolver")) {
				double x = positionOfParent[0] + ((int) node.getAttribute("directionResolver") * spacingX);
				double y = positionOfParent[1] - spacingY;
				node.setAttribute("xyz", x, y, 0.0);
				//node.setAttribute("layouted", "true");
			} else {
				node.setAttribute("xyz", positionOfParent[0], positionOfParent[1] - spacingY, 0.0);
				//node.setAttribute("layouted", "true");
			}
		}
	}

	/**
	 * Determine the parent of a node that has the highest level set.
	 * @param node
	 * @return parent node that has the highest level assigned
	 */
	static Node findParentWithHighestLevel(Node node) {
		int inDegreeOfNode = node.getInDegree();
		Node parent = null;

		Iterator<Edge> nodeIterator = node.getEachEnteringEdge().iterator();
		if(inDegreeOfNode == 1)
			parent = nodeIterator.next().getOpposite(node);
		else if (inDegreeOfNode > 1) {
			parent = nodeIterator.next().getOpposite(node);
			while (nodeIterator.hasNext()) {
				Node temp = nodeIterator.next().getOpposite(node);
				if (temp.hasAttribute("layoutLayer") && (int) temp.getAttribute("layoutLayer") > (int) parent.getAttribute("layoutLayer")) {
					parent = temp;
				}
			}

		}

		if(parent != null && !parent.hasAttribute("layouted")) {
			parent.setAttribute("layouted", "true");
			positionNode(parent);
		}
		return parent;
	}
}
