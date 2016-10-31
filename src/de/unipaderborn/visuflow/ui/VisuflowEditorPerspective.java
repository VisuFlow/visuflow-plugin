package de.unipaderborn.visuflow.ui;

import org.eclipse.debug.ui.IDebugUIConstants;
import org.eclipse.ui.IFolderLayout;
import org.eclipse.ui.IPageLayout;
import org.eclipse.ui.IPerspectiveFactory;

public class VisuflowEditorPerspective implements IPerspectiveFactory {

    private static final String ID_FOLDER_JIMPLE = "perspective.folder.jimple";
    private static final String ID_FOLDER_GRAPH = "perspective.folder.graph";
    private static final String ID_FOLDER_CONSOLE = "perspective.folder.console";
    private static final String ID_FOLDER_DEBUG = "perspective.folder.debug";

    private static final String ID_GRAPH_VIEW = "de.unipaderborn.visuflow.ui.view.CFGViewPart";
    //private static final String ID_JIMPLE_VIEW = "graphstreamfeasibility.views.SampleView";
    private static final String ID_JIMPLE_VIEW = "jimpleeditor.editors.MultiPageEditor";
    private static final String ID_CONSOLE_VIEW = "org.eclipse.ui.console.ConsoleView";
    private static final String ID_RESULT_VIEW = "de.unipaderborn.visuflow.ui.view.ResultView";
    private static final String ID_UNIT_VIEW = "de.unipaderborn.visuflow.ui.view.UnitView";

    @Override
    public void createInitialLayout(IPageLayout layout) {
        IFolderLayout consoleFolder = layout.createFolder(ID_FOLDER_CONSOLE, IPageLayout.BOTTOM, (float) 0.66, layout.getEditorArea());
        consoleFolder.addView(ID_CONSOLE_VIEW);

        IFolderLayout debugFolder = layout.createFolder(ID_FOLDER_DEBUG, IPageLayout.RIGHT, (float) 0.5, ID_FOLDER_CONSOLE);
        
        debugFolder.addView(ID_RESULT_VIEW);
        debugFolder.addView(ID_UNIT_VIEW);
        debugFolder.addView(IDebugUIConstants.ID_VARIABLE_VIEW);
        debugFolder.addView(IDebugUIConstants.ID_BREAKPOINT_VIEW);
        debugFolder.addPlaceholder(IDebugUIConstants.ID_EXPRESSION_VIEW);
        debugFolder.addPlaceholder(IDebugUIConstants.ID_REGISTER_VIEW);

        IFolderLayout jimpleFolder = layout.createFolder(ID_FOLDER_JIMPLE, IPageLayout.RIGHT, (float) 0.33, layout.getEditorArea());
        jimpleFolder.addView(ID_JIMPLE_VIEW);

        IFolderLayout graphFolder = layout.createFolder(ID_FOLDER_GRAPH, IPageLayout.RIGHT, (float) 0.33, ID_FOLDER_JIMPLE);
        graphFolder.addView(ID_GRAPH_VIEW);

        layout.addActionSet(IDebugUIConstants.LAUNCH_ACTION_SET);
        layout.addActionSet(IDebugUIConstants.DEBUG_ACTION_SET);

        setContentsOfShowViewMenu(layout);
    }

    /**
     * Sets the initial contents of the "Show View" menu.
     */
    protected void setContentsOfShowViewMenu(IPageLayout layout) {
    	layout.addShowViewShortcut(ID_RESULT_VIEW);
    	layout.addShowViewShortcut(ID_UNIT_VIEW);
    	layout.addShowViewShortcut(IDebugUIConstants.ID_DEBUG_VIEW);
        layout.addShowViewShortcut(IDebugUIConstants.ID_DEBUG_VIEW);
        layout.addShowViewShortcut(IDebugUIConstants.ID_VARIABLE_VIEW);
        layout.addShowViewShortcut(IDebugUIConstants.ID_BREAKPOINT_VIEW);
        layout.addShowViewShortcut(IDebugUIConstants.ID_EXPRESSION_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_OUTLINE);
        layout.addShowViewShortcut(ID_CONSOLE_VIEW);
        layout.addShowViewShortcut(IPageLayout.ID_TASK_LIST);
    }

}
