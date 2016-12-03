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

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.MapUtil;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class NavigateToCFG extends AbstractHandler {

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
					HashMap<VFMethod,VFUnit> resultantUnit = getSelectedUnit(className, document,
							content.trim().substring(0, content.length() - 1), lineNumber);
					List<VFNode> unit = new ArrayList<>();
					unit.add(new VFNode((VFUnit) resultantUnit.values().toArray()[0], 0));
				try {
					ServiceUtil.getService(DataModel.class).filterGraph(unit,((VFMethod) resultantUnit.keySet().toArray()[0]).getSootMethod(), true);
				} catch (Exception e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				}
			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}

	private HashMap<VFMethod,VFUnit> getSelectedUnit(String className, IDocument document, String content, int lineNumber) {
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
								HashMap<VFMethod,VFUnit> map = new HashMap<VFMethod, VFUnit>();
								map.put(method, unit);
								return map;
							}
						}
					}
				}
			}
		}
		return new HashMap<VFMethod,VFUnit>();
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
