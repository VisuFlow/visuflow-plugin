package de.unipaderborn.visuflow.debug.handlers;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

import de.unipaderborn.visuflow.Logger;
import de.unipaderborn.visuflow.Visuflow;

public class JavaToCFGHandler extends AbstractHandler {
	
	private Logger logger = Visuflow.getDefault().getLogger();
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart part = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		if (part instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) part;
			IVerticalRulerInfo ruleInfo = editor.getAdapter(IVerticalRulerInfo.class);
			int lineNumber = ruleInfo.getLineOfLastMouseButtonActivity();
			System.out.printf("The java source line number is %d\n",lineNumber);
			//call graph highlighting code here
		}else {
			logger.error("Editor not a  Text Editor");
		}
		return null;
	}

}
