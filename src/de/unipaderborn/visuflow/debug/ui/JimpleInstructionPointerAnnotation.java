package de.unipaderborn.visuflow.debug.ui;

import org.eclipse.jface.text.source.Annotation;
import org.eclipse.swt.graphics.Image;

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
