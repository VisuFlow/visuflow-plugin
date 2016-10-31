package de.unipaderborn.visuflow.debug.handlers;

import org.eclipse.ui.editors.text.TextEditor;

public class JimpleEditor extends TextEditor {

	@Override
	public boolean isEditable() {
	    return false;
	}

	@Override
	public boolean isEditorInputModifiable() {
	    return false;
	}

	@Override
	public boolean isEditorInputReadOnly() {
	    return true;
	}
}
