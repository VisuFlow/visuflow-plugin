package de.unipaderborn.visuflow.model.impl;

import java.io.File;
import java.util.Collections;
import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
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

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFMethodEdge;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.model.graph.ICFGStructure;
import soot.SootMethod;

public class DataModelImpl implements DataModel {

	private List<VFClass> classList;

	private VFClass selectedClass;
	private VFMethod selectedMethod;

	private List<VFMethod> selectedClassMethods;
	private List<VFUnit> selectedMethodUnits;
	private List<VFMethodEdge> selectedMethodincEdges;

	private EventAdmin eventAdmin;

	private ICFGStructure icfg;

	private List<VFNode> selectedNodes;

	private boolean selection;

	@Override
	public List<VFClass> listClasses() {
		if (classList == null) {
			return Collections.emptyList();
		}
		return classList;
	}

	@Override
	public List<VFMethod> listMethods(VFClass vfClass) {
		List<VFMethod> methods = Collections.emptyList();
		for (VFClass current : classList) {
			if (current == vfClass) {
				methods = vfClass.getMethods();
			}
		}
		return methods;
	}

	@Override
	public List<VFUnit> listUnits(VFMethod vfMethod) {
		List<VFUnit> units = Collections.emptyList();
		for (VFClass currentClass : classList) {
			for (VFMethod currentMethod : currentClass.getMethods()) {
				if (currentMethod == vfMethod) {
					units = vfMethod.getUnits();
				}
			}
		}
		return units;
	}

	@Override
	public VFClass getSelectedClass() {
		return selectedClass;
	}

	@Override
	public List<VFMethod> getSelectedClassMethods() {
		if (selectedClassMethods == null) {
			return Collections.emptyList();
		}
		return selectedClassMethods;
	}

	@Override
	public List<VFUnit> getSelectedMethodUnits() {
		if (selectedMethodUnits == null) {
			return Collections.emptyList();
		}
		return selectedMethodUnits;
	}

	@Override
	public void setSelectedClass(VFClass selectedClass) {
		this.selectedClass = selectedClass;
		this.selectedMethod = this.selectedClass.getMethods().get(0);
		this.selectedClassMethods = this.selectedClass.getMethods();
		this.populateUnits();
		this.populateEdges();
		this.setSelectedMethod(this.selectedClass.getMethods().get(0), true);
	}

	@Override
	public void setSelectedMethod(VFMethod selectedMethod, boolean panToNode) {
		this.selectedMethod = selectedMethod;
		this.populateUnits();
		this.populateEdges();
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("selectedMethod", selectedMethod);
		properties.put("panToNode", panToNode);
		// properties.put("selectedClassMethods", selectedClassMethods);
		properties.put("selectedMethodUnits", selectedMethodUnits);
		Event modelChanged = new Event(DataModel.EA_TOPIC_DATA_SELECTION, properties);
		eventAdmin.postEvent(modelChanged);
	}

	@Override
	public VFMethod getSelectedMethod() {
		return selectedMethod;
	}

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

	@Override
	public ICFGStructure getIcfg() {
		return icfg;
	}

	@Override
	public void setIcfg(ICFGStructure icfg) {
		this.icfg = icfg;
	}

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

	@Override
	public void setInSet(String unitFqn, String name, String value) {
		VFUnit vfUnit = getVFUnit(unitFqn);
		if (vfUnit != null) {
			vfUnit.setInSet(value);
			fireUnitChanged(vfUnit);
		}
	}

	@Override
	public void setOutSet(String unitFqn, String name, String value) {
		VFUnit vfUnit = getVFUnit(unitFqn);
		if (vfUnit != null) {
			vfUnit.setOutSet(value);
			fireUnitChanged(vfUnit);
		}
	}

	/*
	 * This is a naive implementation, we might need a faster data structure for this
	 */
	private VFUnit getVFUnit(String fqn) {
		VFUnit result = null;
		for (VFClass vfClass : classList) {
			for (VFMethod vfMethod : vfClass.getMethods()) {
				for (VFUnit vfUnit : vfMethod.getUnits()) {
					if (vfUnit.getFullyQualifiedName().equals(fqn)) {
						result = vfUnit;
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

	@Override
	public void filterGraph(List<VFNode> selectedNodes, boolean selection) throws Exception {
		this.selectedNodes = selectedNodes;
		this.selection = selection;
		
		if(!selectedNodes.isEmpty())
			this.setSelectedMethod(selectedNodes.get(0).getVFUnit().getVfMethod(), false);
		
		Dictionary<String, Object> properties = new Hashtable<>();
		properties.put("nodesToFilter", this.selectedNodes);
		properties.put("selection", this.selection);
		Event filterGraph = new Event(DataModel.EA_TOPIC_DATA_FILTER_GRAPH, properties);
		eventAdmin.postEvent(filterGraph);
	}

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
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return -1;

	}
}
