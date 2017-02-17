package de.unipaderborn.visuflow.debug.handlers;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IMarkerDelta;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugPlugin;
import org.eclipse.debug.core.IBreakpointListener;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaMethodBreakpoint;
import org.eclipse.jdt.debug.core.IJavaStackFrame;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.JDIDebugModel;
import org.eclipse.jface.dialogs.MessageDialog;
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

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.debug.BreakpointLocator;
import de.unipaderborn.visuflow.debug.BreakpointLocator.BreakpointLocation;
import de.unipaderborn.visuflow.debug.JimpleBreakpoint;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFClass;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.MapUtil;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class JimpleBreakPointHandler extends AbstractHandler {

	private Logger logger = Visuflow.getDefault().getLogger();
	private BreakpointLocator breakpointLocator = new BreakpointLocator();

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part instanceof ITextEditor) {
			IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
			IFileEditorInput input = (IFileEditorInput) part.getEditorInput();
			IFile file = input.getFile();
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
						IResource res = file;
						problems = res.findMarkers(IBreakpoint.BREAKPOINT_MARKER, true,
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
							IMarker m = res.createMarker(IBreakpoint.BREAKPOINT_MARKER);
							m.setAttribute(IMarker.LINE_NUMBER, actualLineNumber);
							m.setAttribute(IMarker.MESSAGE, content);
							m.setAttribute(IMarker.TEXT, "Jimple Breakpoint");
							m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
							m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
							m.setAttribute(IBreakpoint.PERSISTED, true);
							JimpleBreakpoint jimpleBreakpoint = new JimpleBreakpoint(m);
							DebugPlugin.getDefault().getBreakpointManager().addBreakpoint(jimpleBreakpoint);
							DebugPlugin.getDefault().getBreakpointManager().addBreakpointListener(new IBreakpointListener() {
								@Override
								public void breakpointRemoved(IBreakpoint breakpoint, IMarkerDelta delta) {
									if(breakpoint instanceof JimpleBreakpoint) {
										try {
											breakpoint.delete();
										} catch (CoreException e) {
											// TODO
											e.printStackTrace();
										}
									}
								}

								@Override
								public void breakpointChanged(IBreakpoint breakpoint, IMarkerDelta delta) {
								}

								@Override
								public void breakpointAdded(IBreakpoint breakpoint) {
								}
							});

							List<BreakpointLocation> breakpointLocations = breakpointLocator.findFlowFunctions();
							for (BreakpointLocation breakpointLocation : breakpointLocations) {
								System.out.println("Install breakpoint at " + breakpointLocation);
								IBreakpoint javaBreakpoint = createMethodEntryBreapoint(breakpointLocation);
								if(javaBreakpoint instanceof IJavaLineBreakpoint) {
									IJavaLineBreakpoint javaLineBreakpoint = (IJavaLineBreakpoint) javaBreakpoint;
									javaLineBreakpoint.setConditionEnabled(true);
									String requiredFqn = resultantUnit.getFullyQualifiedName();
									javaLineBreakpoint.setCondition("new String(d.getTag(\"Fully Qualified Name\").getValue()).equals(\""+requiredFqn+"\")");

									IMarker javaBreakpointMarker = javaBreakpoint.getMarker();
									javaBreakpointMarker.setAttribute("Jimple" + IMarker.LINE_NUMBER, actualLineNumber);
									javaBreakpointMarker.setAttribute("Jimple" + IMarker.MESSAGE, content);
									javaBreakpointMarker.setAttribute("Jimple" + IMarker.TEXT, "Jimple Breakpoint");
									javaBreakpointMarker.setAttribute("Jimple" + IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
									javaBreakpointMarker.setAttribute("Jimple" + IMarker.SEVERITY, IMarker.SEVERITY_INFO);
									javaBreakpointMarker.setAttribute("JimpleUnitFqn", requiredFqn);
									jimpleBreakpoint.addJavaBreakpoint(javaBreakpoint);
								} else {
									logger.error("Couldn't set unit condition for jimple breakpoint, because it is no IJavaLineBreakpoint");
								}
							}


						}

						DebugPlugin.getDefault().addDebugEventListener(new IDebugEventSetListener() {
							@Override
							public void handleDebugEvents(DebugEvent[] events) {
								// System.out.println("Events " + events.length);
								for (int i = 0; i < events.length; i++) {
									DebugEvent debugEvent = events[i];
									if (debugEvent.getKind() == DebugEvent.SUSPEND && debugEvent.getDetail() == DebugEvent.BREAKPOINT) {
										IJavaThread thread = (IJavaThread) debugEvent.getSource();
										try {
											IJavaStackFrame top = (IJavaStackFrame) thread.getTopStackFrame();
											if(top == null) {
												continue;
											}

											IBreakpoint[] breakpoints = thread.getBreakpoints();
											for (IBreakpoint breakpoint : breakpoints) {
												IJavaMethodBreakpoint methodBreakpoint = breakpoint.getAdapter(IJavaMethodBreakpoint.class);
												String unitFqn = (String) methodBreakpoint.getMarker().getAttribute("JimpleUnitFqn");
												DataModel model = ServiceUtil.getService(DataModel.class);
												VFUnit unit = model.getVFUnit(unitFqn);
												new NavigationHandler().HighlightJimpleLine(Collections.singletonList(unit));
											}
										} catch (Exception e) {
											// TODO Auto-generated catch block
											e.printStackTrace();
										}
									} else if (debugEvent.getKind() == DebugEvent.TERMINATE) {
										// remove this debug event listener to release it for garbage collection
										//DebugPlugin.getDefault().removeDebugEventListener(this);
									}
								}
							}
						});
					}
				} else {
					MessageDialog.openInformation(window.getShell(), "Breakpoint could not be placed", "Error in inserting breakpoint");
				}
			} catch (Exception e) {
				logger.error("Couldn't create jimple breakpoint", e);
				MessageDialog.openInformation(window.getShell(), "Breakpoint could not be placed", "Error in inserting breakpoint");
			}
		}
		return null;
	}

	private IBreakpoint createMethodEntryBreapoint(BreakpointLocation location) throws CoreException {
		int charStart = location.offset;
		int charEnd = charStart + location.length;
		int hitCount = 0; // no hit count
		boolean register = true; // register at BreakpointManager
		boolean entry = true; // suspend at method entry
		boolean exit = false; // suspend at method exit
		boolean nativeOnly = false; // suspend for native methods
		Map<String, Object> attrs = null;

		System.out.println("resource: " + location.resource);
		System.out.println("class: " + location.className);
		System.out.println("method: " + location.methodName);
		System.out.println("signature: " + location.methodSignature);
		System.out.println("resource: " + location.resource);
		System.out.println("entry: " + entry);
		System.out.println("exit: " + exit);
		System.out.println("nativeOnly: " + nativeOnly);
		System.out.println("line: " + location.lineNumber);
		System.out.println("charStart: " + charStart);
		System.out.println("charEnd: " + charEnd);
		System.out.println("hit count: " + hitCount);
		System.out.println("register: " + register);
		System.out.println("attrs: " + attrs);

		IJavaMethodBreakpoint breakpoint = JDIDebugModel.createMethodBreakpoint(location.resource, location.className, location.methodName,
				location.methodSignature, entry, exit, nativeOnly, location.lineNumber, charStart, charEnd, hitCount, register, attrs);
		//breakpoints.add(breakpoint);
		return breakpoint;
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
}
