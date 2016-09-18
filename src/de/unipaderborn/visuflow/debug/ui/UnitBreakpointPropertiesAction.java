package de.unipaderborn.visuflow.debug.ui;

import org.eclipse.debug.core.model.IBreakpoint;
import org.eclipse.debug.ui.actions.RulerBreakpointAction;
import org.eclipse.jdt.debug.core.IJavaBreakpoint;
import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.texteditor.ITextEditor;
import org.eclipse.ui.texteditor.IUpdate;

import beaver.Action;

public class UnitBreakpointPropertiesAction extends RulerBreakpointAction implements IUpdate {

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

        //        if (getBreakpoint() != null) {
        //            PropertyDialogAction action=
        //                    new PropertyDialogAction(getEditor().getEditorSite(), new ISelectionProvider() {
        //                        @Override
        //                        public void addSelectionChangedListener(ISelectionChangedListener listener) {
        //                        }
        //                        @Override
        //                        public ISelection getSelection() {
        //                            return new StructuredSelection(getBreakpoint());
        //                        }
        //                        @Override
        //                        public void removeSelectionChangedListener(ISelectionChangedListener listener) {
        //                        }
        //                        @Override
        //                        public void setSelection(ISelection selection) {
        //                        }
        //                    });
        //            action.run();
        //        }

        UnitBreakpointPropertiesDialog dialog = new UnitBreakpointPropertiesDialog(getEditor().getSite().getShell());
        int returnCode = dialog.open();
        if(returnCode == Dialog.OK) {
            System.out.println("OK");
        } else {
            System.out.println("Cancel");
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
