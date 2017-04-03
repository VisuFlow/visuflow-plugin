package de.unipaderborn.visuflow.model.impl;

import java.io.File;
import java.util.Collections;
import java.util.Dictionary;
import java.util.HashMap;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.core.resources.IncrementalProjectBuilder;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.resources.WorkspaceJob;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IProgressMonitor;
import org.eclipse.core.runtime.IStatus;
import org.eclipse.core.runtime.Status;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventAdmin;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.debug.handlers.NavigationHandler;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.model.graph.ICFGStructure;
import soot.SootMethod;

public class DataModelImpl implements DataModel {

	private Logger logger = Visuflow.getDefault().getLogger();

	private List<VFClass> classList;

	/**
	 * Maintains the currently selected class
	 */
	private VFClass selectedClass;
	/**
	 * Maintains the currently selected method
	 */
	private VFMethod selectedMethod;

	/**
	 * Contains the list of all the class methods of {@link #selectedClass}
	 */
	private List<VFMethod> selectedClassMethods;
	/**
	 * Contains the list of all the units of {@link #selectedMethod}
	 */
	private List<VFUnit> selectedMethodUnits;
	@SuppressWarnings("unused")
	private List<VFUnit> selectedMethodincEdges;

	private EventAdmin eventAdmin;

	private ICFGStructure icfg;

	private List<VFNode> selectedNodes;

