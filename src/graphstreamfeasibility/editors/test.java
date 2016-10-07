package graphstreamfeasibility.editors;

import java.io.File;

import org.eclipse.core.filesystem.EFS;
import org.eclipse.core.filesystem.IFileStore;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.IRegion;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IWorkbenchPage;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PartInitException;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.ide.IDE;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

public class test {
	public void highlightCodeLine()
	{
		IWorkbenchWindow window = PlatformUI.getWorkbench().getActiveWorkbenchWindow();
		File fileToOpen = new File("C:/Users/Shashank B S/Documents/Projects/GraphStreamFeasibility/targets2/de/visuflow/analyzeMe/ex2/TargetClass2.java");

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

		ITextEditor txteditor = (ITextEditor) editor.getAdapter(ITextEditor.class);
		if (txteditor != null) {
			IDocumentProvider provider = txteditor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());
			System.out.println(txteditor.getHighlightRange());
			System.out.println("==============================="+document);
			System.out.println(document);
		}

		ITextEditor editor1 = (ITextEditor) editor;
		IDocument document = editor1.getDocumentProvider().getDocument(
				editor.getEditorInput());
		if (document != null) {
			IRegion lineInfo = null;
			try {
				// line count internaly starts with 0, and not with 1 like in
				// GUI
				lineInfo = document.getLineInformation(14 - 1);
			} catch (org.eclipse.jface.text.BadLocationException e) {
				// ignored because line number may not really exist in document,
				// we guess this...
			}
			if (lineInfo != null) {
				System.out.println("=================>>>>>>>>>"+lineInfo);
				editor1.selectAndReveal(lineInfo.getOffset(), lineInfo.getLength());
				editor1.setHighlightRange(lineInfo.getOffset(), lineInfo.getLength(), true);

			}
		}

	}
}
