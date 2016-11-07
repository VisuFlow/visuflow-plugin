package de.unipaderborn.visuflow.debug.handlers;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;
import de.unipaderborn.visuflow.util.MapUtil;

import org.eclipse.jface.dialogs.*;

public class JimpleBreakPointHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		DataModel model = ServiceUtil.getService(DataModel.class);
		List<VFMethod> methods = model.listMethods(null);
		for (VFMethod vfMethod : methods) {
			System.out.println(vfMethod.getBody());
		}

		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
		IFile file = input.getFile();
		IResource res = (IResource) file;

		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			IVerticalRulerInfo ruleInfo = editor.getAdapter(IVerticalRulerInfo.class);
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());
			try {
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
						MessageDialog.openInformation(window.getShell(), "Breakpoint could not be placed",
								"Error in inserting breakpoint");
					} else {
						IMarker[] problems = null;
						int depth = IResource.DEPTH_ZERO;
						problems = res.findMarkers("de.uni-paderborn.visuflow.plugin.JimpleBreakPointMarker", true,
								depth);

						Boolean markerPresent = false;
						for (IMarker item : problems) {
							int markerLineNmber = (int) item.getAttribute(IMarker.LINE_NUMBER);
							if (markerLineNmber == actualLineNumber) {
								markerPresent = true;
								item.delete();
								MessageDialog.openInformation(window.getShell(),
										"Debugger deleted at line: " + actualLineNumber, content);
							}
						}

						if (!markerPresent) {

							IMarker m = res.createMarker("de.uni-paderborn.visuflow.plugin.JimpleBreakPointMarker");
							m.setAttribute(IMarker.LINE_NUMBER, actualLineNumber);
							m.setAttribute(IMarker.MESSAGE, content);
							m.setAttribute(IMarker.TEXT, "Jimple Breakpoint");
							m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
							m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
							MessageDialog.openInformation(window.getShell(),
									"Debugger set at line: " + actualLineNumber, content);
						}

						System.out.printf("Line Number:%d\n", (lineNumber + 1));
						System.out.printf("The contents of the line :   %s", content);

						System.out.println();
					}
				} else {
					MessageDialog.openInformation(window.getShell(), "Breakpoint could not be placed",
							"Error in inserting breakpoint");
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private VFUnit getSelectedUnit(String className, IDocument document, String content, int lineNumber) {
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
								System.out.println(unit.toString());
								return unit;
							}
						}
					}
				}
			}
		}
		return null;
	}

	private Map<String, Integer> getMethodLineNumbers(IDocument document, List<VFMethod> vfMethods) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>();
		for (VFMethod method : vfMethods) {
			try {
				method.getSootMethod().getBytecodeSignature();

				IRegion region = findReplaceDocumentAdapter.find(0, method.getSootMethod().getDeclaration(), true, true,
						false, false);
				result.put(method.getSootMethod().getDeclaration(), document.getLineOfOffset(region.getOffset()));
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return MapUtil.sortByValue(result);
	}
}