	private boolean selection;

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#listClasses()
	 */
	@Override
	public List<VFClass> listClasses() {
		if (classList == null) {
			return Collections.emptyList();
		}
		return classList;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#listMethods(de.unipaderborn.visuflow.model.VFClass)
	 */
	@Override
	public List<VFMethod> listMethods(VFClass vfClass) {
		List<VFMethod> methods = Collections.emptyList();
		if(classList != null) {
			for (VFClass current : classList) {
				if (current == vfClass) {
					methods = vfClass.getMethods();
				}
			}
		}
		return methods;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#listUnits(de.unipaderborn.visuflow.model.VFMethod)
	 */
	@Override
	public List<VFUnit> listUnits(VFMethod vfMethod) {
		List<VFUnit> units = Collections.emptyList();
		if(classList != null) {
			for (VFClass currentClass : classList) {
				for (VFMethod currentMethod : currentClass.getMethods()) {
					if (currentMethod == vfMethod) {
						units = vfMethod.getUnits();
					}
				}
			}
		}
		return units;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#getSelectedClass()
	 */
	@Override
	public VFClass getSelectedClass() {
		return selectedClass;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#getSelectedClassMethods()
	 */
	@Override
	public List<VFMethod> getSelectedClassMethods() {
		if (selectedClassMethods == null) {
			return Collections.emptyList();
		}
		return selectedClassMethods;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#getSelectedMethodUnits()
	 */
	@Override
	public List<VFUnit> getSelectedMethodUnits() {
		if (selectedMethodUnits == null) {
			return Collections.emptyList();
		}
		return selectedMethodUnits;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#setSelectedClass(de.unipaderborn.visuflow.model.VFClass)
	 */
	@Override
	public void setSelectedClass(VFClass selectedClass) {
		this.selectedClass = selectedClass;
		this.selectedMethod = this.selectedClass.getMethods().get(0);
		this.selectedClassMethods = this.selectedClass.getMethods();
		this.populateUnits();
		this.populateEdges();
		this.setSelectedMethod(this.selectedClass.getMethods().get(0), true);
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#setSelectedMethod(de.unipaderborn.visuflow.model.VFMethod, boolean)
	 */
	@Override
	public void setSelectedMethod(VFMethod selectedMethod, boolean panToNode) {
		//		if(this.selectedMethod != null && this.selectedMethod.toString().contentEquals(selectedMethod.toString()))
		//			return;
		this.selectedMethod = selectedMethod;
		this.populateUnits();
		this.populateEdges();
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("selectedMethod", selectedMethod);
		properties.put("panToNode", panToNode);
		properties.put("selectedMethodUnits", selectedMethodUnits);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_SELECTION, properties);
		eventAdmin.postEvent(modelChanged);
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#getSelectedMethod()
	 */
	@Override
	public VFMethod getSelectedMethod() {
		return selectedMethod;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#setClassList(java.util.List)
	 */
	@Override
	public void setClassList(List<VFClass> classList) {
		this.classList = classList;
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("model", classList);
		properties.put("icfg", icfg);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_MODEL_CHANGED, properties);
		eventAdmin.postEvent(modelChanged);
	}

	private void populateUnits() {
		this.selectedMethodUnits = this.selectedMethod.getUnits();
	}

	private void populateEdges() {
		this.selectedMethodincEdges = this.selectedMethod.getIncomingEdges();
	}

	public void setEventAdmin(EventAdmin eventAdmin) {
		this.eventAdmin = eventAdmin;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#getIcfg()
	 */
	@Override
	public ICFGStructure getIcfg() {
		return icfg;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#setIcfg(de.unipaderborn.visuflow.model.graph.ICFGStructure)
	 */
	@Override
	public void setIcfg(ICFGStructure icfg) {
		this.icfg = icfg;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#getVFMethodByName(soot.SootMethod)
	 */
	@Override
	public VFMethod getVFMethodByName(SootMethod method) {
		VFClass methodIncludingClass = null;
		String className = method.getDeclaringClass().getName();
		List<VFClass> classes = listClasses();
		Iterator<VFClass> classIterator = classes.iterator();
		while (classIterator.hasNext()) {
			VFClass temp = classIterator.next();
			if (temp.getSootClass().getName().contentEquals(className)) {
				methodIncludingClass = temp;
				break;
			}
		}
		Iterator<VFMethod> methodListIterator = listMethods(methodIncludingClass).iterator();
		while (methodListIterator.hasNext()) {
			VFMethod temp = methodListIterator.next();
			if (temp.getSootMethod().getSignature().contentEquals(method.getSignature())) {
				return temp;
			}
		}
		return null;
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#setInSet(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setInSet(String unitFqn, String name, String value) {
		VFUnit vfUnit = getVFUnit(unitFqn);
		if (vfUnit != null) {
			vfUnit.setInSet(value);
			fireUnitChanged(vfUnit);
		} else {
			logger.info("Unit not found " + unitFqn);
		}
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#setOutSet(java.lang.String, java.lang.String, java.lang.String)
	 */
	@Override
	public void setOutSet(String unitFqn, String name, String value) {
		VFUnit vfUnit = getVFUnit(unitFqn);
		if (vfUnit != null) {
			vfUnit.setOutSet(value);
			fireUnitChanged(vfUnit);
		} else {
			logger.info("Unit not found " + unitFqn);
		}
	}

	/*
	 * This is a naive implementation, we might need a faster data structure for this
	 */
	@Override
	public VFUnit getVFUnit(String fqn) {
		VFUnit result = null;
		if(classList != null) {
			for (VFClass vfClass : classList) {
				for (VFMethod vfMethod : vfClass.getMethods()) {
					for (VFUnit vfUnit : vfMethod.getUnits()) {
						if (vfUnit.getFullyQualifiedName().equals(fqn)) {
							result = vfUnit;
						}
					}
				}
			}
		}
		return result;
	}

	private void fireUnitChanged(VFUnit unit) {
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("unit", unit);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_UNIT_CHANGED, properties);
		eventAdmin.postEvent(modelChanged);
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#filterGraph(java.util.List, boolean, boolean, java.lang.String)
	 */
	@Override
	public void filterGraph(List<VFNode> selectedNodes, boolean selection, boolean panToNode, String uiClassName) {
		NavigationHandler handler = new NavigationHandler();
		handler.removeJimpleHighlight(true);
		this.selectedNodes = selectedNodes;
		this.selection = selection;
		if(uiClassName == null)
			uiClassName = "filter";

		if(!selectedNodes.isEmpty())
			this.setSelectedMethod(selectedNodes.get(0).getVFUnit().getVfMethod(), false);

		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("nodesToFilter", this.selectedNodes);
		properties.put("selection", this.selection);
		properties.put("uiClassName", uiClassName);
		properties.put("panToNode", panToNode);
		Event filterGraph = new Event(DataModel.EA_TOPIC_DATA_FILTER_GRAPH, properties);
		eventAdmin.postEvent(filterGraph);
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#HighlightJimpleUnit(de.unipaderborn.visuflow.model.VFNode)
	 */
	@Override
	public void HighlightJimpleUnit(VFNode node) {
		VFUnit unit = node.getVFUnit();
		String className = unit.getVfMethod().getVfClass().getSootClass().getName();
		VFMethod methodName = unit.getVfMethod();

		File fileToOpen = new File(className + ".jimple");

		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			try {
				IDE.openEditorOnFileStore(page, fileStore);
				IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
				if (part instanceof ITextEditor) {
					final ITextEditor editor = (ITextEditor) part;
					IDocumentProvider provider = editor.getDocumentProvider();
					IDocument document = provider.getDocument(editor.getEditorInput());

					int methodStartLine = getMethodLineNumber(document, methodName);
					if (methodStartLine > -1) {
						FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
						try {
							IRegion region = findReplaceDocumentAdapter.find(methodStartLine, unit.getFullyQualifiedName(), true, true, true, false);
							editor.selectAndReveal(region.getOffset(), unit.getFullyQualifiedName().length());
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					}
				}
			} catch (PartInitException e) {
				e.printStackTrace();
				// Put your exception handler here if you wish to
			}
		}
	}

	private int getMethodLineNumber(IDocument document, VFMethod method) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		try {
			method.getSootMethod().getBytecodeSignature();

			IRegion region = findReplaceDocumentAdapter.find(0, method.getSootMethod().getDeclaration(), true, true, false, false);
			return document.getLineOfOffset(region.getOffset());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return -1;

	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#triggerProjectRebuild()
	 */
	@Override
	public void triggerProjectRebuild() {
		WorkspaceJob build = new WorkspaceJob("rebuild") {

			@Override
			public IStatus runInWorkspace(IProgressMonitor monitor) throws CoreException {
				ResourcesPlugin.getWorkspace().build(IncrementalProjectBuilder.FULL_BUILD, monitor);
				return Status.OK_STATUS;
			}
		};
		build.schedule();
	}

	/* (non-Javadoc)
	 * @see de.unipaderborn.visuflow.model.DataModel#refreshView()
	 */
	@Override
	public void refreshView() {
		Event refreshView = new Event(DataModel.EA_TOPIC_DATA_VIEW_REFRESH, new HashMap<String,String>());
		eventAdmin.postEvent(refreshView);

	}
}
