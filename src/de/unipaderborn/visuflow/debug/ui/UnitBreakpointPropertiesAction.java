package de.unipaderborn.visuflow.debug.ui;

import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.RulerBreakpointAction;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jdt.debug.core.IJavaLineBreakpoint;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

import beaver.Action;
import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;
import de.unipaderborn.visuflow.VisuflowConstants;
import de.unipaderborn.visuflow.debug.UnitLocation;
import de.unipaderborn.visuflow.debug.UnitLocator;
import de.unipaderborn.visuflow.model.VFUnit;

public class UnitBreakpointPropertiesAction extends RulerBreakpointAction implements IUpdate, VisuflowConstants {

	private Logger logger = Visuflow.getDefault().getLogger();
	private IBreakpoint fBreakpoint;

	/**
	 * Creates the action to enable/disable breakpoints
	 */
	public UnitBreakpointPropertiesAction(ITextEditor editor, IVerticalRulerInfo info) {
		super(editor, info);
		setText("Unit Breakpoint Properties");
	}

	/**
	 * @see Action#run()
	 */
	@Override
	public void run() {
		System.out.println("run()");
		System.out.println(getVerticalRulerInfo());
		System.out.println(getEditor());
		System.out.println(getBreakpoint());

		UnitBreakpointPropertiesDialog dialog = new UnitBreakpointPropertiesDialog(getEditor().getSite().getShell());
		int returnCode = dialog.open();
		if(returnCode == Dialog.OK) {
			String condition = null;
			VFUnit unit = null;
			if(dialog.isSuspendOnUnit()) {
				unit = dialog.getSelectedUnit();
				if(unit != null) {
					System.out.println(unit.getFullyQualifiedName());
					String escapedRequiredFqn = unit.getFullyQualifiedName().replaceAll("\"", "\\\\\"");
					// FIXME find out the name of the unit
					condition = "new String(d.getTag(\"Fully Qualified Name\").getValue()).equals(\""+escapedRequiredFqn+"\")";
				} else {
					// no unit selected, nothing to do
				}
			} else {
				String type = dialog.getUnitType();
				condition = "d instanceof " + type;
			}

			if(condition != null) {
				if(fBreakpoint instanceof IJavaLineBreakpoint) {
					IJavaLineBreakpoint breakpoint = (IJavaLineBreakpoint) fBreakpoint;
					try {
						breakpoint.setConditionEnabled(true);
						breakpoint.setCondition(condition);
						breakpoint.addBreakpointListener(JAVA_BREAKPOINT_LISTENER);

						if(unit != null) {
							UnitLocation location = UnitLocator.locateUnit(unit);
							IFile file = location.project.getFile(location.jimpleFile);

							IMarker m = breakpoint.getMarker();
							m.setAttribute("Jimple.breakpoint.type", "unit");
							m.setAttribute("Jimple." + IMarker.LINE_NUMBER, m.getAttribute(IMarker.LINE_NUMBER, -1));
							m.setAttribute("Jimple.file", file.getProjectRelativePath().toPortableString());
							m.setAttribute("Jimple.project", file.getProject().getName());
							m.setAttribute("Jimple.unit.charStart", location.charStart);
							m.setAttribute("Jimple.unit.charEnd", location.charEnd);
							m.setAttribute("Jimple.unit.fqn", unit.getFullyQualifiedName());
						} else {
							IMarker m = breakpoint.getMarker();
							m.setAttribute("Jimple.breakpoint.type", "unitType");
						}

					} catch (Exception e) {
						logger.error("Couldn't add condition to breakpoint", e);
					}
				}
			}
		}
	}

	@Override
	public void update() {
		fBreakpoint = null;
		IBreakpoint breakpoint = getBreakpoint();
		if (breakpoint != null && (breakpoint instanceof IJavaBreakpoint)) {
			fBreakpoint = breakpoint;
		}
		setEnabled(fBreakpoint != null);
	}
}
