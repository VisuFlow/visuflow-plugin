package de.unipaderborn.visuflow.debug.handlers;

import java.io.File;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class ViewJavaSource extends AbstractHandler {

	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindowChecked(event);
		//      MessageDialog.openInformation(
		//                   window.getShell(),
		//                   "TestPlugIn",
		//                   "Executed sample Action");
		//      System.out.println("//////////////////////"+HandlerUtil.getActiveEditor(event).getEditorSite());
		//      System.out.println(HandlerUtil.getCurrentSelection(event).toString());
		//      System.out.println(window.getActivePage());
		//ISelection selection = HandlerUtil.getCurrentSelectionChecked(event);
		File fileToOpen = new File("C:/Users/Shashank B S/Documents/Projects/visuflow-plugin/targets2/de/visuflow/analyzeMe/ex2/TargetClass2.java");

		if (fileToOpen.exists() && fileToOpen.isFile()) {
			IFileStore fileStore = EFS.getLocalFileSystem().getStore(fileToOpen.toURI());
			IWorkbenchPage page = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage();

			try {
				IDE.openEditorOnFileStore( page, fileStore );
			} catch ( PartInitException e ) {
				//Put your exception handler here if you wish to
			}
		} else {
			System.out.println("File does not exist");
		}
		IEditorPart editor = window.getActivePage().getActiveEditor();

		ITextEditor txteditor = editor.getAdapter(ITextEditor.class);
		if (txteditor != null) {
			IDocumentProvider provider = txteditor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());
		}

		ITextEditor editor1 = (ITextEditor) editor;
		IDocument document = editor1.getDocumentProvider().getDocument(
				editor.getEditorInput());
		if (document != null) {
			IRegion lineInfo = null;
			try {
				// line count internaly starts with 0, and not with 1 like in
				// GUI
				lineInfo = document.getLineInformation(17 - 1);
			} catch (BadLocationException e) {
				// ignored because line number may not really exist in document,
				// we guess this...
			}
			if (lineInfo != null) {
				editor1.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
				// editor1.setHighlightRange(lineInfo.getOffset(), lineInfo.getLength(), true);

			}
		}
		//      IEditorInput input = editor.getEditorInput();
		//      FileEditorInput file = (FileEditorInput)input;
		//      ITextEditor itext = (ITextEditor)editor;
		//      System.out.println("mmmmmmmmmmmmmmmmm"+editor.getSite());
		return null;
	}

}
