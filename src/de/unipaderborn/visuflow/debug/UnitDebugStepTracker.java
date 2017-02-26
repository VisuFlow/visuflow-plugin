package de.unipaderborn.visuflow.debug;

import java.util.Collections;
import java.util.Hashtable;
import java.util.List;

import org.apache.commons.lang.ClassUtils;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugEvent;
import org.eclipse.debug.core.DebugException;
import org.eclipse.debug.core.IDebugEventSetListener;
import org.eclipse.debug.core.model.IStackFrame;
import org.eclipse.debug.core.model.IValue;
import org.eclipse.debug.core.model.IVariable;
import org.eclipse.jdt.debug.core.IJavaArray;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaObject;
import org.eclipse.jdt.debug.core.IJavaPrimitiveValue;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.jdt.debug.core.IJavaValue;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;
import org.osgi.service.event.Event;
import org.osgi.service.event.EventConstants;
import org.osgi.service.event.EventHandler;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;
import soot.tagkit.Host;

public class UnitDebugStepTracker implements IDebugEventSetListener, EventHandler {

	private static final transient Logger logger = Visuflow.getDefault().getLogger();

	private IFile file;
	private int lineNumber;
	private int charStart;
	private int charEnd;
	private String unitFqn;

	private IMarker marker;

	private IJavaThread thread;

	public UnitDebugStepTracker(IFile file, int lineNumber, int charStart, int charEnd, String unitFqn) {
		super();
		this.file = file;
		this.lineNumber = lineNumber;
		this.charStart = charStart;
		this.charEnd = charEnd;
		this.unitFqn = unitFqn;

		registerForEvents();
	}

	private void registerForEvents() {
		Hashtable<String, Object> properties = new Hashtable<>();
		properties.put(EventConstants.EVENT_TOPIC, "de/unipaderborn/visuflow/debug/*");
		ServiceUtil.registerService(EventHandler.class, this, properties);
	}

	@Override
	public void handleDebugEvents(DebugEvent[] events) {
		for (int i = 0; i < events.length; i++) {
			DebugEvent debugEvent = events[i];
			try {
				if (isSupendedAtBreakpoint(debugEvent)) {
					if(debugEvent.getSource() instanceof IJavaThread) {
						thread = (IJavaThread) debugEvent.getSource();
					} else {
						continue;
					}

					if (isEventRelatedToThisUnit()) {
						revealLocationInFile();
						revealUnitInGraph();
						highlightLine();
					}
				} else if (isResumeByClientRequest(debugEvent)) {
					removeLineHighlight();
				} else if (debugEvent.getKind() == DebugEvent.TERMINATE) {
					// remove this debug event listener to release it for garbage collection
					// DebugPlugin.getDefault().removeDebugEventListener(this);
				}
			} catch (Exception e) {
				logger.error("Error while tracking unit debugging steps", e);
			}
		}
	}

	private void revealUnitInGraph() throws Exception {
		DataModel model = ServiceUtil.getService(DataModel.class);
		VFUnit vfUnit = model.getVFUnit(unitFqn);
		if(vfUnit != null) {
			VFNode node = new VFNode(vfUnit, 0); // TODO ask shanki, if ID 0 is ok
			model.filterGraph(Collections.singletonList(node), true, "filter");
		}
	}

	private void highlightLine() throws CoreException {
		if(marker == null) {
			marker = file.createMarker("visuflow.debug.instructionPointer.marker");
			marker.setAttribute(IMarker.MESSAGE, "");
			marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
			marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
			marker.setAttribute(IMarker.CHAR_START, charStart);
			marker.setAttribute(IMarker.CHAR_END, charEnd);
		}
	}

	private void removeLineHighlight() throws CoreException {
		if(marker != null) {
			marker.delete();
			marker = null;
		}
	}

	private void revealLocationInFile() {
		Display.getDefault().syncExec(() -> {
			try {
				ITextEditor textEditor = openFileInEditor(file);
				scrollToPosition(textEditor, charStart);
			} catch (PartInitException e) {
				logger.error("Couldn't open jimple file", e);
			}
		});
	}

