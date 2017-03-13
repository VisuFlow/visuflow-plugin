package de.unipaderborn.visuflow.ui.graph;

import static de.unipaderborn.visuflow.model.DataModel.EA_TOPIC_DATA_FILTER_GRAPH;
import static de.unipaderborn.visuflow.model.DataModel.EA_TOPIC_DATA_MODEL_CHANGED;
import static de.unipaderborn.visuflow.model.DataModel.EA_TOPIC_DATA_SELECTION;
import static de.unipaderborn.visuflow.model.DataModel.EA_TOPIC_DATA_UNIT_CHANGED;
import de.unipaderborn.visuflow.ui.view.filter.ReturnPathFilter;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Container;
import java.awt.Graphics2D;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.Label;
import java.awt.Point;
import java.awt.RenderingHints;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.MouseMotionListener;
import java.awt.event.MouseWheelEvent;
import java.awt.event.MouseWheelListener;
import java.awt.image.BufferedImage;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JColorChooser;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;
import javax.swing.event.PopupMenuEvent;
import javax.swing.event.PopupMenuListener;

import org.apache.commons.lang.StringEscapeUtils;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.graphstream.algorithm.Toolkit;
import org.graphstream.graph.Edge;
import org.graphstream.graph.Graph;
import org.graphstream.graph.Node;
import org.graphstream.graph.implementations.MultiGraph;
import org.graphstream.ui.geom.Point3;
import org.graphstream.ui.graphicGraph.GraphicElement;
import org.graphstream.ui.layout.Layout;
import org.graphstream.ui.layout.springbox.implementations.SpringBox;
import org.graphstream.ui.swingViewer.ViewPanel;
import org.graphstream.ui.view.Viewer;
import org.graphstream.ui.view.ViewerListener;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import com.google.common.base.Optional;

import de.unipaderborn.visuflow.ProjectPreferences;
import de.unipaderborn.visuflow.builder.GlobalSettings;
import de.unipaderborn.visuflow.debug.handlers.NavigationHandler;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFEdge;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFMethodEdge;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.model.graph.ControlFlowGraph;
import de.unipaderborn.visuflow.model.graph.ICFGStructure;
import de.unipaderborn.visuflow.util.ServiceUtil;
import scala.collection.mutable.HashSet;
import soot.jimple.InvokeExpr;
import soot.jimple.ReturnStmt;
import soot.jimple.ReturnVoidStmt;
import soot.jimple.Stmt;

public class GraphManager implements Runnable, ViewerListener, EventHandler {

	// private static final transient Logger logger = Visuflow.getDefault().getLogger();

	Graph graph;
	String styleSheet;
	int maxLength;
	private Viewer viewer;
	private ViewPanel view;
	List<VFClass> analysisData;

	static Node start = null;
	static Node end, previous = null;
	static Map<Node, Node> map = new HashMap<>();
	static HashSet<Node> setOfNode = new HashSet<>();

	Container panel;
	JApplet applet;
	JButton zoomInButton, zoomOutButton, viewCenterButton, toggleLayout, showICFGButton, btColor;
	JToolBar headerBar, settingsBar;
	JTextField searchText;
	JLabel header;

	JDialog dialog;
	JPanel panelColor;
	JColorChooser jcc;

	List<JButton> stmtTypes;

	double zoomInDelta, zoomOutDelta, maxZoomPercent, minZoomPercent, panXDelta, panYDelta;

	boolean autoLayoutEnabled = false;

	Layout graphLayout = new SpringBox();

	private JButton panLeftButton;
	private JButton panRightButton;
	private JButton panUpButton;
	private JButton panDownButton;
	private JPopupMenu popUp;
	private BufferedImage imgLeft;
	private BufferedImage imgRight;
	private BufferedImage imgUp;
	private BufferedImage imgDown;
	private BufferedImage imgPlus;
	private BufferedImage imgMinus;

	private int x = 0;
	private int y = 0;

	private boolean CFG;

	// following 3 variables are used for graph dragging with the mouse
	private boolean draggingGraph = false;
	private Point mouseDraggedFrom;
	private Point mouseDraggedTo;
	private JMenuItem navigateToJimple;
	private JMenuItem navigateToJava;
	private JMenuItem showInUnitView;
	private JMenuItem followCall;
	private JMenuItem followReturn;
	private JMenuItem setCosAttr;
	private JMenu callGraphOption;
	private JMenuItem cha;
	private JMenuItem spark;

	public GraphManager(String graphName, String styleSheet) {
		// System.setProperty("sun.awt.noerasebackground", "true");
		// System.setProperty("org.graphstream.ui.renderer", "org.graphstream.ui.j2dviewer.J2DGraphRenderer");
		this.panXDelta = 2;
		this.panYDelta = 2;
		this.zoomInDelta = .075;
		this.zoomOutDelta = .075;
		this.maxZoomPercent = 0.2;
		this.minZoomPercent = 1.0;
		this.maxLength = 45;
		this.styleSheet = styleSheet;
		createGraph(graphName);
		createUI();

		renderICFG(ServiceUtil.getService(DataModel.class).getIcfg());

		view.setToolTipText("Tooltip Test");
	}

	public Container getApplet() {
		return applet.getRootPane();
	}

