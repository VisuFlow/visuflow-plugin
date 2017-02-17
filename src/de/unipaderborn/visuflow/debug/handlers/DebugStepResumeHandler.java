package de.unipaderborn.visuflow.debug.handlers;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.eclipse.core.commands.ExecutionEvent;
import org.eclipse.core.commands.ExecutionException;
import org.eclipse.jface.text.source.Annotation;
import org.eclipse.jface.text.source.IAnnotationModel;
import org.eclipse.swt.graphics.Image;
import org.eclipse.ui.IEditorInput;
import org.eclipse.ui.IEditorPart;
import org.eclipse.ui.PlatformUI;
import org.eclipse.ui.texteditor.ITextEditor;

public class DebugStepResumeHandler extends org.eclipse.core.commands.AbstractHandler {
	@Override
	public Object execute(ExecutionEvent event) throws ExecutionException {
		IEditorPart editorPart = PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage().getActiveEditor();
		IEditorInput editorInput = editorPart.getEditorInput();
		if (editorPart instanceof ITextEditor) {
			final ITextEditor editor = (ITextEditor) editorPart;
			IAnnotationModel annotationModel = editor.getDocumentProvider().getAnnotationModel(editorInput);
			removeOldAnnotations(annotationModel);
		}
		return null;
	}


	private void removeOldAnnotations(IAnnotationModel annotationModel) {
		Iterator<Annotation> annotations = annotationModel.getAnnotationIterator();
		List<Annotation> toRemove = new ArrayList<>();
		while(annotations.hasNext()) {
			Annotation current = annotations.next();
			if(current instanceof JimpleInstructionPointerAnnotation) {
				toRemove.add(current);
			}
		}
		for (Annotation annotation : toRemove) {
			annotationModel.removeAnnotation(annotation);
		}
	}


	public class JimpleInstructionPointerAnnotation extends Annotation {
		private Image image;

		public JimpleInstructionPointerAnnotation() {
			super(false);
		}

		public JimpleInstructionPointerAnnotation(Image image) {
			this();
			this.image = image;
		}

		public Image getImage() {
			return image;
		}
	}
}
