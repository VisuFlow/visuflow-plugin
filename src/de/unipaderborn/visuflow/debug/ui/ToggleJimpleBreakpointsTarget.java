package de.unipaderborn.visuflow.debug.ui;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.ui.actions.IToggleBreakpointsTarget;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPart;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.debug.JimpleBreakpointManager;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.MapUtil;
import de.unipaderborn.visuflow.util.ServiceUtil;

/**
 * Handles the double click in the left ruler bar of the JimpleEditor.
 * If a jimple breakpoint exists, we remove it and all of its associated java breakpoints.
 * Otherwise we determine all the properties needed to create an IMarker, create that IMarker
 * and pass that to the JimpleBreakpointManager to create the actual breakpoints.
 *
 * @author henni@upb.de
 *
 */
public class ToggleJimpleBreakpointsTarget implements IToggleBreakpointsTarget, VisuflowConstants {

	private Logger logger = Visuflow.getDefault().getLogger();

	private IWorkbenchPart part;

	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		this.part = part;

		try {
			if(jimpleBreakpointExists()) {
				deleteJimpleBreakpoint();
			} else {
				createJimpleBreakpoint();
			}
		} catch (BadLocationException e) {
			logger.error("Couldn't place breakpoint", e);
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Breakpoint could not be placed", "Error in inserting breakpoint");
		}
	}

	private void createJimpleBreakpoint() throws CoreException, BadLocationException {
		IDocument document = getDocument();
		IFile file = getFile();
		int lineNumber = getLineNumber();

		int offset = document.getLineOffset(lineNumber - 1);
		int length = document.getLineInformation(lineNumber - 1).getLength();
		int charStart = offset;
		int charEnd = offset + length;
		String unitFqn = getUnitFqn(lineNumber - 1, offset, length);

		IMarker m = file.createMarker(JIMPLE_BREAKPOINT_MARKER);
		m.setAttribute(IMarker.LINE_NUMBER, getLineNumber());
		m.setAttribute(IMarker.MESSAGE, "Unit breakpoint: " + file.getName() + " [Line "+getLineNumber()+"]");
		m.setAttribute("Jimple.file", file.getProjectRelativePath().toPortableString());
		m.setAttribute("Jimple.project", file.getProject().getName());
		m.setAttribute("Jimple.unit.charStart", charStart);
		m.setAttribute("Jimple.unit.charEnd", charEnd);
		m.setAttribute("Jimple.unit.fqn", unitFqn);

		JimpleBreakpointManager.getInstance().createBreakpoint(m);
	}

	private String getUnitFqn(int lineNumber, int offset, int length) throws BadLocationException {
		IDocument document = getDocument();
		IFile file = getFile();
		String content = document.get(offset, length).trim();
		if (content.trim().length() > 0) {
			String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
			VFUnit resultantUnit = getSelectedUnit(className, document, content.trim().substring(0, content.length() - 1), lineNumber);
			if (resultantUnit != null) {
				return resultantUnit.getFullyQualifiedName();
			}
		}
		throw new RuntimeException("Couldn't determine fully qualified name for unit");
	}

	private void deleteJimpleBreakpoint() throws CoreException, BadLocationException {
		IMarker[] markers = getMarkersInFile();
		for (IMarker item : markers) {
			int markerLineNumber = (int) item.getAttribute(IMarker.LINE_NUMBER);
			if (markerLineNumber == getLineNumber()) {
				item.delete();
			}
		}
	}

	private boolean jimpleBreakpointExists() throws CoreException, BadLocationException {
		IMarker[] markers = getMarkersInFile();
		boolean markerPresent = false;
		for (IMarker item : markers) {
			int markerLineNumber = (int) item.getAttribute(IMarker.LINE_NUMBER);
			if (markerLineNumber == getLineNumber()) {
				markerPresent = true;
			}
		}
		return markerPresent;
	}

	private IMarker[] getMarkersInFile() throws CoreException {
		IFile file = getFile();
		String markerType = JIMPLE_BREAKPOINT_MARKER;
		IMarker[] markers = file.findMarkers(markerType, true, IResource.DEPTH_ZERO);
		return markers;
	}

	private IFile getFile() {
		final ITextEditor editor = (ITextEditor) part;
		IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
		IFile file = input.getFile();
		return file;
	}

	private int getLineNumber() throws BadLocationException {
		IVerticalRulerInfo ruleInfo = getTextEditor().getAdapter(IVerticalRulerInfo.class);
		int lineNumber = ruleInfo.getLineOfLastMouseButtonActivity();
		int actualLineNumber = lineNumber + 1;
		return actualLineNumber;
	}

	private ITextEditor getTextEditor() {
		return (ITextEditor) part;
	}

	private IDocument getDocument() {
		IDocumentProvider provider = getTextEditor().getDocumentProvider();
		IDocument document = provider.getDocument(getTextEditor().getEditorInput());
		return document;
	}

	private VFUnit getSelectedUnit(String className, IDocument document, String content, int lineNumber) throws BadLocationException {
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		// VFClass
		// vfClass=dataModel.listClasses().stream().filter(x->x.getSootClass().getName()==className).collect(Collectors.toList()).get(0);
		for (VFClass vfClass : dataModel.listClasses()) {
			if (vfClass.getSootClass().getName().equals(className)) {
				List<VFMethod> vfMethods = vfClass.getMethods();
				Map<String, Integer> methodLines = getMethodLineNumbers(document, vfMethods);
				Collection<Integer> allMethodLines = methodLines.values();
				List<Integer> lesserThanCuurent = allMethodLines.stream().filter(x -> x.intValue() < lineNumber)
						.collect(Collectors.toList());
				int toBeCompared = lesserThanCuurent.get(lesserThanCuurent.size() - 1);
				for (VFMethod method : vfMethods) {
					int methodLine = methodLines.get(method.getSootMethod().getDeclaration());
					if (toBeCompared == methodLine) {
						for (VFUnit unit : method.getUnits()) {
							if (unit.getUnit().toString().trim().equals(content)) {
								return unit;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private Map<String, Integer> getMethodLineNumbers(IDocument document, List<VFMethod> vfMethods) throws BadLocationException {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		TreeMap<String, Integer> result = new TreeMap<>();
		for (VFMethod method : vfMethods) {
			method.getSootMethod().getBytecodeSignature();
			IRegion region = findReplaceDocumentAdapter.find(0, method.getSootMethod().getDeclaration(), true, true, false, false);
			result.put(method.getSootMethod().getDeclaration(), document.getLineOfOffset(region.getOffset()));
		}
		return MapUtil.sortByValue(result);
	}

	@Override
	public boolean canToggleLineBreakpoints(IWorkbenchPart part, ISelection selection) {
		return true;
	}

	@Override
	public void toggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean canToggleMethodBreakpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

	@Override
	public void toggleWatchpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		throw new RuntimeException("Not implemented");
	}

	@Override
	public boolean canToggleWatchpoints(IWorkbenchPart part, ISelection selection) {
		return false;
	}

}
