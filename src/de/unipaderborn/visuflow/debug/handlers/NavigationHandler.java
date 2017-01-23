package de.unipaderborn.visuflow.debug.handlers;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.unipaderborn.visuflow.builder.GlobalSettings;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.MapUtil;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class NavigationHandler extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();

		IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
		IFile file = input.getFile();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			IVerticalRulerInfo ruleInfo = editor.getAdapter(IVerticalRulerInfo.class);
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());
			try {
				int lineNumber = ruleInfo.getLineOfLastMouseButtonActivity();
				int offset = document.getLineOffset(lineNumber);
				int length = document.getLineInformation(lineNumber).getLength();
				String content = document.get(offset, length).trim();
				if (content.trim().length() > 0) {
					String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
					HashMap<VFMethod, VFUnit> resultantUnit = getSelectedUnit(className, document, content.trim().substring(0, content.length() - 1),
							lineNumber);
					List<VFNode> unit = new ArrayList<>();
					unit.add(new VFNode((VFUnit) resultantUnit.values().toArray()[0], 0));
					if (event.getCommand().getId().equals("JimpleEditor.NavigateToCFG")) {
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					else if (event.getCommand().getId().equals("JimpleEditor.NavigateToUnitView")) {
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	private HashMap<VFMethod, VFUnit> getSelectedUnit(String className, IDocument document, String content, int lineNumber) {
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		// VFClass
		// vfClass=dataModel.listClasses().stream().filter(x->x.getSootClass().getName()==className).collect(Collectors.toList()).get(0);
		for (VFClass vfClass : dataModel.listClasses()) {
			if (vfClass.getSootClass().getName().equals(className)) {
				List<VFMethod> vfMethods = vfClass.getMethods();
				Map<String, Integer> methodLines = getMethodLineNumbers(document, vfMethods);
				Collection<Integer> allMethodLines = methodLines.values();
				List<Integer> lesserThanCuurent = allMethodLines.stream().filter(x -> x.intValue() < lineNumber).collect(Collectors.toList());
				int toBeCompared = lesserThanCuurent.get(lesserThanCuurent.size() - 1);
				for (VFMethod method : vfMethods) {
					int methodLine = methodLines.get(method.getSootMethod().getDeclaration());
					if (toBeCompared == methodLine) {
						for (VFUnit unit : method.getUnits()) {
							if (unit.getUnit().toString().trim().equals(content)) {
								HashMap<VFMethod, VFUnit> map = new HashMap<VFMethod, VFUnit>();
								map.put(method, unit);
								return map;
							}
						}
					}
				}
			}
		}
		return new HashMap<VFMethod, VFUnit>();
	}

	private Map<String, Integer> getMethodLineNumbers(IDocument document, List<VFMethod> vfMethods) {

		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		TreeMap<String, Integer> result = new TreeMap<String, Integer>();
		for (VFMethod method : vfMethods) {
			try {
				method.getSootMethod().getBytecodeSignature();

				IRegion region = findReplaceDocumentAdapter.find(0, method.getSootMethod().getDeclaration(), true, true, false, false);
				result.put(method.getSootMethod().getDeclaration(), document.getLineOfOffset(region.getOffset()));
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return MapUtil.sortByValue(result);
	}

	private Integer getMethodLineNumbers(IDocument document, VFMethod method) {

		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		try {
			method.getSootMethod().getBytecodeSignature();

			IRegion region = findReplaceDocumentAdapter.find(0, method.getSootMethod().getDeclaration(), true, true, false, false);
			return document.getLineOfOffset(region.getOffset());
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	public void HighlightJimpleLine(ArrayList<VFUnit> units) {
		for (VFUnit unit : units) {
			// Get the current page
			String className = unit.getVfMethod().getVfClass().getSootClass().getName();
			IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
			IWorkbenchPage page = window.getActivePage();
			String projectName = GlobalSettings.get("AnalysisProject");

			IPath path = new Path(projectName + "/sootOutput/" + className);
			path = path.addFileExtension("jimple");

			IFile file = ResourcesPlugin.getWorkspace().getRoot().getFile(path);
			// Open default editor for the file

			try {
				IDE.openEditor(page, file, true);
				IDocumentProvider provider = new TextFileDocumentProvider();
				try {
					provider.connect(file);
				} catch (CoreException e) {
					e.printStackTrace();
				}
				IDocument document = provider.getDocument(file);
				Integer methodLine = getMethodLineNumbers(document, unit.getVfMethod());
				FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
				
				try {
					IRegion region = findReplaceDocumentAdapter.find(methodLine, FindReplaceDocumentAdapter.escapeForRegExPattern(unit.getUnit().toString()), true, true, false, true);
					if (region != null) {
						ITextEditor editor = (ITextEditor) IDE.openEditor(page, file);
						editor.selectAndReveal(region.getOffset(), region.getLength());
					}
				} catch (BadLocationException e) {
					e.printStackTrace();
				}
			} catch (PartInitException e) {
				e.printStackTrace();
			}
		}
	}

}
