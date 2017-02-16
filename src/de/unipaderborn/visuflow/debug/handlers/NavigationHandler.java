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
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IWorkspaceRoot;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.core.runtime.IPath;
import org.eclipse.core.runtime.Path;
import org.eclipse.jdt.core.IJavaProject;
import org.eclipse.jdt.core.IType;
import org.eclipse.jdt.core.JavaCore;
import org.eclipse.jdt.core.JavaModelException;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.FindReplaceDocumentAdapter;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IEditorReference;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.editors.text.TextFileDocumentProvider;
import org.eclipse.ui.handlers.HandlerUtil;
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
import soot.Unit;
import soot.Value;
import soot.ValueBox;
import soot.jimple.internal.JInstanceFieldRef;
import soot.tagkit.LineNumberTag;

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
					HashMap<VFUnit, VFMethod> resultantUnit = getSelectedUnit(className, document, content.trim().substring(0, content.length() - 1),
							lineNumber);
					List<VFNode> unit = new ArrayList<>();
					if (resultantUnit.size() > 0) {
						unit.add(new VFNode((VFUnit) resultantUnit.keySet().toArray()[0], 0));
					}
					if (event.getCommand().getId().equals("JimpleEditor.NavigateToCFG")) {
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					else if (event.getCommand().getId().equals("JimpleEditor.NavigateToUnitView")) {
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true, null);
						} catch (Exception e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} else if (event.getCommand().getId().equals("JimpleEditor.VariablePath")) {
						try {
							List<VFNode> unitList = prepareVariablePath(className, document, content.trim().substring(0, content.length() - 1), lineNumber);
							ServiceUtil.getService(DataModel.class).filterGraph(unitList, true, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (event.getCommand().getId().equals("JimpleEditor.sourceCodeCommand")) {
						if (unit.size() > 0) {

							LineNumberTag ln = (LineNumberTag) unit.get(0).getUnit().getTag("LineNumberTag");
							if (ln != null) {
								HighLightSourceCode(ln.getLineNumber(), className);

							}else{
								IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
								MessageDialog.openInformation(window.getShell(), "Error",
										"No equivalent java source line found");
							}
						}

					}
				}
			} catch (BadLocationException e) {
				e.printStackTrace();
			}
		}
		return null;
	}

	public void HighlightJimpleLine(ArrayList<VFUnit> units) {

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
				for (VFUnit unit : units) {
					// Get the current page
					String className = unit.getVfMethod().getVfClass().getSootClass().getName();
					// IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();

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
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
						IDocument document = provider.getDocument(file);
						Integer methodLine = getMethodLineNumbers(document, unit.getVfMethod());
						FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);

						try {
							IRegion region = findReplaceDocumentAdapter.find(methodLine,
									FindReplaceDocumentAdapter.escapeForRegExPattern(unit.getUnit().toString()), true, true, false, true);
							// IRegion region = findReplaceDocumentAdapter.find(methodLine, unit.getUnit().toString(), true, true, true, false);
							if (region != null) {
								ITextEditor editor = (ITextEditor) IDE.openEditor(page, file);
								// the 1 added is to include the semi colon
								editor.selectAndReveal(region.getOffset(), region.getLength() + 1);
							}
						} catch (BadLocationException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (PartInitException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});

	}

	public void NavigateToSource(VFUnit unit) {

		LineNumberTag ln = (LineNumberTag) unit.getUnit().getTag("LineNumberTag");
		String className = unit.getVfMethod().getVfClass().getSootClass().getName();
		if (ln != null) {
			HighLightSourceCode(ln.getLineNumber(), className);
		}
	}
	
	public void RemoveJimpleHighlight(){
		IEditorReference[] references = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
		for (int i=0; i<references.length; i++) {
            //List out all Exist editor
            //compare with EmployeeEditor.Id="rcp_demo.Editor.emp";
			IEditorPart editorpart = references[i].getEditor(false);
             if(editorpart  instanceof ITextEditor){
            	 final ITextEditor editor = (ITextEditor) editorpart;
            	 editor.selectAndReveal(0,0);
             }
		}
	}

	private HashMap<VFUnit, VFMethod> getSelectedUnit(String className, IDocument document, String content, int lineNumber) {
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		HashMap<VFUnit, VFMethod> map = new HashMap<VFUnit, VFMethod>();
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
								map.put(unit, method);

							}
						}
					}
				}
			}
		}
		return map;
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

	private List<VFNode> prepareVariablePath(String className, IDocument document, String content, int lineNumber) {
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		List<VFNode> unitList = new ArrayList<VFNode>();
		// Map<VFUnit,Value> map = new LinkedHashMap<VFUnit,Value>();
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
						unitList = createPointsToSet(method, content);
					}
				}
			}
		}
		return unitList;
	}

	private List<VFNode> createPointsToSet(VFMethod method, String content) {
		List<VFNode> unitList = new ArrayList<VFNode>();
		List<Value> valueList = new ArrayList<Value>();
		for (VFUnit unit : method.getUnits()) {
			Unit u = unit.getUnit();
			for (ValueBox db : u.getDefBoxes()) {
				for (ValueBox ub : u.getUseBoxes()) {
					if (!valueList.contains(ub.getValue())) {
						valueList.remove(db.getValue());
					}
				}
			}
			if (unit.getUnit().toString().trim().equals(content)) {
				for (ValueBox db : u.getDefBoxes()) {
					unitList.add(new VFNode(unit, 0));
					valueList.add(db.getValue());
				}
			}
			System.out.println(unit.getUnit());
			for (ValueBox ub : u.getUseBoxes()) {
				if (valueList.contains(ub.getValue())) {
					if (u.getDefBoxes().isEmpty()) {
						unitList.add(new VFNode(unit, 0));
					}
					for (ValueBox db : u.getDefBoxes()) {
						if (db.getValue() instanceof JInstanceFieldRef) {
							JInstanceFieldRef jirf = (JInstanceFieldRef) db.getValue();
							if (!jirf.getBase().equals(ub.getValue())) {
								unitList.add(new VFNode(unit, 0));
								valueList.add(db.getValue());
							}
						} else {
							unitList.add(new VFNode(unit, 0));
							valueList.add(db.getValue());
						}
					}
				}
			}
		}
		return unitList;
	}

	private Integer getMethodLineNumbers(IDocument document, VFMethod method) {

		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		try {
			IRegion region = findReplaceDocumentAdapter.find(0, FindReplaceDocumentAdapter.escapeForRegExPattern(method.getSootMethod().getDeclaration()), true,
					true, false, true);
			return region.getOffset();
		} catch (BadLocationException e) {
			e.printStackTrace();
		}
		return 0;
	}

	private void HighLightSourceCode(int lineNumber, String className) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				IWorkspaceRoot myWorkspaceRoot = ResourcesPlugin.getWorkspace().getRoot();
				IProject analysisProject = myWorkspaceRoot.getProject(GlobalSettings.get("TargetProject"));
				if (analysisProject.exists()) {
					IJavaProject javaProj = JavaCore.create(analysisProject);
					try {
						IType classType = javaProj.findType(className);
						IFile classFile = ResourcesPlugin.getWorkspace().getRoot().getFile(classType.getPath());
						IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
						IWorkbenchPage page = window.getActivePage();
						try {

							ITextEditor javaEditor = (ITextEditor) IDE.openEditor(page, classFile, true);
							IDocument javaDocument = javaEditor.getDocumentProvider().getDocument(javaEditor.getEditorInput());
							if (javaDocument != null) {
								IRegion lineInfo = null;
								try {
									lineInfo = javaDocument.getLineInformation(lineNumber - 1);
								} catch (BadLocationException e) {
								}
								if (lineInfo != null) {
									javaEditor.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
								}
							}

						} catch (PartInitException e) {
							// TODO Auto-generated catch block
							e.printStackTrace();
						}
					} catch (JavaModelException e) {
						// TODO Auto-generated catch block
						e.printStackTrace();
					}
				}
			}
		});
	}

}
