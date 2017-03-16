package de.unipaderborn.visuflow.debug;

import java.util.Collections;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IProject;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.resources.ResourcesPlugin;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.debug.core.DebugException;
import org.eclipse.jdt.core.dom.Message;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaBreakpointListener;
import org.eclipse.jdt.debug.core.IJavaDebugTarget;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jdt.debug.core.IJavaThread;
import org.eclipse.jdt.debug.core.IJavaType;
import org.eclipse.swt.widgets.Display;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.ITextEditor;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFNode;
import de.unipaderborn.visuflow.model.VFUnit;
import de.unipaderborn.visuflow.util.ServiceUtil;

public class JavaBreakpointListener implements IJavaBreakpointListener, VisuflowConstants {

	private static final transient Logger logger = Visuflow.getDefault().getLogger();

	@Override
	public int breakpointHit(IJavaThread thread, IJavaBreakpoint breakpoint) {
		JimpleBreakpointManager.getInstance().breakpointHit(thread, breakpoint);

		try {
			IMarker marker = breakpoint.getMarker();
			if(marker.getAttribute("Jimple.breakpoint.type") == null) {
				// we are missing breakpoint type information. don't know what to do -> return
				return JavaBreakpointListener.DONT_CARE;
			}

			String type = marker.getAttribute("Jimple.breakpoint.type").toString();
			if(!"unit".equals(type)) {
				// this is a breakpoint for a certain type of units

				// at the moment we don't know, which unit this is, so we just remove the
				// instruction pointer marker and continue
				String project = marker.getAttribute("Jimple.project").toString();
				removeOldInstructionPointer(project);

				return 0;
			} else {
				// this is a unit breakpoint
				String project = marker.getAttribute("Jimple.project").toString();
				String path = marker.getAttribute("Jimple.file").toString();
				IFile file = getFile(project, path);
				int line = marker.getAttribute("Jimple." + IMarker.LINE_NUMBER, -1);
				int charStart = marker.getAttribute("Jimple.unit.charStart", -1);
				int charEnd = marker.getAttribute("Jimple.unit.charEnd", -1);
				String unitFqn = marker.getAttribute("Jimple.unit.fqn").toString();

				revealLocationInFile(file, charStart);
				revealUnitInGraph(unitFqn);
				highlightLine(project, path, line, charStart, charEnd);
			}
		} catch (Exception e) {
			logger.error("Couldn't open hit breakpoint location in jimple file", e);
		}
		return 0;
	}

	private void revealLocationInFile(IFile file, int position) {
		Display.getDefault().syncExec(() -> {
			try {
				ITextEditor textEditor = openFileInEditor(file);
				scrollToPosition(textEditor, position);
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
			textEditor.selectAndReveal(position, 0);
			textEditor.resetHighlightRange();
		}
	}

	private IFile getFile(String projectName, String path) {
		IProject project = ResourcesPlugin.getWorkspace().getRoot().getProject(projectName);
		IFile file = project.getFile(path);
		return file;
	}

	private void revealUnitInGraph(String unitFqn) throws Exception {
		DataModel model = ServiceUtil.getService(DataModel.class);
		VFUnit vfUnit = model.getVFUnit(unitFqn);
		if(vfUnit != null) {
			VFNode node = new VFNode(vfUnit, 0);
			model.filterGraph(Collections.singletonList(node), true, true, "filter");
		}
	}

	private void highlightLine(String project, String file, int line, int charStart, int charEnd) throws CoreException {
		removeOldInstructionPointer(project);
		addNewInstructionPointer(project, file, line, charStart, charEnd);
	}

	private void addNewInstructionPointer(String project, String path, int line, int charStart, int charEnd) throws CoreException {
		IFile file = getFile(project, path);
		IMarker marker = file.createMarker("visuflow.debug.instructionPointer.marker");
		marker.setAttribute(IMarker.MESSAGE, "");
		marker.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_WARNING);
		marker.setAttribute(IMarker.LINE_NUMBER, line);
		marker.setAttribute(IMarker.CHAR_START, charStart);
		marker.setAttribute(IMarker.CHAR_END, charEnd);
	}

	private void removeOldInstructionPointer(String project) throws CoreException {
		IProject currentProject = ResourcesPlugin.getWorkspace().getRoot().getProject(project);
		IMarker[] instructionPointers = currentProject.findMarkers(JIMPLE_INSTRUCTIONPOINTER_MARKER, false, IResource.DEPTH_INFINITE);
		for (IMarker m : instructionPointers) {
			m.delete();
		}
	}

	// ################## the rest of IJavaBreakpointListener is unused for now ###################
	@Override
	public void addingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	@Override
	public int installingBreakpoint(IJavaDebugTarget target, IJavaBreakpoint breakpoint, IJavaType type) {
		return 0;
	}

	@Override
	public void breakpointInstalled(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	@Override
	public void breakpointRemoved(IJavaDebugTarget target, IJavaBreakpoint breakpoint) {
	}

	@Override
	public void breakpointHasRuntimeException(IJavaLineBreakpoint breakpoint, DebugException exception) {
	}

	@Override
	public void breakpointHasCompilationErrors(IJavaLineBreakpoint breakpoint, Message[] errors) {
	}
}