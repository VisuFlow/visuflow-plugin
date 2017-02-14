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
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.MapUtil;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.tagkit.LineNumberTag;

public class JavaToCFGHandler extends AbstractHandler {

	private Logger logger = Visuflow.getDefault().getLogger();

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
			int lineNumber = ruleInfo.getLineOfLastMouseButtonActivity();
			String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
			HashMap<VFMethod, VFUnit> resultantUnit = getSelectedUnit(className, document, lineNumber);
			List<VFNode> unit = new ArrayList<>();
			if (resultantUnit.size() > 0) {
				for (VFUnit vfUnit : resultantUnit.values()) {
					unit.add(new VFNode(vfUnit, 0));
				}
			}
			try {
				ServiceUtil.getService(DataModel.class).filterGraph(unit, true, null);
			} catch (Exception e) {
				e.printStackTrace();
			}
			// call graph highlighting code here
		} else {
			logger.error("Editor not a  Text Editor");
		}
		return null;
	}

	private HashMap<VFMethod, VFUnit> getSelectedUnit(String className, IDocument document, int lineNumber) {
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		// VFClass
		// vfClass=dataModel.listClasses().stream().filter(x->x.getSootClass().getName()==className).collect(Collectors.toList()).get(0);
		for (VFClass vfClass : dataModel.listClasses()) {
			if (vfClass.getSootClass().getShortJavaStyleName().equals(className)) {
				List<VFMethod> vfMethods = vfClass.getMethods();
				Map<String, Integer> methodLines = getMethodLineNumbers(document, vfMethods);
				Collection<Integer> allMethodLines = methodLines.values();
				List<Integer> lesserThanCuurent = allMethodLines.stream().filter(x -> x.intValue() < lineNumber).collect(Collectors.toList());
				int toBeCompared = lesserThanCuurent.get(lesserThanCuurent.size() - 1);
				for (VFMethod method : vfMethods) {
					int methodLine = methodLines.getOrDefault(method.getSootMethod().getDeclaration(), 0);
					if (methodLine != 0 && toBeCompared == methodLine) {
						for (VFUnit unit : method.getUnits()) {
							LineNumberTag ln = (LineNumberTag) unit.getUnit().getTag("LineNumberTag");
							if (ln != null && ln.getLineNumber() == lineNumber + 1) {
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

				IRegion region = findReplaceDocumentAdapter.find(0,
						method.getSootMethod().getDeclaration().substring(0, method.getSootMethod().getDeclaration().indexOf('(')), true, true, false, false);
				if (region != null) {
					result.put(method.getSootMethod().getDeclaration(), document.getLineOfOffset(region.getOffset()));
				}

			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return MapUtil.sortByValue(result);
	}

}
