package de.unipaderborn.visuflow.debug;

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
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.MapUtil;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class ToggleJimpleBreakpointsTarget implements IToggleBreakpointsTarget {

	private Logger logger = Visuflow.getDefault().getLogger();

	@Override
	public void toggleLineBreakpoints(IWorkbenchPart part, ISelection selection) throws CoreException {
		final ITextEditor editor = (ITextEditor) part;
		IDocumentProvider provider = editor.getDocumentProvider();
		IDocument document = provider.getDocument(editor.getEditorInput());
		IFileEditorInput input = (IFileEditorInput) editor.getEditorInput();
		IFile file = input.getFile();
		try {
			IVerticalRulerInfo ruleInfo = editor.getAdapter(IVerticalRulerInfo.class);
			int lineNumber = ruleInfo.getLineOfLastMouseButtonActivity();
			int offset = document.getLineOffset(lineNumber);
			int length = document.getLineInformation(lineNumber).getLength();
			int actualLineNumber = lineNumber + 1;
			String content = document.get(offset, length).trim();
			if (content.trim().length() > 0) {
				String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
				VFUnit resultantUnit = getSelectedUnit(className, document,
						content.trim().substring(0, content.length() - 1), lineNumber);
				if (resultantUnit == null) {
					MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Breakpoint could not be placed",
							"Error in inserting breakpoint");
				} else {
					IMarker[] problems = null;
					int depth = IResource.DEPTH_ZERO;
					IResource res = file;

					//String markerType = IBreakpoint.BREAKPOINT_MARKER;
					String markerType = "visuflow.debug.breakpoint.marker";
					problems = res.findMarkers(markerType, true,
							depth);

					Boolean markerPresent = false;
					for (IMarker item : problems) {
						int markerLineNmber = (int) item.getAttribute(IMarker.LINE_NUMBER);
						if (markerLineNmber == actualLineNumber) {
							markerPresent = true;
							item.delete();
						}
					}

					if (!markerPresent) {
						int charStart = offset;
						int charEnd = offset + length;
						String unitFqn = resultantUnit.getFullyQualifiedName();

						IMarker m = res.createMarker(markerType);
						m.setAttribute(IMarker.LINE_NUMBER, actualLineNumber);
						m.setAttribute(IMarker.MESSAGE, "Unit breakpoint: " + file.getName() + " [Line "+actualLineNumber+"]");
						m.setAttribute("Jimple.file", file.getProjectRelativePath().toPortableString());
						m.setAttribute("Jimple.project", file.getProject().getName());
						m.setAttribute("Jimple.unit.charStart", charStart);
						m.setAttribute("Jimple.unit.charEnd", charEnd);
						m.setAttribute("Jimple.unit.fqn", unitFqn);

						JimpleBreakpointManager.getInstance().createBreakpoint(m);
					}
				}
			} else {
				// TODO replace this with a mechanism, which tries to find the next valid line (similar to the standard java editor breakpoints)
				MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Breakpoint could not be placed", "The selected line does not contain a valid Jimple unit");
			}
		} catch (Exception e) {
			logger.error("Couldn't create jimple breakpoint", e);
			MessageDialog.openInformation(Display.getCurrent().getActiveShell(), "Breakpoint could not be placed", "Error in inserting breakpoint");
		}
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