	private ITextEditor openFileInEditor(IFile file) throws PartInitException {
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		IWorkbenchPage page = window.getActivePage();
		IEditorPart editor = IDE.openEditor(page, file, true);
		ITextEditor textEditor = editor.getAdapter(ITextEditor.class);
		return textEditor;
	}

	private void scrollToPosition(ITextEditor textEditor, int position) {
		if(textEditor != null) {
			textEditor.selectAndReveal(charStart, 0);
			textEditor.resetHighlightRange();
		}
	}

	private boolean topStackFrameHasUnit(String fqn) throws CoreException {
		if (!thread.hasStackFrames()) {
			return false;
		}

		IStackFrame top = thread.getTopStackFrame();
		if (top.getVariables().length > 0) {
			for (IVariable var : top.getVariables()) {
				try {
					IValue value = var.getValue();
					if (value instanceof IJavaValue) {
						IJavaObject javaValue = (IJavaObject) value;
						IJavaDebugTarget debugTarget = thread.getDebugTarget().getAdapter(IJavaDebugTarget.class);
						IJavaValue arg = debugTarget.newValue("Fully Qualified Name");
						// the signature (2nd argument) can be retrieved with javap. Unit extends soot.tagkit.Host for the tag support
						// -> javap -cp soot-trunk.jar -s soot.tagkit.Host
						// the signature is in the output under "descriptor"
						IJavaType type = javaValue.getJavaType();
						if (isTagHost(type)) { // check, if this is a unit, which contains Tags
							IJavaValue fqnTag = javaValue.sendMessage("getTag", "(Ljava/lang/String;)Lsoot/tagkit/Tag;", new IJavaValue[] { arg }, thread,
									false);
							IJavaValue tagValue = ((IJavaObject) fqnTag).sendMessage("getValue", "()[B", new IJavaValue[0], thread, false);
							IJavaArray byteArray = (IJavaArray) tagValue;
							byte[] b = new byte[byteArray.getLength()];
							for (int i = 0; i < b.length; i++) {
								IJavaPrimitiveValue byteValue = (IJavaPrimitiveValue) byteArray.getValue(i);
								b[i] = byteValue.getByteValue();
							}
							String currentUnitFqn = new String(b);
							if (currentUnitFqn.equals(unitFqn)) {
								return true;
							}
						}
					}
				} catch (Exception e) {
					logger.error("Couldn't retrieve variable " + var.getName() + " from top stack frame", e);
				}
			}
		}
		return false;
	}

	private boolean isTagHost(IJavaType type) throws ClassNotFoundException, DebugException {
		try {
			Class<?> clazz = Class.forName(type.getName());
			List<?> interfaces = ClassUtils.getAllInterfaces(clazz);
			return interfaces.contains(Host.class);
		} catch(ClassNotFoundException e) {
			// outside of scope, we can ignore this
			return false;
		}
	}

	private boolean isEventRelatedToThisUnit() throws CoreException {
		return topStackFrameHasUnit(unitFqn);
	}

	private boolean isResumeByClientRequest(DebugEvent debugEvent) {
		return debugEvent.getKind() == DebugEvent.RESUME && debugEvent.getDetail() == DebugEvent.CLIENT_REQUEST;
	}

	private boolean isSupendedAtBreakpoint(DebugEvent debugEvent) {
		return debugEvent.getKind() == DebugEvent.SUSPEND && debugEvent.getDetail() == DebugEvent.BREAKPOINT;
	}

	@Override
	public void handleEvent(Event event) {
		if(event.getTopic().equals("de/unipaderborn/visuflow/debug/resume")) {
			handleResumeEvent();
		} else if(event.getTopic().equals("de/unipaderborn/visuflow/debug/stepOver")) {
			handleStepOverEvent();
		}
	}

	private void handleResumeEvent() {
		if(thread != null && thread.isSuspended() && thread.canResume()) {
			try {
				thread.resume();
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}

	private void handleStepOverEvent() {
		if(thread != null && thread.isSuspended() && thread.canStepOver()) {
			try {
				thread.stepOver();
			} catch (DebugException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
	}
}
