package de.unipaderborn.visuflow.debug.handlers;

import java.util.List;

import org.eclipse.core.commands.AbstractHandler;
import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.core.resources.IFile;
import org.eclipse.core.resources.IMarker;
import org.eclipse.core.resources.IResource;
import org.eclipse.core.runtime.CoreException;
import org.eclipse.jface.text.BadLocationException;
import org.eclipse.jface.text.IDocument;
import org.eclipse.jface.text.source.IVerticalRulerInfo;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.IFileEditorInput;
import org.eclipse.ui.IWorkbenchWindow;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.handlers.HandlerUtil;
import org.eclipse.ui.texteditor.IDocumentProvider;
import org.eclipse.ui.texteditor.ITextEditor;

import de.unipaderborn.visuflow.model.DataModel;
import de.unipaderborn.visuflow.model.VFMethod;
import de.unipaderborn.visuflow.util.ServiceUtil;
import scala.reflect.macros.internal.macroImpl;

import org.eclipse.jface.dialogs.*;

public class JimpleBreakPointHandler extends AbstractHandler {


	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		DataModel model = ServiceUtil.getService(DataModel.class);
		List<VFMethod> methods = model.listMethods(null);
		for (VFMethod vfMethod : methods) {
			System.out.println(vfMethod.getBody());
		}

		IEditorPart part =PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IWorkbenchWindow window = HandlerUtil.getActiveWorkbenchWindow(event);
		IFileEditorInput input = (IFileEditorInput)part.getEditorInput() ;
		IFile file = input.getFile();
		IResource res = (IResource) file;

		if (part instanceof ITextEditor)
		{
			final ITextEditor editor = (ITextEditor) part;
			IVerticalRulerInfo ruleInfo = editor.getAdapter(IVerticalRulerInfo.class);
			IDocumentProvider provider = editor.getDocumentProvider();
			IDocument document = provider.getDocument(editor.getEditorInput());
			try {
				int lineNumber = ruleInfo.getLineOfLastMouseButtonActivity();
				int offset=document.getLineOffset(lineNumber);
				int length=document.getLineInformation(lineNumber).getLength();
				int actualLineNumber = lineNumber+1;
				String content =document.get(offset,length).trim();

				IMarker[] problems = null;
				int depth = IResource.DEPTH_ZERO;
				problems = res.findMarkers(IMarker.TASK, true, depth);
				Boolean markerPresent = false;
				for (IMarker item : problems) {
					int markerLineNmber = (int) item.getAttribute(IMarker.LINE_NUMBER);
					if(markerLineNmber == actualLineNumber){
						markerPresent=true;
						item.delete();
						MessageDialog.openInformation(window.getShell(), "Debugger deleted at line: "+actualLineNumber,content);
					}
				}

				if(!markerPresent){

					IMarker m = res.createMarker(IMarker.TASK);
					m.setAttribute(IMarker.LINE_NUMBER,actualLineNumber);
					m.setAttribute(IMarker.MESSAGE, content);
					m.setAttribute(IMarker.TEXT, "Jimple Breakpoint");
					m.setAttribute(IMarker.PRIORITY, IMarker.PRIORITY_HIGH);
					m.setAttribute(IMarker.SEVERITY, IMarker.SEVERITY_INFO);
					MessageDialog.openInformation(window.getShell(), "Debugger set at line: "+actualLineNumber,content);
				}


				System.out.printf("Line Number:%d\n",(lineNumber+1));
				System.out.printf("The contents of the line :   %s",content);

			} catch (BadLocationException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			} catch (CoreException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
		return null;
	}
}