	private void registerEventHandler() {
		String[] topics = new String[] { EA_TOPIC_DATA_FILTER_GRAPH, EA_TOPIC_DATA_SELECTION, EA_TOPIC_DATA_MODEL_CHANGED, EA_TOPIC_DATA_UNIT_CHANGED,
				"GraphReady" };
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, topics);
		ServiceUtil.registerService(EventHandler.class, this, properties);
	}

	void createGraph(String graphName) {
		graph = new MultiGraph(graphName);
		graph.addAttribute("ui.stylesheet", styleSheet);
		graph.setStrict(true);
		graph.setAutoCreate(true);
		graph.addAttribute("ui.quality");
		graph.addAttribute("ui.antialias");

		viewer = new Viewer(graph, Viewer.ThreadingModel.GRAPH_IN_GUI_THREAD);
		viewer.setCloseFramePolicy(Viewer.CloseFramePolicy.CLOSE_VIEWER);

		view = viewer.addDefaultView(false);
		view.getCamera().setAutoFitView(true);
	}

	private void reintializeGraph() throws Exception {
		if (graph != null) {
			graph.clear();
			graph.addAttribute("ui.stylesheet", styleSheet);
			graph.setStrict(true);
			graph.setAutoCreate(true);
			graph.addAttribute("ui.quality");
			graph.addAttribute("ui.antialias");
		} else {
			throw new Exception("Graph is null");
		}
	}

	private void createUI() {
		createIcons();
		createZoomControls();
		createShowICFGButton();
		createPanningButtons();
		createPopUpMenu();
		createViewListeners();
		createToggleLayoutButton();
		createSearchText();
		createHeaderBar();
		createSettingsBar();
		createPanel();
		createAppletContainer();
		// colorNode();
	}

	private void panUp() {
		Point3 currCenter = view.getCamera().getViewCenter();
		view.getCamera().setViewCenter(currCenter.x, currCenter.y + panYDelta, 0);
	}

	private void panDown() {
		Point3 currCenter = view.getCamera().getViewCenter();
		view.getCamera().setViewCenter(currCenter.x, currCenter.y - panYDelta, 0);
	}

	private void panLeft() {
		Point3 currCenter = view.getCamera().getViewCenter();
		view.getCamera().setViewCenter(currCenter.x - panXDelta, currCenter.y, 0);
	}

	private void panRight() {
		Point3 currCenter = view.getCamera().getViewCenter();
		view.getCamera().setViewCenter(currCenter.x + panXDelta, currCenter.y, 0);
	}

	private void panToNode(String nodeId) {
		// view.getCamera().resetView();
		Node panToNode = graph.getNode(nodeId);
		double[] pos = Toolkit.nodePosition(panToNode);
		double currPosition = view.getCamera().getViewCenter().y;
		while (pos[1] > currPosition) {
			try {
				Thread.sleep(40);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			view.getCamera().setViewCenter(pos[0], currPosition++, 0.0);
		}
	}

	private void defaultPanZoom() {
		int count = 0;
		if (graph.getNodeCount() > 10)
			count = 10;
		for (int i = 0; i < count; i++) {
			try {
				Thread.sleep(40);
				zoomOut();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
			/*
			 * SwingUtilities.invokeLater(new Runnable() {
			 * 
			 * @Override public void run() { GraphManager.this.zoomIn(); } });
			 */
		}
	}

	private void createPanningButtons() {
		panLeftButton = new JButton("");
		panRightButton = new JButton("");
		panUpButton = new JButton("");
		panDownButton = new JButton("");

		panLeftButton.setToolTipText("shift + arrow key left");
		panRightButton.setToolTipText("shift + arrow key right");
		panUpButton.setToolTipText("shift + arrow key up");
		panDownButton.setToolTipText("shift + arrow key down");

		panLeftButton.setIcon(new ImageIcon(getScaledImage(imgLeft, 20, 20)));
		panRightButton.setIcon(new ImageIcon(getScaledImage(imgRight, 20, 20)));
		panUpButton.setIcon(new ImageIcon(getScaledImage(imgUp, 20, 20)));
		panDownButton.setIcon(new ImageIcon(getScaledImage(imgDown, 20, 20)));

		panLeftButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				panLeft();
			}
		});

		panRightButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				panRight();
			}
		});

		panUpButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				panUp();
			}
		});

		panDownButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				panDown();
			}
		});
	}

	private void createPopUpMenu() {
		navigateToJimple = new JMenuItem("Navigate to Jimple");
		navigateToJava = new JMenuItem("Navigate to Java");
		showInUnitView = new JMenuItem("Highlight on Units view");
		setCosAttr = new JMenuItem("Set costusm attribute");
		followCall = new JMenuItem("Follow the Call");
		followReturn = new JMenuItem("Follow the Return");

		callGraphOption = new JMenu("Call Graph Option");
		cha = new JMenuItem("CHA");
		spark = new JMenuItem("SPARK");

		callGraphOption.add(cha);
		callGraphOption.add(spark);

		followCall.setVisible(false);
		followReturn.setVisible(false);

		navigateToJimple.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GraphicElement curElement = view.findNodeOrSpriteAt(x, y);
				if (curElement == null)
					return;
				Node curr = graph.getNode(curElement.getId());
				Object node = curr.getAttribute("nodeUnit");
				NavigationHandler handler = new NavigationHandler();
				if (node instanceof VFNode) {
					ArrayList<VFUnit> units = new ArrayList<>();
					units.add(((VFNode) node).getVFUnit());
					handler.highlightJimpleSource(units);
				}
			}
		});

		setCosAttr.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				GraphicElement curElement = view.findNodeOrSpriteAt(x, y);
				if (curElement == null) {
					return;
				}
				Node curr = graph.getNode(curElement.getId());
				Object node = curr.getAttribute("nodeUnit");
				if (node instanceof VFNode) {
					VFUnit selectedVF = ((VFNode) node).getVFUnit();
					setCosAttr(selectedVF, curr);
					// curr.setAttribute("ui.color", jcc.getColor());
				}
			}
		});

		navigateToJava.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GraphicElement curElement = view.findNodeOrSpriteAt(x, y);
				if (curElement == null)
					return;
				Node curr = graph.getNode(curElement.getId());
				Object node = curr.getAttribute("nodeUnit");
				NavigationHandler handler = new NavigationHandler();
				if (node instanceof VFNode) {
					ArrayList<VFUnit> units = new ArrayList<>();
					units.add(((VFNode) node).getVFUnit());
					handler.highlightJavaSource(units.get(0));
				}
			}
		});

		showInUnitView.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				DataModel dataModel = ServiceUtil.getService(DataModel.class);
				GraphicElement curElement = view.findNodeOrSpriteAt(x, y);
				if (curElement == null)
					return;
				Node curr = graph.getNode(curElement.getId());
				Object node = curr.getAttribute("nodeUnit");
				if (node instanceof VFNode) {
					ArrayList<VFUnit> units = new ArrayList<>();
					units.add(((VFNode) node).getVFUnit());

					ArrayList<VFNode> nodes = new ArrayList<>();
					nodes.add((VFNode) node);
					try {
						dataModel.filterGraph(nodes, true, true, null);
					} catch (Exception e1) {
						e1.printStackTrace();
					}
				}
			}
		});

		followCall.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GraphicElement curElement = view.findNodeOrSpriteAt(x, y);
				if (curElement == null)
					return;
				Node curr = graph.getNode(curElement.getId());
				Object node = curr.getAttribute("nodeUnit");
				if (node instanceof VFNode) {
					if (((Stmt) ((VFNode) node).getUnit()).containsInvokeExpr()) {
						callInvokeExpr(((Stmt) ((VFNode) node).getUnit()).getInvokeExpr());
					}
				}
			}
		});

		followReturn.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GraphicElement curElement = view.findNodeOrSpriteAt(x, y);
				if (curElement == null)
					return;
				Node curr = graph.getNode(curElement.getId());
				Object node = curr.getAttribute("nodeUnit");
				if (node instanceof VFNode) {
					if (((VFNode) node).getUnit() instanceof ReturnStmt || ((VFNode) node).getUnit() instanceof ReturnVoidStmt) {
						System.out.println("Return ahoy");
						List<VFUnit> list = new ArrayList<>();
						for (VFUnit edge : ((VFNode) node).getVFUnit().getVfMethod().getIncomingEdges()) {
							list.add(edge);
						}
						Display.getDefault().syncExec(new Runnable() {
							public void run() {
								Shell shell = new Shell();
								ReturnPathFilter returnFilter = new ReturnPathFilter(shell);
								returnFilter.setPaths(list);
								returnFilter.setInitialPattern("?");
								returnFilter.open();
								if (returnFilter.getFirstResult() != null) {
									returnToCaller((VFUnit) returnFilter.getFirstResult());
								}
								shell.close();
							}
						});
					}
				}
			}
		});

		cha.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GlobalSettings.put("CallGraphOption", "CHA");
				ServiceUtil.getService(DataModel.class).triggerProjectRebuild();
				header.setText(header.getText() + " ------> CHA");
			}
		});

		spark.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				GlobalSettings.put("CallGraphOption", "SPARK");
				ServiceUtil.getService(DataModel.class).triggerProjectRebuild();
				header.setText(header.getText() + " ------> SPARK");
			}
		});
	}

	private void createIcons() {
		try {
			ClassLoader loader = GraphManager.class.getClassLoader();
			imgLeft = ImageIO.read(loader.getResource("/icons/left.png"));
			imgRight = ImageIO.read(loader.getResource("/icons/right.png"));
			imgUp = ImageIO.read(loader.getResource("/icons/up.png"));
			imgDown = ImageIO.read(loader.getResource("/icons/down.png"));
			imgPlus = ImageIO.read(loader.getResource("/icons/plus.png"));
			imgMinus = ImageIO.read(loader.getResource("/icons/minus.png"));
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	private Image getScaledImage(Image srcImg, int w, int h) {
		BufferedImage resizedImg = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
		Graphics2D g2 = resizedImg.createGraphics();

		g2.setRenderingHint(RenderingHints.KEY_INTERPOLATION, RenderingHints.VALUE_INTERPOLATION_BILINEAR);
		g2.drawImage(srcImg, 0, 0, w, h, null);
		g2.dispose();

		return resizedImg;
	}

	private void createShowICFGButton() {
		showICFGButton = new JButton("Show ICFG");
		showICFGButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				renderICFG(ServiceUtil.getService(DataModel.class).getIcfg());
			}
		});
	}

	private void createSearchText() {
		this.searchText = new JTextField("Search graph");
		searchText.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				String searchString = searchText.getText().toLowerCase();
				ArrayList<VFNode> vfNodes = new ArrayList<>();
				ArrayList<VFUnit> vfUnits = new ArrayList<>();
				for (Node node : graph) {
					if (node.getAttribute("ui.label").toString().toLowerCase().contains((searchString))) {
						vfNodes.add((VFNode) node.getAttribute("nodeUnit"));
						vfUnits.add(((VFNode) node.getAttribute("nodeUnit")).getVFUnit());
					}
				}

				try {
					DataModel model = ServiceUtil.getService(DataModel.class);
					model.filterGraph(vfNodes, true, true, null);
				} catch (Exception e1) {
					e1.printStackTrace();
				}

				NavigationHandler handler = new NavigationHandler();
				handler.highlightJimpleSource(vfUnits);
			}
		});
	}

	private void createAppletContainer() {
		applet = new JApplet();
		applet.add(panel);
	}

	private void createSettingsBar() {
		settingsBar = new JToolBar("ControlsBar", JToolBar.HORIZONTAL);

		settingsBar.add(zoomInButton);
		settingsBar.add(zoomOutButton);
		settingsBar.add(showICFGButton);
		settingsBar.add(viewCenterButton);
		settingsBar.add(toggleLayout);
		settingsBar.add(btColor);
		settingsBar.add(panLeftButton);
		settingsBar.add(panRightButton);
		settingsBar.add(panUpButton);
		settingsBar.add(panDownButton);
		settingsBar.add(searchText);
	}

	private void createHeaderBar()
	{
		this.headerBar = new JToolBar("Header");
		this.headerBar.setFloatable(false);
		this.header = new JLabel("ICFG");
		this.headerBar.add(header);
	}

	private void createPanel() {
		JFrame temp = new JFrame();
		temp.setLayout(new BorderLayout());
		panel = temp.getContentPane();
		panel.add(headerBar,BorderLayout.PAGE_START);
		panel.add(view);
		panel.add(settingsBar, BorderLayout.PAGE_END);
	}

	private void createViewListeners() {

		MouseMotionListener defaultListener = view.getMouseMotionListeners()[0];
		view.removeMouseMotionListener(defaultListener);

		view.addMouseWheelListener(new MouseWheelListener() {

			@Override
			public void mouseWheelMoved(MouseWheelEvent e) {
				int rotationDirection = e.getWheelRotation();
				if (rotationDirection > 0)
					zoomIn();
				else
					zoomOut();
			}
		});

		view.addMouseMotionListener(new MouseMotionListener() {
			@Override
			public void mouseMoved(MouseEvent event) {

				GraphicElement curElement = view.findNodeOrSpriteAt(event.getX(), event.getY());

				if (curElement == null) {
					view.setToolTipText(null);
				}

				if (curElement != null) {
					Node node = graph.getNode(curElement.getId());
					String result = "<html><table>";
					int maxToolTipLength = 0;
					for (String key : node.getEachAttributeKey()) {
						if (key.startsWith("nodeData")) {
							Object value = node.getAttribute(key);
							String tempVal = key.substring(key.lastIndexOf(".") + 1) + " : " + value.toString();
							if (tempVal.length() > maxToolTipLength) {
								maxToolTipLength = tempVal.length();
							}

							result += "<tr><td>" + key.substring(key.lastIndexOf(".") + 1) + "</td>" + "<td>" + value.toString() + "</td></tr>";
						}
					}
					result += "</table></html>";
					view.setToolTipText(result);
				}
			}

			@Override
			public void mouseDragged(MouseEvent e) {
				if (draggingGraph) {
					dragGraph(e);
				} else {
					defaultListener.mouseDragged(e);
				}
			}

			private void dragGraph(MouseEvent e) {
				if (mouseDraggedFrom == null) {
					mouseDraggedFrom = e.getPoint();
				} else {
					if (mouseDraggedTo != null) {
						mouseDraggedFrom = mouseDraggedTo;
					}
					mouseDraggedTo = e.getPoint();

					Point3 from = view.getCamera().transformPxToGu(mouseDraggedFrom.x, mouseDraggedFrom.y);
					Point3 to = view.getCamera().transformPxToGu(mouseDraggedTo.x, mouseDraggedTo.y);

					double deltaX = from.x - to.x;
					double deltaY = from.y - to.y;
					double deltaZ = from.z - to.z;

					Point3 viewCenter = view.getCamera().getViewCenter();
					view.getCamera().setViewCenter(viewCenter.x + deltaX, viewCenter.y + deltaY, viewCenter.z + deltaZ);
				}
			}
		});

		view.addMouseListener(new MouseListener() {

			@Override
			public void mouseReleased(MouseEvent e) {
				draggingGraph = false;
			}

			@Override
			public void mousePressed(MouseEvent e) {
				if (e.getButton() == MouseEvent.BUTTON3) {
					// reset mouse drag tracking
					mouseDraggedFrom = null;
					mouseDraggedTo = null;
					draggingGraph = true;
				}
			}

			@Override
			public void mouseExited(MouseEvent e) {
				// noop
			}

			@Override
			public void mouseEntered(MouseEvent e) {
				// noop
			}

			@Override
			public void mouseClicked(MouseEvent e) {
				DataModel dataModel = ServiceUtil.getService(DataModel.class);
				GraphicElement curElement = view.findNodeOrSpriteAt(e.getX(), e.getY());
				if (e.getButton() == MouseEvent.BUTTON3 && !CFG) {
					x = e.getX();
					y = e.getY();
					popUp = new JPopupMenu("right click menu");
					popUp.add(callGraphOption);
					popUp.show(e.getComponent(), x, y);
				}
				if (e.getButton() == MouseEvent.BUTTON3 && CFG) {
					x = e.getX();
					y = e.getY();

					if (curElement != null) {
						popUp = new JPopupMenu("right click menu");

						popUp.add(navigateToJimple);
						popUp.add(navigateToJava);
						popUp.add(showInUnitView);
						popUp.add(setCosAttr);
						popUp.add(followCall);
						popUp.add(followReturn);
						popUp.addPopupMenuListener(new PopupMenuListener() {

							@Override
							public void popupMenuWillBecomeVisible(PopupMenuEvent arg0) {
								GraphicElement curElement = view.findNodeOrSpriteAt(x, y);
								if (curElement == null) {
									return;
								}
								Node curr = graph.getNode(curElement.getId());
								Object node = curr.getAttribute("nodeUnit");
								if (node instanceof VFNode) {
									if (((Stmt) ((VFNode) node).getUnit()).containsInvokeExpr()) {
										followCall.setVisible(true);
										followReturn.setVisible(false);
									} else if (((VFNode) node).getUnit() instanceof ReturnStmt || ((VFNode) node).getUnit() instanceof ReturnVoidStmt) {
										followCall.setVisible(false);
										followReturn.setVisible(true);
									} else {
										followCall.setVisible(false);
										followReturn.setVisible(false);
									}
								}
							}

							@Override
							public void popupMenuCanceled(PopupMenuEvent arg0) {
								followCall.setVisible(false);
								followReturn.setVisible(false);
							}

							@Override
							public void popupMenuWillBecomeInvisible(PopupMenuEvent arg0) {
								followCall.setVisible(false);
								followReturn.setVisible(false);
							}

						});

						popUp.show(e.getComponent(), x, y);
					}
				}
				if (curElement == null)
					return;
				Node curr = graph.getNode(curElement.getId());
				if (e.getButton() == MouseEvent.BUTTON1 && !CFG && !((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
					Object node = curr.getAttribute("nodeMethod");
					if (node instanceof VFMethod) {
						VFMethod selectedMethod = (VFMethod) node;
						try {
							if (selectedMethod.getControlFlowGraph() == null)
								throw new Exception("CFG Null Exception");
							else {
								dataModel.setSelectedMethod(selectedMethod, true);
							}
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} else if (e.getButton() == MouseEvent.BUTTON1 && CFG && !((e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK)) {
					Object node = curr.getAttribute("nodeUnit");
					NavigationHandler handler = new NavigationHandler();
					if (node instanceof VFNode) {
						ArrayList<VFUnit> units = new ArrayList<>();
						units.add(((VFNode) node).getVFUnit());
						handler.highlightJimpleSource(units);
						handler.highlightJavaSource(units.get(0));

						ArrayList<VFNode> nodes = new ArrayList<>();
						nodes.add((VFNode) node);
						try {
							dataModel.filterGraph(nodes, true, true, null);
						} catch (Exception e1) {
							e1.printStackTrace();
						}
					}
				} else if (e.getButton() == MouseEvent.BUTTON1 && (e.getModifiers() & ActionEvent.CTRL_MASK) == ActionEvent.CTRL_MASK) {
					String id = curr.getId();
					if (id != null)
						toggleNode(id);
				}
				if (e.getButton() == MouseEvent.BUTTON3 && CFG) {
					x = e.getX();
					y = e.getY();
					if (curElement != null)
						popUp.show(e.getComponent(), x, y);
				}
			}
		});

		view.addKeyListener(new KeyListener() {

			@Override
			public void keyTyped(KeyEvent e) {

			}

			@Override
			public void keyReleased(KeyEvent e) {

			}

			@Override
			public void keyPressed(KeyEvent e) {
				int keyCode = e.getKeyCode();
				if (e.isShiftDown()) {
					switch (keyCode) {
					case KeyEvent.VK_UP:
						// handle up
						panUp();
						break;
					case KeyEvent.VK_DOWN:
						// handle down
						panDown();
						break;
					case KeyEvent.VK_LEFT:
						// handle left
						panLeft();
						break;
					case KeyEvent.VK_RIGHT:
						// handle right
						panRight();
						break;
					}
				}
			}
		});
	}

	private void returnToCaller(VFUnit unit) {
		if (unit == null)
			return;
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		try {
			if (unit.getVfMethod().getControlFlowGraph() == null)
				throw new Exception("CFG Null Exception");
			else {
				dataModel.setSelectedMethod(unit.getVfMethod(), true);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void callInvokeExpr(InvokeExpr expr) {
		if (expr == null)
			return;
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		System.out.println(expr);
		VFMethod selectedMethod = dataModel.getVFMethodByName(expr.getMethod());
		try {
			if (selectedMethod.getControlFlowGraph() == null)
				throw new Exception("CFG Null Exception");
			else {
				dataModel.setSelectedMethod(selectedMethod, true);
			}
		} catch (Exception e1) {
			e1.printStackTrace();
		}
	}

	private void zoomOut() {
		double viewPercent = view.getCamera().getViewPercent();
		if (viewPercent > maxZoomPercent)
			view.getCamera().setViewPercent(viewPercent - zoomInDelta);
	}

	private void zoomIn() {
		double viewPercent = view.getCamera().getViewPercent();
		if (viewPercent < minZoomPercent)
			view.getCamera().setViewPercent(viewPercent + zoomOutDelta);
	}

	private void createZoomControls() {
		zoomInButton = new JButton();
		zoomOutButton = new JButton();
		viewCenterButton = new JButton("reset");

		zoomInButton.setIcon(new ImageIcon(getScaledImage(imgPlus, 20, 20)));
		zoomOutButton.setIcon(new ImageIcon(getScaledImage(imgMinus, 20, 20)));

		zoomInButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				zoomIn();
			}
		});

		zoomOutButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				zoomOut();
			}
		});

		viewCenterButton.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				view.getCamera().resetView();
			}
		});

		colorNode();
	}

	private void createToggleLayoutButton() {
		toggleLayout = new JButton();
		toggleAutoLayout();
		toggleLayout.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				toggleAutoLayout();
			}
		});
	}

	private void toggleAutoLayout() {
		if (!autoLayoutEnabled) {
			if (viewer != null && graphLayout != null) {
				// viewer.enableAutoLayout(graphLayout);
				experimentalLayout();
			} else if (viewer != null) {
				// viewer.enableAutoLayout();
				experimentalLayout();
			}
			autoLayoutEnabled = true;
			toggleLayout.setText("Disable Layouting");
		} else {
			viewer.disableAutoLayout();
			autoLayoutEnabled = false;
			toggleLayout.setText("Enable Layouting");
		}
	}

	private void colorNode() {
		jcc = new JColorChooser(Color.RED);
		jcc.getSelectionModel().addChangeListener(new ChangeListener() {

			@Override
			public void stateChanged(ChangeEvent e) {
				Color color = jcc.getColor();
				System.out.println("color:" + color);

			}
		});

		dialog = JColorChooser.createDialog(null, "Color Chooser", true, jcc, null, null);
		panelColor = new JPanel(new GridLayout(0, 2));

		btColor = new JButton("Color nodes");
		btColor.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				colorTheGraph();

			}
		});
	}

	private void filterGraphNodes(List<VFNode> nodes, boolean selected, boolean panToNode, String uiClassName) {
		boolean panned = false;
		if (uiClassName == null) {
			uiClassName = "filter";
		}
		Iterable<? extends Node> graphNodes = graph.getEachNode();
		for (Node node : graphNodes) {
			if (node.hasAttribute("ui.class")) {
				node.removeAttribute("ui.class");
			}
			for (VFNode vfNode : nodes) {
				if (node.getAttribute("unit").toString().contentEquals(vfNode.getUnit().toString())) {
					if (selected) {
						node.setAttribute("ui.class", uiClassName);
					}
					if (!panned && panToNode) {
						this.panToNode(node.getId());
						panned = true;
					}
				}
			}
		}
	}

	private void renderICFG(ICFGStructure icfg) {
		if (icfg == null) {
			return;
		}

		Iterator<VFMethodEdge> iterator = icfg.listEdges.iterator();
		try {
			reintializeGraph();
		} catch (Exception e) {
			e.printStackTrace();
		}
		while (iterator.hasNext()) {
			VFMethodEdge curr = iterator.next();

			VFMethod src = curr.getSourceMethod();
			VFMethod dest = curr.getDestMethod();

			createGraphMethodNode(src);
			createGraphMethodNode(dest);
			createGraphMethodEdge(src, dest);
		}
		this.CFG = false;
		this.header.setText("ICFG");
		experimentalLayout();
	}

	private void createGraphMethodEdge(VFMethod src, VFMethod dest) {
		if (graph.getEdge("" + src.getId() + dest.getId()) == null) {
			graph.addEdge(src.getId() + "" + dest.getId(), src.getId() + "", dest.getId() + "", true);
		}
	}

	private void createGraphMethodNode(VFMethod src) {
		if (graph.getNode(src.getId() + "") == null) {
			Node createdNode = graph.addNode(src.getId() + "");
			String methodName = src.getSootMethod().getName();
			String escapedMethodName = StringEscapeUtils.escapeHtml(methodName);
			String escapedMethodSignature = StringEscapeUtils.escapeHtml(src.getSootMethod().getSignature());
			createdNode.setAttribute("ui.label", methodName);
			createdNode.setAttribute("nodeData.methodName", escapedMethodName);
			createdNode.setAttribute("nodeData.methodSignature", escapedMethodSignature);
			createdNode.setAttribute("nodeData.unescapedMethodName", methodName);
			createdNode.setAttribute("nodeMethod", src);
		}
	}

	private void renderMethodCFG(ControlFlowGraph interGraph, boolean panToNode) throws Exception {
		if (interGraph == null)
			throw new Exception("GraphStructure is null");

		this.reintializeGraph();
		ListIterator<VFEdge> edgeIterator = interGraph.listEdges.listIterator();

		while (edgeIterator.hasNext()) {
			VFEdge currEdgeIterator = edgeIterator.next();

			VFNode src = currEdgeIterator.getSource();
			VFNode dest = currEdgeIterator.getDestination();

			createGraphNode(src);
			createGraphNode(dest);
			createGraphEdge(src, dest);
		}
		if(interGraph.listEdges.size() == 1)
		{
			VFNode node = interGraph.listNodes.get(0);
			createGraphNode(node);
		}
		this.CFG = true;
		experimentalLayout();

		if (panToNode) {
			defaultPanZoom();
			panToNode(graph.getNodeIterator().next().getId());
		}
		this.header.setText("Method CFG ----> " + ServiceUtil.getService(DataModel.class).getSelectedMethod().toString());
	}

	private void createGraphEdge(VFNode src, VFNode dest) {
		if (graph.getEdge("" + src.getId() + dest.getId()) == null) {
			Edge createdEdge = graph.addEdge(src.getId() + "" + dest.getId(), src.getId() + "", dest.getId() + "", true);
			VFUnit unit = src.getVFUnit();
			createdEdge.addAttribute("ui.label", Optional.fromNullable(unit.getOutSet()).or("").toString());
			createdEdge.addAttribute("edgeData.outSet", Optional.fromNullable(unit.getInSet()).or("").toString());
		}
	}

	private void createGraphNode(VFNode node) {
		if (graph.getNode(node.getId() + "") == null) {
			Node createdNode = graph.addNode(node.getId() + "");
			if (node.getUnit().toString().length() > maxLength) {
				createdNode.setAttribute("ui.label", node.getUnit().toString().substring(0, maxLength) + "...");
			} else {
				createdNode.setAttribute("ui.label", node.getUnit().toString());
			}
			String str = node.getUnit().toString();
			String escapedNodename = StringEscapeUtils.escapeHtml(str);
			createdNode.setAttribute("unit", str);
			createdNode.setAttribute("nodeData.unit", escapedNodename);

			createdNode.setAttribute("nodeData.unitType", node.getUnit().getClass());
			String str1 = Optional.fromNullable(node.getVFUnit().getInSet()).or("n/a").toString();
			String nodeInSet = StringEscapeUtils.escapeHtml(str1);
			String str2 = Optional.fromNullable(node.getVFUnit().getInSet()).or("n/a").toString();
			String nodeOutSet = StringEscapeUtils.escapeHtml(str2);

			createdNode.setAttribute("nodeData.inSet", nodeInSet);
			createdNode.setAttribute("nodeData.outSet", nodeOutSet);
			createdNode.setAttribute("nodeData.line", node.getUnit().getJavaSourceStartLineNumber());
			// createdNode.setAttribute("nodeData.column", node.getUnit().getJavaSourceStartColumnNumber());
			createdNode.setAttribute("nodeUnit", node);
		}
	}

	@SuppressWarnings("unused")
	private void getNodesToCollapse(Node n)
	{
		boolean present = false;
		scala.collection.Iterator<Node> setIterator = setOfNode.iterator();
		while (setIterator.hasNext()) {
			if (setIterator.next().equals(n)) {
				present = true;
				break;
			}
		}
		if (!present) {
			Iterator<Edge> edgeIterator = n.getLeavingEdgeIterator();
			while (edgeIterator.hasNext()) {
				Edge edge = edgeIterator.next();
				start = edge.getNode1();
				Node k = start;
				while (true) {
					if (k.getOutDegree() == 1 && k.getInDegree() == 1) {
						previous = k;
						k = k.getEachLeavingEdge().iterator().next().getNode1();
					} else {
						break;
					}
				}

				map.put(start, previous);
				setOfNode.add(n);
				getNodesToCollapse(k);

			}
		}
	}

	/*private void experimentalLayoutOld()
		if (!CFG) {
			viewer.enableAutoLayout(new SpringBox());
			view.getCamera().resetView();
			return;
		}
		viewer.disableAutoLayout();

		double rowSpacing = 3.0;
		double columnSpacing = 3.0;
		Iterator<Node> nodeIterator = graph.getNodeIterator();
		int totalNodeCount = graph.getNodeCount();
		int currNodeIndex = 0;
		while (nodeIterator.hasNext()) {
			Node curr = nodeIterator.next();
			if (curr.hasAttribute("layout.visited")) {
				continue;
			}
			int currEdgeCount = curr.getOutDegree();
			int currEdgeIndex = 0;
			if (currEdgeCount > 1) {
				Iterator<Edge> currEdgeIterator = curr.getEdgeIterator();
				curr.setAttribute("xyz", 0.0, ((totalNodeCount * rowSpacing) - currNodeIndex), 0.0);
				curr.setAttribute("layout.visited");
				currNodeIndex++;
				while (currEdgeIterator.hasNext()) {
					Node temp = currEdgeIterator.next().getOpposite(curr);
					temp.setAttribute("xyz", ((columnSpacing - currEdgeIndex) * currEdgeCount), ((totalNodeCount * columnSpacing) - currNodeIndex), 0.0);
					curr.setAttribute("layout.visited");
					currEdgeIndex++;
				}
				currNodeIndex++;
			}
			curr.setAttribute("xyz", 0.0, ((totalNodeCount * rowSpacing) - currNodeIndex), 0.0);
			curr.setAttribute("layout.visited");
			currNodeIndex++;
		}

		for (Node node : graph) {
			double[] pos = Toolkit.nodePosition(graph, node.getId());
			int inDegree = node.getInDegree();
			int outDegree = node.getOutDegree();
			if (inDegree == 0)
				continue;
			if (inDegree > outDegree) {
				node.setAttribute("xyz", pos[0] - rowSpacing, pos[1], 0.0);
			}
			if (outDegree > inDegree) {
				node.setAttribute("xyz", pos[0] + rowSpacing, pos[1], 0.0);
			}
			if (inDegree > 1 && outDegree > 1 && inDegree == outDegree) {
				node.setAttribute("xyz", pos[0] - rowSpacing, pos[1], 0.0);
			} else
				continue;
		}

		view.getCamera().resetView();
	}*/

	private void experimentalLayout() {
		if (!CFG) {
			viewer.enableAutoLayout(new SpringBox());
			view.getCamera().resetView();
			return;
		}
		viewer.disableAutoLayout();

		Iterator<Node> nodeIterator = graph.getNodeIterator();
		Node first = nodeIterator.next();
		Iterator<Node> depthFirstIterator = first.getDepthFirstIterator();

		//Assign the layer to each node
		first.setAttribute("layoutLayer", 0);
		while(depthFirstIterator.hasNext())
		{
			Node curr = depthFirstIterator.next();
			int inDegree = curr.getInDegree();
			int layer = 1;
			if(inDegree == 1)
			{
				Iterable<Edge> edges = curr.getEachEnteringEdge();
				for (Edge edge : edges) {
					layer = edge.getOpposite(curr).getAttribute("layoutLayer");
					layer++;
				}
			}
			if(inDegree > 1)
			{
				Iterable<Edge> edges = curr.getEachEnteringEdge();
				int parentLayer = layer;
				for (Edge edge : edges) {
					Node parent = edge.getOpposite(curr);
					if(curr.hasAttribute("layoutLayer"))
					{
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
		
		HashMap<Integer, Integer> levelCount = new HashMap<Integer, Integer>();
		for (Node node : graph) {
			int layer = node.getAttribute("layoutLayer");
			if(levelCount.containsKey(layer))
			{
				int currCount = levelCount.get(layer);
				levelCount.remove(layer);
				levelCount.put(layer, currCount++);
			}
			else
			{
				levelCount.put(layer, 1);
			}
		}
		
		for (Node node : graph) {
			Collection<Edge> leavingEdgeSet = node.getLeavingEdgeSet();
			Edge[] childEdge = new Edge[leavingEdgeSet.size()];
			leavingEdgeSet.toArray(childEdge);
			int directionResolver = childEdge.length/2;
			int even = childEdge.length % 2;
			
			if(even == 0)
			{
				for(int i = 0; i < childEdge.length; i++)
				{
					Node child = childEdge[i].getOpposite(node);
					if(i < directionResolver)
					{
						child.setAttribute("directionResolver", -1);
						child.setAttribute("nodeData.direction", -1);
					}
					else
					{
						child.setAttribute("directionResolver", 1);
						child.setAttribute("nodeData.direction", 1);
					}
				}
			}
			else
			{
				for(int i = 0; i < childEdge.length; i++)
				{
					Node child = childEdge[i].getOpposite(node);
					if(i > directionResolver)
					{
						child.setAttribute("directionResolver", -1);
						child.setAttribute("nodeData.direction", -1);
					}
					else if(i < directionResolver)
					{
						child.setAttribute("directionResolver", 1);
						child.setAttribute("nodeData.direction", 1);
					}
				}
			}
		}
		
		//Assign the coordinates to each node
		double spacingX = 16.0;
		double spacingY = 3.0;
//		Iterator<Node> breadthFirstIterator = first.getBreadthFirstIterator();
		depthFirstIterator = first.getDepthFirstIterator();
		first.setAttribute("xyz", spacingX, spacingY * graph.getNodeCount(), 0.0);
		while(depthFirstIterator.hasNext())
		{
			Node curr = depthFirstIterator.next();
			Node parent = findParentWithHighestLevel(curr);
			
			if(parent == null)
				continue;
			double[] positionOfParent = Toolkit.nodePosition(parent);
			int outDegreeOfParent = parent.getOutDegree();
			
			curr.setAttribute("nodeData.parent", (String) parent.getAttribute("ui.label"));
			curr.setAttribute("nodeData.layerOfParent", (int) parent.getAttribute("layoutLayer"));
			
			if(outDegreeOfParent == 1)
			{
				curr.setAttribute("xyz", positionOfParent[0], positionOfParent[1] - spacingY, 0.0);
			}
			else
			{
				if(curr.hasAttribute("directionResolver"))
					curr.setAttribute("xyz", positionOfParent[0] + ((int) curr.getAttribute("directionResolver") * spacingX), positionOfParent[1] - spacingY, 0.0);
				else
					curr.setAttribute("xyz", positionOfParent[0], positionOfParent[1] - spacingY, 0.0);
			}
		}
	}
	
	Node findParentWithHighestLevel(Node node)
	{
		int inDegreeOfNode = node.getInDegree();
		Node parent = null;
		
		Iterator<Edge> nodeIterator = node.getEachEnteringEdge().iterator();
		if(inDegreeOfNode == 1)
			parent = nodeIterator.next().getOpposite(node);
		else if(inDegreeOfNode > 1)
		{
			parent = nodeIterator.next().getOpposite(node);
			while(nodeIterator.hasNext())
			{
				Node temp = nodeIterator.next().getOpposite(node);
				if(temp.hasAttribute("layoutLayer") && (int) temp.getAttribute("layoutLayer") > (int) parent.getAttribute("layoutLayer"))
				{
					parent = temp;
				}
			}
		}
		return parent;
	}

	void toggleNode(String id) {
		Node n = graph.getNode(id);
		Object[] pos = n.getAttribute("xyz");
		Iterator<Node> it = n.getBreadthFirstIterator(true);
		if (n.hasAttribute("collapsed")) {
			n.removeAttribute("collapsed");
			while (it.hasNext()) {
				Node m = it.next();

				for (Edge e : m.getLeavingEdgeSet()) {
					e.removeAttribute("ui.hide");
				}
				m.removeAttribute("layout.frozen");
				m.setAttribute("x", ((double) pos[0]) + Math.random() * 0.0001);
				m.setAttribute("y", ((double) pos[1]) + Math.random() * 0.0001);

				m.removeAttribute("ui.hide");

			}
			n.removeAttribute("ui.class");

		} else {
			n.setAttribute("ui.class", "plus");
			n.setAttribute("collapsed");

			while (it.hasNext()) {
				Node m = it.next();

				for (Edge e : m.getLeavingEdgeSet()) {
					e.setAttribute("ui.hide");
				}
				if (n != m) {
					m.setAttribute("layout.frozen");
					// m.setAttribute("x", ((double) pos[0]) + Math.random() * 0.0001);
					// m.setAttribute("y", ((double) pos[1]) + Math.random() * 0.0001);

					m.setAttribute("xyz", ((double) pos[0]) + Math.random() * 0.0001, ((double) pos[1]) + Math.random() * 0.0001, 0.0);

					m.setAttribute("ui.hide");
				}

			}
		}
		experimentalLayout();
		panToNode(id);
	}

	@Override
	public void run() {
		this.registerEventHandler();
		System.out.println("GraphManager ---> registered for events");

		// No need to have the following code.

		/*
		 * ViewerPipe fromViewer = viewer.newViewerPipe(); fromViewer.addViewerListener(this); fromViewer.addSink(graph);
		 * 
		 * // FIXME the Thread.sleep slows down the loop, so that it does not eat up the CPU // but this really should be implemented differently. isn't there
		 * an event listener // or something we can use, so that we call pump() only when necessary while(true) { try { Thread.sleep(1); } catch
		 * (InterruptedException e) { } fromViewer.pump(); }
		 */
	}

	@Override
	public void buttonPushed(String id) {
		// noop
	}

	@Override
	public void buttonReleased(String id) {
		toggleNode(id);
		experimentalLayout();
	}

	@Override
	public void viewClosed(String id) {
		// noop
	}

	@SuppressWarnings("unchecked")
	@Override
	public void handleEvent(Event event) {
		if (event.getTopic().contentEquals(DataModel.EA_TOPIC_DATA_MODEL_CHANGED)) {
			renderICFG((ICFGStructure) event.getProperty("icfg"));
		}
		if (event.getTopic().contentEquals(DataModel.EA_TOPIC_DATA_SELECTION)) {
			VFMethod selectedMethod = (VFMethod) event.getProperty("selectedMethod");
			boolean panToNode = (boolean) event.getProperty("panToNode");
			try {
				renderMethodCFG(selectedMethod.getControlFlowGraph(), panToNode);
			} catch (Exception e) {
				e.printStackTrace();
			}
		}
		if (event.getTopic().contentEquals(DataModel.EA_TOPIC_DATA_FILTER_GRAPH)) {
			filterGraphNodes((List<VFNode>) event.getProperty("nodesToFilter"), (boolean) event.getProperty("selection"), (boolean) event.getProperty("panToNode"),
					(String) event.getProperty("uiClassName"));
		}
		if (event.getTopic().equals(DataModel.EA_TOPIC_DATA_UNIT_CHANGED)) {
			VFUnit unit = (VFUnit) event.getProperty("unit");

			for (Edge edge : graph.getEdgeSet()) {
				Node src = edge.getSourceNode();
				VFNode vfNode = src.getAttribute("nodeUnit");
				if (vfNode != null) {
					VFUnit currentUnit = vfNode.getVFUnit();
					if (unit.getFullyQualifiedName().equals(currentUnit.getFullyQualifiedName())) {
						String outset = Optional.fromNullable(unit.getOutSet()).or("").toString();
						edge.setAttribute("ui.label", outset);
						edge.setAttribute("edgeData.outSet", outset);
						src.addAttribute("nodeData.inSet", unit.getInSet());
						src.addAttribute("nodeData.outSet", unit.getOutSet());
					}
				}
			}
		}
	}

	private boolean inPanel(String btName, JPanel panel) {
		boolean exist = false;

		for (Component c : panel.getComponents()) {

			if (!(c.getName() == null)) {
				if ((c.getClass().toString().equals("class javax.swing.JButton"))) {
					JButton bt = (JButton) c;
					System.out.println("Button's name = " + c.getName());
					if (bt.getName().equals(btName)) {
						System.out.println("btName = " + btName + " , component name = " + bt.getName());
						exist = true;
						break;
					}
				}
			}
		}

		return exist;
	}

	public void colorTheGraph() {

		panelColor.removeAll();

		boolean inICFG = this.CFG;
		if (!inICFG) {

			for (JButton bt : createStmtTypes(stmtTypes)) {
				panelColor.add(new Label("Set color preference for this kind of statement"));
				panelColor.add(bt);
				bt.addActionListener(new ActionListener() {

					@Override
					public void actionPerformed(ActionEvent e) {
						dialog.setVisible(true);

						// TODO Send vfNode and Color to Kaarthik
								ProjectPreferences pref = new ProjectPreferences();
								pref.updateColorPreferences(vfNode.getUnit().getClass().getName(),jcc.getColor().getRGB());
						
						System.out.println("Statement is : " + bt.getName());
						System.out.println("Color is : " + jcc.getColor().getRGB());

					}
				});
			}

		} else {
			for (Node n : graph.getEachNode()) {
				if (!(n.getAttribute("nodeData.unescapedMethodName") == null)) {
					JLabel l = new JLabel("Change the color of this unit");
					JButton bt = new JButton(n.getAttribute("nodeData.unescapedMethodName").toString());
					bt.setName(n.getAttribute("nodeData.unescapedMethodName").toString());

					bt.addActionListener(new ActionListener() {

						@Override
						public void actionPerformed(ActionEvent e) {
							dialog.setVisible(true);
							System.out.println("Button's name is : " + bt.getName());
							System.out.println("Button's name is...");

							for (Node n : graph.getEachNode()) {
								if (n.getAttribute("nodeData.unescapedMethodName").toString().equals(bt.getName())) {
									n.setAttribute("ui.color", jcc.getColor());
									System.out.println(jcc.getColor());
								}

							}
						}

					});

					if (!(inPanel(bt.getName(), panelColor))) {
						panelColor.add(l);
						panelColor.add(bt);
					}

				}

			}

		}
		// for (Node n : graph.getEachNode()) {
		// if (!(n.getAttribute("nodeData.unitType") == null)) {
		//
		//
		//
		//
		//
		//
		// // JLabel l = new JLabel("Change the color of this unit");
		// // JButton bt = new JButton(n.getAttribute("nodeData.unitType").toString());
		// // // bt.setName(n.getAttribute("nodeData.methodName").toString());
		// // bt.setName(n.getAttribute("nodeData.unitType").toString());
		// //
		// // bt.addActionListener(new ActionListener() {
		// //
		// // @Override
		// // public void actionPerformed(ActionEvent e) {
		// // dialog.setVisible(true);
		// // System.out.println("Button's name is : " + bt.getName());
		// // System.out.println("Button's name is..." );
		// // for(Node n: graph.getEachNode()){
		// // // if(n.getAttribute("nodeData.methodName").toString().equals(bt.getName())){
		// // // if(n.getAttribute("ui.label").toString().equals(bt.getName())){
		// // if(n.getAttribute("nodeData.unitType").toString().equals(bt.getName())){
		// // n.setAttribute("ui.color", jcc.getColor());
		// // // n.setAttribute("ui.text-size", "20%");
		// // System.out.println(jcc.getColor());
		// // }
		// //
		// // }
		// //
		// //
		// // }
		// // });
		// //
		// // if(!(inPanel(bt.getName(), panelColor))){
		// // panelColor.add(l);
		// // panelColor.add(bt);
		// // }
		//
		// }
		//
		// if (!(n.getAttribute("nodeData.unescapedMethodName") == null)) {
		// JLabel l = new JLabel("Change the color of this unit");
		// JButton bt = new JButton(n.getAttribute("nodeData.unescapedMethodName").toString());
		// bt.setName(n.getAttribute("nodeData.unescapedMethodName").toString());
		// bt.addActionListener(new ActionListener() {
		//
		// @Override
		// public void actionPerformed(ActionEvent e) {
		// dialog.setVisible(true);
		// System.out.println("Button's name is : " + bt.getName());
		// System.out.println("Button's name is...");
		// for (Node n : graph.getEachNode()) {
		// // if(n.getAttribute("nodeData.methodName").toString().equals(bt.getName())){
		// // if(n.getAttribute("ui.label").toString().equals(bt.getName())){
		// if (n.getAttribute("nodeData.unescapedMethodName").toString().equals(bt.getName())) {
		// n.setAttribute("ui.color", jcc.getColor());
		// System.out.println(jcc.getColor());
		// }
		//
		// }
		//
		// }
		// });
		//
		// if (!(inPanel(bt.getName(), panelColor))) {
		// panelColor.add(l);
		// panelColor.add(bt);
		// }
		//
		// }
		//
		// }

		int result = JOptionPane.showConfirmDialog(null, panelColor, "Test", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			System.out.println("Color in JOptionPane"+ jcc.getColor().getRGB());

		} else {
			System.out.println("Cancelled");
		}
	}

	@SuppressWarnings("rawtypes")
	public void setCosAttr(VFUnit selectedVF, Node curr) {
		JPanel panel = new JPanel(new GridLayout(0, 2));

		JTextField tfAnalysis = new JTextField("");
		JTextField tfAttr = new JTextField("");

		panel.add(new JLabel("Analysis: "));
		panel.add(tfAnalysis);
		panel.add(new JLabel("Attribute: "));
		panel.add(tfAttr);

		int result = JOptionPane.showConfirmDialog(null, panel, "Setting costum attribute", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE);
		if (result == JOptionPane.OK_OPTION) {
			Map<String, String> hmCustAttr = new HashMap<>();

			// Get actual customized attributes
			Set set = selectedVF.getHmCustAttr().entrySet();
			Iterator i = set.iterator();

			// Display elements
			while (i.hasNext()) {
				Map.Entry me = (Map.Entry) i.next();
				hmCustAttr.put((String) me.getKey(), (String) me.getValue());
			}

			if ((tfAnalysis.getText().length() > 0) && (tfAttr.getText().length() > 0)) {
				try {
					hmCustAttr.put(tfAnalysis.getText(), tfAttr.getText());
					selectedVF.setHmCustAttr(hmCustAttr);

					ArrayList<VFUnit> units = new ArrayList<>();
					units.add(selectedVF);
					curr.setAttribute("ui.color", jcc.getColor());

				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
			} else {
				JOptionPane.showMessageDialog(new JPanel(), "Please make sure all fields are correctly filled out", "Warning", JOptionPane.WARNING_MESSAGE);
				System.out.println("Please make sure all fields are correctly filled out");
			}

		} else {
			System.out.println("Cancelled");
		}

	}

	public void colorCostumizedNode() {
		for (Node n : graph.getEachNode()) {
			n.setAttribute("ui.color", jcc.getColor());
		}
	}

	private List<JButton> createStmtTypes(List<JButton> stmtTypes) {
		stmtTypes = new ArrayList<JButton>();

		// All button
		JButton btJNopStmt, btJIdentityStmt, btJAssignStmt, btJIfStmt, btJGotoStmt, btJTableSwitchStmt, btJLookupSwitchStmt, btJInvokeStmt, btJReturnStmt,
				JReturnVoidStmt, btJThrowStmt, btJRetStmt, btJEnterMonitorSmt, btJExitMonitorStmt;

		btJNopStmt = new JButton("soot.jimple.internal.JNopStmt");
		btJNopStmt.setName("soot.jimple.internal.JNopStmt");

		btJIdentityStmt = new JButton("soot.jimple.internal.JIdentityStmt");
		btJIdentityStmt.setName("soot.jimple.internal.JIdentityStmt");

		btJAssignStmt = new JButton("soot.jimple.internal.JAssignStmt");
		btJAssignStmt.setName("soot.jimple.internal.JAssignStmt");

		btJIfStmt = new JButton("soot.jimple.internal.JIfStmt");
		btJIfStmt.setName("soot.jimple.internal.JIfStmt");

		btJGotoStmt = new JButton("soot.jimple.internal.JGotoStmt");
		btJGotoStmt.setName("soot.jimple.internal.JGotoStmt");

		btJTableSwitchStmt = new JButton("soot.jimple.internal.JTableSwitchStmt");
		btJTableSwitchStmt.setName("soot.jimple.internal.JTableSwitchStmt");

		btJLookupSwitchStmt = new JButton("soot.jimple.internal.JLookupSwitchStmt");
		btJLookupSwitchStmt.setName("soot.jimple.internal.JLookupSwitchStmt");

		btJInvokeStmt = new JButton("soot.jimple.internal.JInvokeStmt");
		btJInvokeStmt.setName("soot.jimple.internal.JInvokeStmt");

		btJReturnStmt = new JButton("soot.jimple.internal.JReturnStmt");
		btJReturnStmt.setName("soot.jimple.internal.JReturnStmt");

		JReturnVoidStmt = new JButton("soot.jimple.internal.JReturnVoidStmt");
		JReturnVoidStmt.setName("soot.jimple.internal.JReturnVoidStmt");

		btJThrowStmt = new JButton("soot.jimple.internal.JThrowStmt");
		btJThrowStmt.setName("soot.jimple.internal.JThrowStmt");

		btJRetStmt = new JButton("soot.jimple.internal.JRetStmt");
		btJRetStmt.setName("soot.jimple.internal.JRetStmt");

		btJEnterMonitorSmt = new JButton("soot.jimple.internal.JEnterMonitorStmt");
		btJEnterMonitorSmt.setName("soot.jimple.internal.JEnterMonitorStmt");

		btJExitMonitorStmt = new JButton("soot.jimple.internal.JExitMonitorStmt");
		btJExitMonitorStmt.setName("soot.jimple.internal.JExitMonitorStmt");

		// Add buttons to the list
		stmtTypes.add(btJNopStmt);
		stmtTypes.add(btJIdentityStmt);
		stmtTypes.add(btJAssignStmt);
		stmtTypes.add(btJIfStmt);
		stmtTypes.add(btJGotoStmt);
		stmtTypes.add(btJTableSwitchStmt);
		stmtTypes.add(btJLookupSwitchStmt);
		stmtTypes.add(btJInvokeStmt);
		stmtTypes.add(btJReturnStmt);
		stmtTypes.add(JReturnVoidStmt);
		stmtTypes.add(btJThrowStmt);
		stmtTypes.add(btJRetStmt);
		stmtTypes.add(btJEnterMonitorSmt);
		stmtTypes.add(btJExitMonitorStmt);

		return stmtTypes;

	}

	/*private boolean inICFG(Graph graph) {
		boolean inICFG = false;

		for (Node n : graph.getEachNode()) {
			if ((n.getAttribute("nodeData.unitType") == null)) {
				inICFG = true;
				break;
			}
		}

		return inICFG;

	}*/

}