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
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JInstanceFieldRef;

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
					HashMap<VFMethod,VFUnit> resultantUnit = getSelectedUnit(className, document,
							content.trim().substring(0, content.length() - 1), lineNumber);
					List<VFNode> unit = new ArrayList<>();
					unit.add(new VFNode((VFUnit) resultantUnit.values().toArray()[0], 0));
					if (event.getCommand().getId().equals("JimpleEditor.NavigateToCFG"))
					{
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					
					else if (event.getCommand().getId().equals("JimpleEditor.NavigateToUnitView"))
					{
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
					else if (event.getCommand().getId().equals("JimpleEditor.VariablePath"))
					{
						try {
							List<VFNode> unitList = prepareVariablePath(className, document,
									content.trim().substring(0, content.length() - 1), lineNumber);
							ServiceUtil.getService(DataModel.class).filterGraph(unitList, true);
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
				e.printStackTrace();
			}
		}
		return MapUtil.sortByValue(result);
	}
	
	private List<VFNode> prepareVariablePath(String className, IDocument document, String content, int lineNumber){
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		List<VFNode> unitList = new ArrayList<VFNode>();
		//Map<VFUnit,Value> map = new LinkedHashMap<VFUnit,Value>();
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
						unitList = createPointsToSet(method, content);
					}
				}
			}
		}
		return unitList;
	}
	
	private List<VFNode> createPointsToSet(VFMethod method, String content){
		List<VFNode> unitList = new ArrayList<VFNode>();
		List<Value> valueList = new ArrayList<Value>();
		for (VFUnit unit : method.getUnits()) {
			Unit u = unit.getUnit();
			for(ValueBox db : u.getDefBoxes()){
				for(ValueBox ub : u.getUseBoxes()){
					if(!valueList.contains(ub.getValue())){
						valueList.remove(db.getValue());
					}
				}
			}
			if (unit.getUnit().toString().trim().equals(content)) {
				for(ValueBox db : u.getDefBoxes()){
					unitList.add(new VFNode(unit, 0));
					valueList.add(db.getValue());
				}
			}
			System.out.println(unit.getUnit());
			for(ValueBox ub : u.getUseBoxes()){
				if(valueList.contains(ub.getValue())){
					if(u.getDefBoxes().isEmpty()){
						unitList.add(new VFNode(unit, 0));
					}
					for(ValueBox db : u.getDefBoxes()){
						if(db.getValue() instanceof JInstanceFieldRef){
							JInstanceFieldRef jirf = (JInstanceFieldRef) db.getValue();
							if(!jirf.getBase().equals(ub.getValue())){
								unitList.add(new VFNode(unit, 0));
								valueList.add(db.getValue());
							}
						}
						else {
							unitList.add(new VFNode(unit, 0));
							valueList.add(db.getValue());
						}
					}
				}
			}
		}
		return unitList;
	}
	
}
