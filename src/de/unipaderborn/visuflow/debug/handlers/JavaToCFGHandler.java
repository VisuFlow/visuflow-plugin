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

	/**
	 * Get instance of default logger.`
	 */
	private Logger logger = Visuflow.getDefault().getLogger();

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
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
			HashMap<VFUnit, VFMethod> resultantUnit = getSelectedUnit(className, document, lineNumber);
			List<VFNode> unit = new ArrayList<>();
			if (resultantUnit.size() > 0) {
				if (event.getCommand().getId().equals("JavaHandler.NavigateToJimple")) {
					NavigationHandler handler = new NavigationHandler();
					handler.highlightJimpleSource(new ArrayList<>(resultantUnit.keySet()));
				} else {
					for (VFUnit vfUnit : resultantUnit.keySet()) {
						unit.add(new VFNode(vfUnit, 0));
					}
					try {
						ServiceUtil.getService(DataModel.class).filterGraph(unit, true, true, null);

					} catch (Exception e) {
						e.printStackTrace();
					}
				}

			}

			// call graph highlighting code here
		} else {
			logger.error("Editor not a  Text Editor");
		}
		return null;
	}
	/**
	 * This function filters the units of a class according to the function name and class name and content. 
	 * We need to filter this based on methods, because different function might contain similar lines of code and 
	 * hence their contents will be similar.
	 * @param className The name of the class.
	 * @param document The document which the user is currently interacting with
	 * @param content The contents of the document.
	 * @param lineNumber The linenumber on which the user has right-clicked.
	 * @return Map of filtered unit and the function it belongs to.
	 */
	private HashMap<VFUnit, VFMethod> getSelectedUnit(String className, IDocument document, int lineNumber) {
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		HashMap<VFUnit, VFMethod> map = new HashMap<VFUnit, VFMethod>();
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

								map.put(unit, method);

							}
						}
					}
				}
			}
		}
		return map;
	}
	/**
	 * This method returns the line numbers of all the methods passed to it.
	 * @param document The document with which the user is interacting.
	 * @param vfMethods The list of methods for which the line numbers are required.
	 * @return Map containing method names and their starting line numbers.
	 */
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
