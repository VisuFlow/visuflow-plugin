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
import org.eclipse.jface.text.ITextSelection;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.jface.viewers.ISelection;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorInput;
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
import org.eclipse.ui.part.FileEditorInput;
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

/**
 * This class handles the navigation between jimple/java to other components of the visuflow plugin.
 * @author kaarthik
 *
 */
public class NavigationHandler extends AbstractHandler {

	/* (non-Javadoc)
	 * @see org.eclipse.core.commands.IHandler#execute(org.eclipse.core.commands.ExecutionEvent)
	 */
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {

		//Get the editor part object
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
		//Get a handle of the current file. This is used further in the code below.
		IFile file = input.getFile();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			//Get a handle to the vertical rulebar of the text editor.
			IVerticalRulerInfo ruleInfo = editor.getAdapter(IVerticalRulerInfo.class);
			IDocumentProvider provider = editor.getDocumentProvider();
			//Grab the document currently being edited.
			IDocument document = provider.getDocument(editor.getEditorInput());
			try {
				//Get the line number of the line on which the user clicked last.
				int lineNumber = ruleInfo.getLineOfLastMouseButtonActivity();
				int offset = document.getLineOffset(lineNumber);
				int length = document.getLineInformation(lineNumber).getLength();
				String content = document.get(offset, length).trim();
				if (content.trim().length() > 0) {
					//Since the classname of a java source file is the same as it's file name, we use the filename as the classname.
					String className = file.getName().substring(0, file.getName().lastIndexOf('.'));
					HashMap<VFUnit, VFMethod> resultantUnit = getSelectedUnit(className, document, content.trim().substring(0, content.length() - 1),
							lineNumber);
					List<VFNode> unit = new ArrayList<>();
					if (resultantUnit.size() > 0) {
						unit.add(new VFNode((VFUnit) resultantUnit.keySet().toArray()[0], 0));
					}
					//The below conditions check for events.
					if (event.getCommand().getId().equals("JimpleEditor.NavigateToCFG")) {
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true, true, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}

					else if (event.getCommand().getId().equals("JimpleEditor.NavigateToUnitView")) {
						try {
							ServiceUtil.getService(DataModel.class).filterGraph(unit, true, true, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (event.getCommand().getId().equals("JimpleEditor.VariablePath")) {
						try {
							List<VFNode> unitList = prepareVariablePath(className, document, content.trim().substring(0, content.length() - 1), lineNumber);
							ServiceUtil.getService(DataModel.class).filterGraph(unitList, true, true, null);
						} catch (Exception e) {
							e.printStackTrace();
						}
					} else if (event.getCommand().getId().equals("JimpleEditor.sourceCodeCommand")) {
						if (unit.size() > 0) {

							LineNumberTag ln = (LineNumberTag) unit.get(0).getUnit().getTag("LineNumberTag");
							if (ln != null) {
								highLightJavaSourceCode(ln.getLineNumber(), className);

							} else {
								IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
								MessageDialog.openInformation(window.getShell(), "Error", "No equivalent java source line found");
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

	/**
	 * This functions highlights units in a jimple file.
	 * @param units The units which need to be highlighted
	 */
	public void highlightJimpleSource(List<VFUnit> units) {

		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				removeJimpleHighlight(true);
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
							e.printStackTrace();
						}
						IDocument document = provider.getDocument(file);
						Integer methodOffset = getMethodOffset(document, unit.getVfMethod());
						FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);

						try {
							IRegion region = findReplaceDocumentAdapter.find(methodOffset,
									FindReplaceDocumentAdapter.escapeForRegExPattern(unit.getUnit().toString()), true, true, false, true);
							// IRegion region = findReplaceDocumentAdapter.find(methodLine, unit.getUnit().toString(), true, true, true, false);
							if (region != null) {
								ITextEditor editor = (ITextEditor) IDE.openEditor(page, file);
								// the 1 added is to include the semi colon
								editor.selectAndReveal(region.getOffset(), region.getLength() + 1);
							}
						} catch (BadLocationException e) {
							e.printStackTrace();
						}
					} catch (PartInitException e) {
						e.printStackTrace();
					}
				}
			}
		});

	}

	/**
	 * Highlights units in java source.
	 * @param unit The unit which needs to be highlighted.
	 */
	public void highlightJavaSource(VFUnit unit) {
		try {
			ServiceUtil.getService(DataModel.class).filterGraph(new ArrayList<VFNode>(), false, true, null);
		} catch (Exception e) {
			e.printStackTrace();
		}
		LineNumberTag ln = (LineNumberTag) unit.getUnit().getTag("LineNumberTag");
		String className = unit.getVfMethod().getVfClass().getSootClass().getName();
		if (ln != null) {
			highLightJavaSourceCode(ln.getLineNumber(), className);
		}
	}

	/**
	 * Removes highligghting from jimple/java files.
	 * @param isJimple Flag to differenciate between java and jimple files.
	 */
	public void removeJimpleHighlight(boolean isJimple) {
		Display.getDefault().asyncExec(() -> {
			try {
				IEditorReference[] references = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getEditorReferences();
				for (int i = 0; i < references.length; i++) {
					IEditorPart editorpart = references[i].getEditor(false);
					if (editorpart instanceof ITextEditor) {
						final ITextEditor editor = (ITextEditor) editorpart;
						IEditorInput input = editorpart.getEditorInput();
						IPath path = ((FileEditorInput) input).getPath();
						boolean extensionJimple = path.getFileExtension().equals("jimple");
						boolean extensionJava = path.getFileExtension().equals("java");
						if (isJimple && extensionJava) {
							ISelection selection = editor.getSelectionProvider().getSelection();
							if (selection != null) {
								ITextSelection textSelection = (ITextSelection) selection;
								editor.selectAndReveal(textSelection.getOffset(), 0);
							}

						} else if (extensionJimple && !isJimple) {
							ISelection selection = editor.getSelectionProvider().getSelection();
							if (selection != null) {
								ITextSelection textSelection = (ITextSelection) selection;
								editor.selectAndReveal(textSelection.getOffset(), 0);
							}
						}

					}
				}
			} catch (NullPointerException e) {
				e.printStackTrace();
			}
		});
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
	private HashMap<VFUnit, VFMethod> getSelectedUnit(String className, IDocument document, String content, int lineNumber) {
		//Get the VFClass which contains all the VFMethods which inturn contains VFUnits
		DataModel dataModel = ServiceUtil.getService(DataModel.class);
		HashMap<VFUnit, VFMethod> map = new HashMap<>();
		//Iterate over all classes
		for (VFClass vfClass : dataModel.listClasses()) {
			//Filter by the class that is passed.
			if (vfClass.getSootClass().getName().equals(className)) {
				//Get all methods of the class.
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

	/**
	 * This method returns the line numbers of all the methods passed to it.
	 * @param document The document with which the user is interacting.
	 * @param vfMethods The list of methods for which the line numbers are required.
	 * @return Map containing method names and their starting line numbers.
	 */
	private Map<String, Integer> getMethodLineNumbers(IDocument document, List<VFMethod> vfMethods) {
		FindReplaceDocumentAdapter findReplaceDocumentAdapter = new FindReplaceDocumentAdapter(document);
		TreeMap<String, Integer> result = new TreeMap<>();
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
		List<VFNode> unitList = new ArrayList<>();
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
		List<VFNode> unitList = new ArrayList<>();
		List<Value> valueList = new ArrayList<>();
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

	/**
	 * This method returns the offset of the method passed to it.
	 * @param document The document user is interacting with.
	 * @param method The method whose offset is required.
	 * @return The offset of the method.
	 */
	private Integer getMethodOffset(IDocument document, VFMethod method) {

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

	/**
	 * This functions highlights the contents of a java source file.
	 * @param lineNumber The line number which needs to be highlighted.
	 * @param className The name of the class which the line which needs to be highlighted is present in.
	 */
	private void highLightJavaSourceCode(int lineNumber, String className) {
		Display.getDefault().asyncExec(new Runnable() {
			@Override
			public void run() {
				removeJimpleHighlight(false);
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
